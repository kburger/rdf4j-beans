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

import org.eclipse.rdf4j.rio.RDFFormat

import com.github.kburger.rdf4j.beans.annotation.Predicate
import com.github.kburger.rdf4j.beans.annotation.Subject
import com.github.kburger.rdf4j.beans.annotation.Type
import com.github.kburger.rdf4j.beans.exception.BeanException

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class BeanWriterSpec extends Specification {
    @Shared def analyzer = new BeanAnalyzer()
    def beanWriter = new BeanWriter()
    
    def "test for serialization of an IRI property"() {
        setup:
        def analysis = analyzer.analyze(UriValueBean)
        def writer = new StringWriter()
        def bean = new UriValueBean()
        bean.value = URI.create "http://example.com/foo"
        
        when:
        beanWriter.write(writer, analysis, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (writer.toString()) {
            contains """\
                <http://example.com/subject> a <http://example.com/Type> ;
                \t<http://example.com/value> <http://example.com/foo> .
                """.stripIndent()
        }
    }
    
    def "test for serialization of a literal property"() {
        setup:
        def analysis = analyzer.analyze(LiteralValueBean)
        def writer = new StringWriter()
        def bean = new LiteralValueBean()
        bean.value = "foo"
        
        when:
        beanWriter.write(writer, analysis, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (writer.toString()) {
            contains """\
                <http://example.com/subject> a <http://example.com/Type> ;
                \t<http://example.com/value> "foo" .
                """.stripIndent()
        }
    }
    
    def "test for serialization of a list of properties"() {
        setup:
        def analysis = analyzer.analyze(TestPropertyListBean)
        def writer = new StringWriter()
        def bean = new TestPropertyListBean()
        bean.values = ["one", "two", "three"]
        
        when:
        beanWriter.write(writer, analysis, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (writer.toString()) {
            contains """\
                    <http://example.com/subject> a <http://example.com/Type> ;
                    \t<http://example.com/value> "one" , "two" , "three" .
                    """.stripIndent()
        }
    }
    
    def "test for nested bean"() {
        setup:
        def analysis = analyzer.analyze(ParentTestBean)
        def writer = new StringWriter()
        def bean = new ParentTestBean()
        bean.nestedBean = new NestedTestBean()
        bean.nestedBean.subject = URI.create "http://example.com/nested"
        bean.nestedBean.value = "nested"
        
        when:
        beanWriter.write(writer, analysis, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (writer.toString()) {
            contains """\
                <http://example.com/subject> a <http://example.com/Type> ;
                \t<http://example.com/hasnested> <http://example.com/nested> .
                """.stripIndent()
            contains """\
                <http://example.com/nested> a <http://example.com/Subtype> ;
                \t<http://example.com/value> "nested" .
                """.stripIndent()
        }
    }
    
    def "test for serialization of a relative-subject nested bean"() {
        setup:
        def analysis = analyzer.analyze(RelativeSubjectParentBean)
        def writer = new StringWriter()
        def bean = new RelativeSubjectParentBean()
        bean.nestedBean = new RelativeSubjectNestedBean()
        bean.nestedBean.subject = "#nestedBean"
        bean.nestedBean.value = "nested"
        
        when:
        beanWriter.write(writer, analysis, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (writer.toString()) {
            contains """\
                    <http://example.com/subject> a <http://example.com/Type> ;
                    \t<http://example.com/hasnested> <http://example.com/subject#nestedBean> .
                    """.stripIndent()
            contains """\
                    <http://example.com/subject#nestedBean> a <http://example.com/Subtype> ;
                    \t<http://example.com/value> "nested" .
                    """.stripIndent()
        }
    }
    
    def "check exception handling when bean class differs from class analysis"() {
        setup:
        def analysis = analyzer.analyze(LiteralValueBean)
        def wrongBean = new UriValueBean()
        
        when:
        beanWriter.write(new StringWriter(), analysis, wrongBean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        noExceptionThrown()
    }
    
    def "check for exceptions thrown from predicate getter"() {
        setup:
        def analysis = analyzer.analyze(LiteralValueBean)
        def bean = Mock(LiteralValueBean)
        
        when:
        beanWriter.write(new StringWriter(), analysis, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        bean.getValue() >> { throw new IllegalStateException() }
        noExceptionThrown()
    }
    
    @Ignore
    def "check exception handling when nested bean class differs from class analysis"() {
        def analysis = analyzer.analyze(ParentTestBean)
        def parent = new ParentTestBean()
        parent.nestedBean = new LiteralValueBean()
        
        when:
        beanWriter.write(new StringWriter(), analysis, parent, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        noExceptionThrown()
    }
    
    def "check exception handling when nested subject getter is invoked"() {
        setup:
        def analysis = analyzer.analyze(ParentTestBean)
        def parent = new ParentTestBean()
        def nested = Mock(NestedTestBean)
        parent.nestedBean = nested
        
        when:
        beanWriter.write(new StringWriter(), analysis, parent, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        nested.getSubject() >> { throw new RuntimeException() }
        thrown BeanException
    }
    
    def "check exception handling when a nested field is null"() {
        setup:
        def analysis = analyzer.analyze(ParentTestBean)
        def bean = new ParentTestBean()
        
        when:
        beanWriter.write(new StringWriter(), analysis, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        notThrown NullPointerException
    }
}

@Type(EXAMPLE_TYPE)
class TestPropertyListBean {
    @Predicate(value = VALUE_PREDICATE, isLiteral = true) List<String> values
}

@Type(EXAMPLE_TYPE)
class ParentTestBean {
    @Predicate(NESTED_PREDICATE) NestedTestBean nestedBean
}

@Type(EXAMPLE_SUBTYPE)
class NestedTestBean {
    @Subject URI subject
    @Predicate(value = VALUE_PREDICATE, isLiteral = true) String value
}

@Type(EXAMPLE_TYPE)
class RelativeSubjectParentBean {
    @Predicate(NESTED_PREDICATE) RelativeSubjectNestedBean nestedBean
}

@Type(EXAMPLE_SUBTYPE)
class RelativeSubjectNestedBean {
    @Subject(relative = true) String subject
    @Predicate(value = VALUE_PREDICATE, isLiteral = true) String value
}