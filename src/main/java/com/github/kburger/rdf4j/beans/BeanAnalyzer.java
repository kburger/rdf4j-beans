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

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kburger.rdf4j.beans.annotation.Predicate;
import com.github.kburger.rdf4j.beans.annotation.Subject;
import com.github.kburger.rdf4j.beans.annotation.Type;
import com.github.kburger.rdf4j.beans.exception.BeanException;

/**
 * Analyzes Java beans that have been annotated with the rdf4j-beans annotations. The analysis
 * result is captured in a {@link ClassAnalysis} object. Common use will not interact with the
 * {@code BeanAnalyzer} directly, but rather through the {@link BeanMapper} class.
 */
public class BeanAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(BeanAnalyzer.class);
    
    /** Cached analysis. */
    private final Map<Class<?>, ClassAnalysis> cache;
    /** Mix-in specific analyzer. */
    private final MixInAnalyzer mixInAnalyzer;
    
    public BeanAnalyzer() {
        cache = new HashMap<>();
        mixInAnalyzer = new MixInAnalyzer();
    }
    
    public void registerMixIn(Class<?> target, Class<?> mixIn) {
        mixInAnalyzer.registerMixIn(target, mixIn);
    }
    
    /**
     * Analyzes the given {@link Class} for rdf4j-beans annotations.
     * @param clazz bean class to analyze.
     * @return the class analysis.
     */
    public ClassAnalysis analyze(final Class<?> clazz) {
        if (cache.containsKey(clazz)) {
            logger.debug("Class {} was found in cache, did not analyze again", clazz);
            return cache.get(clazz);
        }
        
        final BeanInfo bean;
        try {
            // FIXME On 1.8.0_45 passing Object.class to Introspector.getBeanInfo results in a
            // nullpointer exception. 
            bean = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException | NullPointerException e) {
            throw new BeanException("Failed to get bean info through introspection", e);
        }
        
        ClassAnalysis classAnalysis = new ClassAnalysis();
        
        if (!Object.class.equals(clazz.getSuperclass())) {
            classAnalysis = analyze(clazz.getSuperclass());
        }
        
        mixInAnalyzer.analyzeMixIn(clazz, classAnalysis);
        
        if (clazz.isAnnotationPresent(Type.class)) { 
            final PropertyAnalysis<Type> typeAnalysis =
                    new PropertyAnalysis<Type>(clazz.getAnnotation(Type.class), null, null);
            classAnalysis.setTypeProperty(typeAnalysis);
        }
        
        for (final PropertyDescriptor property : bean.getPropertyDescriptors()) {
            analyzeProperty(clazz, property, classAnalysis);
        }
        
        synchronized (cache) {
            cache.put(clazz, classAnalysis);
            logger.debug("Cached analysis for {}", clazz);
        }
        
        return classAnalysis;
    }
    
    /**
     * Analyzes a bean property. The presence of a {@link Predicate}, {@link Subject}, or {@link
     * Type} annotation on a field or method will include the property in the analysis result.
     * @param clazz bean class under analysis.
     * @param property bean property under analysis.
     * @param classAnalysis the analysis result object.
     */
    private void analyzeProperty(final Class<?> clazz, final PropertyDescriptor property,
            final ClassAnalysis classAnalysis) {
        final Field field;
        try {
            field = clazz.getDeclaredField(property.getName());
        } catch (NoSuchFieldException e) {
            logger.warn("Could not get property {} on class {}: {}",
                    property.getName(), clazz, e.getMessage());
            return;
        }
        
        final Optional<Predicate> p = getPropertyAnnotation(property, field, Predicate.class);
        final Optional<Subject> s = getPropertyAnnotation(property, field, Subject.class);
        final Optional<Type> t = getPropertyAnnotation(property, field, Type.class);
        
        if (p.isPresent()) {
            final PropertyAnalysis<Predicate> predicate = new PropertyAnalysis<>(p.get(),
                    property.getReadMethod(), property.getWriteMethod());
            classAnalysis.addPredicateProperty(predicate);
            
            final Class<?> fieldType = field.getType();
            
            if (isNested(fieldType)) {
                final ClassAnalysis nestedAnalysis = analyze(fieldType);
                predicate.setNested(nestedAnalysis);
            }
        } else if (s.isPresent()) {
            final PropertyAnalysis<Subject> subject = new PropertyAnalysis<>(s.get(),
                    property.getReadMethod(), property.getWriteMethod());
            
            classAnalysis.setSubjectProperty(subject);
        } else if (t.isPresent()) {
            final PropertyAnalysis<Type> type = new PropertyAnalysis<>(t.get(),
                    property.getReadMethod(), property.getWriteMethod());
            
            classAnalysis.setTypeProperty(type);
        }
    }
    
    /**
     * Convenience method to retreive an annotation of a bean property or method.
     * @param property bean property to investigate.
     * @param field concrete class field to retrieve the annotation from.
     * @param annotationClass the annotation to retrieve.
     * @return the retrieved annotation from field or method; {@link Optional#empty()} if not found.
     */
    private <T extends Annotation> Optional<T> getPropertyAnnotation(
            final PropertyDescriptor property, final Field field, final Class<T> annotationClass) {
        Optional<T> annotation = Optional.ofNullable(field.getAnnotation(annotationClass));
        
        if (!annotation.isPresent()) {
            final Method getter = property.getReadMethod();
            annotation = Optional.ofNullable(getter.getAnnotation(annotationClass));
        }
        
        return annotation;
    }
    
    /**
     * Check whether the given class is elligable as a nested bean.
     * @param clazz the class to check.
     * @return {@code true} if the given class is elligable as a nested bean; {@code false} if not.
     */
    private boolean isNested(final Class<?> clazz) {
        return !clazz.getName().matches("^java(x?)\\..+");
    }
}
