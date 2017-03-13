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

import com.github.kburger.rdf4j.beans.annotation.Predicate
import com.github.kburger.rdf4j.beans.annotation.Type

import spock.lang.Specification

class BeanAnalyzerSpec extends Specification {
    def beanAnalyzer = new BeanAnalyzer()
    
    def "cached classes should prevent re-analysis"() {
        when:
        beanAnalyzer.analyze(PropertyTestClass)
        then:
        beanAnalyzer.cache.size() == 1
        
        when:
        beanAnalyzer.analyze(PropertyTestClass)
        then:
        beanAnalyzer.cache.size() == 1
    }
    
    def "check for annotated property from field annotation"() {
        when:
        def analysis = beanAnalyzer.analyze(PropertyTestClass)
        
        then:
        analysis.predicates.size() == 1
        analysis.predicates[0].annotation.value() == EXAMPLE_PREDICATE
    }
    
    def "check for annotated property from method annotation"() {
        when:
        def analysis = beanAnalyzer.analyze(MethodTestClass)
        
        then:
        analysis.predicates.size() == 1
        analysis.predicates[0].annotation.value() == EXAMPLE_PREDICATE
    }
    
    def "check for annotated properties from both field and method annotation"() {
        when:
        def analysis = beanAnalyzer.analyze(PropertyAndMethodTestClass)
        
        then:
        analysis.predicates.size() == 2
        analysis.predicates[0].annotation.value() == EXAMPLE_PREDICATE
        analysis.predicates[1].annotation.value() == "test2"
    }
    
    def "check for class level type annotation"() {
        when:
        def analysis = beanAnalyzer.analyze(ClassTypeTestClass)
        
        then:
        analysis.type
        analysis.type.annotation.value() == EXAMPLE_TYPE
    }
    
    def "check for property level type annotation"() {
        when:
        def analysis = beanAnalyzer.analyze(PropertyTypeTestClass)
        
        then:
        analysis.type
        with (analysis.type.getter) {
            name == "getType"
            returnType == URI
            declaringClass == PropertyTypeTestClass
        }
    }
    
    def "check for method level type annotation"() {
        when:
        def analysis = beanAnalyzer.analyze(MethodTypeTestClass)
        
        then:
        analysis.type
        with (analysis.type.getter) {
            name == "getType"
            returnType == URI
            declaringClass == MethodTypeTestClass
        }
    }
    
    def "check for nested classes"() {
        when:
        def analysis = beanAnalyzer.analyze(WithNestedTestClass)
        
        then:
        beanAnalyzer.cache.size() == 2
        
        analysis.predicates.size() == 1
        
        with (analysis.predicates[0]) {
            annotation.value() == "parent-foo"
            nested.isPresent() == true
        }
        with (analysis.predicates[0].nested.get()) {
            predicates.size() == 1
            predicates[0].annotation.value() == "nested-foo"
        }
    }
}

class PropertyTestClass {
    @Predicate(EXAMPLE_PREDICATE) private String test
    
    public String getTest() { test }
}

class MethodTestClass {
    private String test
    
    @Predicate(EXAMPLE_PREDICATE)
    public String getTest() { test }
}

class PropertyAndMethodTestClass {
    @Predicate(EXAMPLE_PREDICATE) private String test
    
    private String test2
    
    public String getTest() { test }
    
    @Predicate("test2")
    public String getTest2() { test2 }
}

@Type(EXAMPLE_TYPE)
class ClassTypeTestClass {
}

class PropertyTypeTestClass {
    @Type("") private URI type
    
    public URI getType() { type }
}

class MethodTypeTestClass {
    private URI type
    
    @Type("")
    public URI getType() { type }
}

class NestedClass {
    @Predicate("nested-foo") private String foo
    
    public String getFoo() { foo }
}

class WithNestedTestClass {
    @Predicate("parent-foo") private NestedClass parentfoo;
    
    public NestedClass getParentfoo() { parentfoo }
}
