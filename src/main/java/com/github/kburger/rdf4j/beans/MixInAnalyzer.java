/**
 * Copyright 2017 https://github.com/kburger
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.kburger.rdf4j.beans;

import java.beans.Introspector;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kburger.rdf4j.beans.annotation.Predicate;
import com.github.kburger.rdf4j.beans.annotation.Type;

/**
 * Provides Jackson-like mix-in annotation functionality for rdf4j-beans.
 * 
 * <p>Mix-ins are registered against a <i>target</i> class. The annotations provided by the mix-in
 * will be applied to the target class.
 * 
 * <p>Mix-ins can be implemented through interfaces or classes. An interface mix-in will mimic a
 * getter method signature. The following example illustrates the usage of a mix-in interface.
 * <pre>
 * class Target {
 *     private String value;
 * 
 *     public String getValue() {
 *         return value;
 *     }
 * 
 *     public void setValue(String value) {
 *         this.value = value;
 *     }
 * }
 * 
 * // the mixin class will provide an annotation on the value property of the Target class
 * interface MixIn {
 *     &#64;Predicate("http://purl.org/dc/terms/title")
 *     String getValue();
 * }
 * 
 * // register the mixin with the analyzer
 * mixInAnalyzer.registerMixIn(Target.class, MixIn.class);
 * // analyze the target class, now the mixin annotation is added to the analysis result
 * mixInAnalyzer.analyzeMixIn(Target.class, new ClassAnalysis());
 * </pre>
 * 
 * @see <a href="https://github.com/FasterXML/jackson-docs/wiki/JacksonMixInAnnotations">Jackson Feature: Mix-in Annotations.</a>
 */
public class MixInAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(MixInAnalyzer.class);
    
    /** General bean property getter method prefix. */
    private static final String PREFIX_GET = "get";
    /** Boolean bean property getter method prefix. */
    private static final String PREFIX_IS = "is";
    /** General bean property setter method prefix. */
    private static final String PREFIX_SET = "set";
    
    /** Registered mix-in classes. Map key is the target class, map value is the mix-in class. */
    private final Map<Class<?>, Class<?>> mixIns;
    
    public MixInAnalyzer() {
        mixIns = new HashMap<>();
    }
    
    /**
     * Register a new mix-in class for a specified target.
     * @param target the target bean class on which the mix-in will be applied.
     * @param mixIn the mix-in class providing additonal annotations to the target class.
     */
    public void registerMixIn(final Class<?> target, final Class<?> mixIn) {
        mixIns.put(target, mixIn);
    }
    
    /**
     * Analyzes the given {@code target} class against any registered mix-in classes. The provided
     * {@code classAnalysis} will be updated accordingly, if any mix-ins are found.
     * @param target the target bean class.
     * @param classAnalysis a class analysis instance on which the mix-in will be applied.
     */
    public void analyzeMixIn(final Class<?> target, final ClassAnalysis classAnalysis) {
        if (!mixIns.containsKey(target)) {
            logger.debug("No mix-in class found for {}", target);
            return;
        }
        
        final Class<?> mixIn = mixIns.get(target);
        
        if (mixIn.isAnnotationPresent(Type.class)) {
            final PropertyAnalysis<Type> typeAnalysis =
                    new PropertyAnalysis<Type>(mixIn.getAnnotation(Type.class), null, null);
            classAnalysis.setTypeProperty(typeAnalysis);
        }
        
        for (final Method mixInMethod : mixIn.getDeclaredMethods()) {
            analyzeMethod(target, mixInMethod, classAnalysis);
        }
    }
    
    /**
     * Analyzes a mix-in method for annotation presence. Mix-in methods will be mapped to their
     * equivalent on the {@code target} class. If the method signature does not match, the mix-in
     * method will be ignored.
     * @param target the target bean class.
     * @param mixInMethod mix-in method under analysis.
     * @param classAnalysis the analysis result instance.
     */
    private void analyzeMethod(final Class<?> target, final Method mixInMethod,
            final ClassAnalysis classAnalysis) {
        final String methodName = mixInMethod.getName();
        
        final String rawPropertyName;
        if (methodName.startsWith(PREFIX_GET)) {
            rawPropertyName = methodName.substring(PREFIX_GET.length());
        } else if (methodName.startsWith(PREFIX_IS)) {
            rawPropertyName = methodName.substring(PREFIX_IS.length());
        } else {
            logger.debug("Method {} does not use JavaBean convention", methodName);
            rawPropertyName = methodName;
        }
        
        final Method targetMethod;
        try {
            targetMethod = target.getDeclaredMethod(methodName, mixInMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            logger.warn("Method {} could not be found for target {}", methodName, target);
            return;
        }
        
        final Optional<Method> setterMethod = getSetterMethod(target, rawPropertyName, mixInMethod);
        
        final Optional<Predicate> p = getMethodAnnotation(mixInMethod, Predicate.class);
        
        if (p.isPresent()) {
            final PropertyAnalysis<Predicate> predicate = new PropertyAnalysis<Predicate>(p.get(),
                    targetMethod, setterMethod.orElse(null));
            classAnalysis.addPredicateProperty(predicate);
        }
    }
    
    /**
     * Internal convenience method to retrieve a bean property setter method based on the associated
     * getter method. Assuming the JavaBean naming convention is applied, replacing the {@code get}
     * prefix with the {@code set} prefix should produce the right method name. The method signature
     * is based on the getter signature. 
     * @param target the target bean class.
     * @param rawPropertyName non-{@link Introspector#decapitalize(String) normalized} bean property
     *        accessor name. This assumes proper capitalization of the property name.
     * @param getterMethod setter method counterpart, used to derrive the correct setter signature.
     * @return bean property setter method if present; {@link Optional#empty()} if not present.
     */
    private Optional<Method> getSetterMethod(final Class<?> target, final String rawPropertyName,
            final Method getterMethod) {
        final String setterMethodName = PREFIX_SET + rawPropertyName;
        
        final Method setter;
        try {
            setter = target.getDeclaredMethod(setterMethodName, getterMethod.getReturnType());
        } catch (NoSuchMethodException e) {
            logger.debug("No setter with name {} found for target {}: {}",
                    setterMethodName, target, e.getMessage());
            return Optional.empty();
        }
        
        return Optional.of(setter);
    }
    
    /**
     * Internal convenience method to retrieve an annotation of a mix-in method.
     * @param method mix-in method to investigate.
     * @param annotationClass the annotation to retrieve.
     * @return the retrieved annotation from the method; {@link Optional#empty()} if not found.
     */
    private <T extends Annotation> Optional<T> getMethodAnnotation(final Method method,
            final Class<T> annotationClass) {
        if (!method.isAnnotationPresent(annotationClass)) {
            return Optional.empty();
        }
        
        final T annotation = method.getDeclaredAnnotation(annotationClass);
        return Optional.of(annotation);
    }
}
