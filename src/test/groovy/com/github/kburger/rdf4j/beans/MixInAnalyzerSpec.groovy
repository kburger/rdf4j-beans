package com.github.kburger.rdf4j.beans

import static com.github.kburger.rdf4j.beans.Constants.*

import com.github.kburger.rdf4j.beans.annotation.Predicate
import com.github.kburger.rdf4j.beans.annotation.Type

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
//            type.annotation.value() == EXAMPLE_TYPE
            predicates.size() == 2
            predicates[0].annotation.value() == "hasValue"
            predicates[1].annotation.value() == "hasFlag"
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
            predicates[0].annotation.value() == "hasValue"
            predicates[1].annotation.value() == "hasFlag"
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
            predicates[0].annotation.value() == "hasValue"
            predicates[1].annotation.value() == "hasFlag"
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
        with (analysis) {
            predicates.size() == 1
            predicates[0].annotation.value() == "hasValue"
        }
    }
    
    def "what happens to methods not following javabean convention?"() {
        setup:
        mixinAnalyzer.registerMixIn(NonConventionClass, NonConventionMixIn)
        def analysis = new ClassAnalysis()
        
        when:
        mixinAnalyzer.analyzeMixIn(NonConventionClass, analysis)
        
        then:
        with (analysis) {
            predicates.size() == 0
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