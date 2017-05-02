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
package com.github.kburger.rdf4j.beans

import static com.github.kburger.rdf4j.beans.Constants.*

import java.lang.annotation.Annotation

import com.github.kburger.rdf4j.beans.annotation.Predicate
import com.github.kburger.rdf4j.beans.annotation.Type
import com.google.common.eventbus.AnnotatedSubscriberFinder

import spock.lang.Specification

class MixInAnalyzerSpec extends Specification {
    def mixinAnalyzer = new MixInAnalyzer()
    
    def "non-mixin target is ignored"() {
        setup:
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(TargetBean, analysis)
        
        then:
        with (analysis) {
            predicates.size() == 0
        }
    }
    
    def "mixin annotations on an interface"() {
        setup:
        mixinAnalyzer.registerMixIn(TargetBean, MixInInterface)
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(TargetBean, analysis)
        
        then:
        with (analysis) {
            predicates.size() == 2
            checkPredicate(predicates, "hasValue", TargetBean, "getValue")
            checkPredicate(predicates, "hasFlag", TargetBean, "isFlag") 
        }
    }
    
    def "mixin annotations on an abstract class"() {
        setup:
        mixinAnalyzer.registerMixIn(TargetBean, MixInAbstractClass)
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(TargetBean, analysis)
        
        then:
        with (analysis) {
            predicates.size() == 2
            checkPredicate(predicates, "hasValue", TargetBean, "getValue")
            checkPredicate(predicates, "hasFlag", TargetBean, "isFlag")
        }
    }
    
    def "mixin annotations on an inherited abstract class"() {
        setup:
        mixinAnalyzer.registerMixIn(TargetBean, MixInAbstractInheritedClass)
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(TargetBean, analysis)
        
        then:
        with (analysis) {
            predicates.size() == 2
            checkPredicate(predicates, "hasValue", TargetBean, "getValue")
            checkPredicate(predicates, "hasFlag", TargetBean, "isFlag")
            
        }
    }
    
    def "mixin type on a non-typed target is added to the analysis"() {
        setup:
        mixinAnalyzer.registerMixIn(NonTypedBean, TypedMixIn)
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(NonTypedBean, analysis)
        
        then:
        with (analysis) {
            type.annotation.value() == EXAMPLE_TYPE
        }
    }
    
    def "mixin annotations on a bean with getter and setter methods"() {
        setup:
        mixinAnalyzer.registerMixIn(GetterAndSetterBean, GetterAndSetterMixIn)
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(GetterAndSetterBean, analysis)
        
        then:
        def metaMethods = GetterAndSetterBean.metaClass.methods
        with (analysis) {
            predicates.size() == 1
            checkPredicate(predicates, "hasValue", GetterAndSetterBean, "getValue")
            predicates[0].setter == metaMethods.find { it.name == "setValue" }.cachedMethod
        }
    }
    
    def "mixin for non-javabean convention target"() {
        setup:
        mixinAnalyzer.registerMixIn(NonConventionClass, NonConventionMixIn)
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(NonConventionClass, analysis)
        
        then:
        with (analysis) {
            predicates.size() == 1
            checkPredicate(predicates, "hasValue", NonConventionClass, "value")
        }
    }
    
    def "mixin that is disjoint from the target should be ignored"() {
        setup:
        mixinAnalyzer.registerMixIn(TargetBean, DisjointMixIn)
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(TargetBean, analysis)
        
        then:
        with (analysis) {
            predicates.size() == 0
        }
    }
    
    // convenience method for analysis predicate checking
    def checkPredicate(predicates, annotation, target, methodName) {
        predicates.find { it.annotation.value() == annotation }.getter == target.metaClass.methods.find { it.name == methodName }.cachedMethod
    }
}

//
class TargetBean {
    String value
    boolean flag
}
interface MixInInterface {
    @Predicate("hasValue") String getValue();
    @Predicate("hasFlag") boolean isFlag();
}
abstract class MixInAbstractClass {
    @Predicate("hasValue") abstract String getValue();
    @Predicate("hasFlag") abstract boolean isFlag();
}
abstract class MixInAbstractInheritedClass extends TargetBean {
    @Predicate("hasValue")
    @Override abstract String getValue();
    @Predicate("hasFlag")
    @Override abstract boolean isFlag();
}

//
class NonTypedBean {
}
@Type(EXAMPLE_TYPE)
interface TypedMixIn {
}

//
class GetterAndSetterBean {
    private String value
    
    public String getValue() { value }
    public void setValue(String value) { this.value = value }
}
interface GetterAndSetterMixIn {
    @Predicate("hasValue") String getValue();
}

//
class NonConventionClass {
    String value
    
    public String value() { value }
}
interface NonConventionMixIn {
    @Predicate("hasValue") String value();
}

// disjoint mixin class
interface DisjointMixIn {
    @Predicate("count") int getCount();
}