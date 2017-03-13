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

import java.util.ArrayList;
import java.util.List;

import com.github.kburger.rdf4j.beans.annotation.Predicate;
import com.github.kburger.rdf4j.beans.annotation.Subject;
import com.github.kburger.rdf4j.beans.annotation.Type;

/**
 * Analysis result for a bean class. The analysis holds three types of properties:
 * 
 * <p>{@link Type}: one type indication per class.
 * 
 * <p>{@link Subject}: one subject indication per class. Only used for nested classes.
 * 
 * <p>{@link Predicate}: any number of predicates per class.
 */
public class ClassAnalysis {
    private PropertyAnalysis<Type> type;
    private PropertyAnalysis<Subject> subject;
    private final List<PropertyAnalysis<Predicate>> predicates;
    
    public ClassAnalysis() {
        predicates = new ArrayList<>();
    }
    
    public PropertyAnalysis<Subject> getSubject() {
        return subject;
    }
    
    public void setSubjectProperty(PropertyAnalysis<Subject> subject) {
        this.subject = subject;
    }
    
    public PropertyAnalysis<Type> getType() {
        return type;
    }
    
    public void setTypeProperty(PropertyAnalysis<Type> type) {
        this.type = type;
    }
    
    public List<PropertyAnalysis<Predicate>> getPredicates() {
        return predicates;
    }
    
    public void addPredicateProperty(PropertyAnalysis<Predicate> predicate) {
        predicates.add(predicate);
    }
}
