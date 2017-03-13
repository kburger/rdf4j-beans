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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;

/**
 * Analysis result for a bean property. The analysis holds the following metadata:
 * 
 * <p>Annotation: one of the rdf4j-beans annotations with which the bean property is annotated.
 * 
 * <p>Getter/setter: access methods to retrieve/define the bean property.
 * 
 * <p>Nested {@link ClassAnalysis}: if the bean property value is of a nested bean type, the nested
 * class-analysis holds the metadata of the nested bean.
 * 
 * @param <T> one of the 3 rdf4j-beans annotation types.
 */
public class PropertyAnalysis<T extends Annotation> {
    final private T annotation;
    final private Method getter;
    final private Method setter;
    private ClassAnalysis nested;
    
    public PropertyAnalysis(final T annotation, final Method getter, final Method setter) {
        this.annotation = annotation;
        this.getter = getter;
        this.setter = setter;
    }
    
    public T getAnnotation() {
        return annotation;
    }
    
    public Method getGetter() {
        return getter;
    }
    
    public Method getSetter() {
        return setter;
    }
    
    public Optional<ClassAnalysis> getNested() {
        return Optional.ofNullable(nested);
    }
    
    public void setNested(ClassAnalysis nested) {
        this.nested = nested;
    }
}
