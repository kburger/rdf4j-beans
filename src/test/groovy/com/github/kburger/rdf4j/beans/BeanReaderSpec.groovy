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

import java.time.ZonedDateTime

import org.eclipse.rdf4j.model.vocabulary.XMLSchema
import org.eclipse.rdf4j.rio.RDFFormat

import com.github.kburger.rdf4j.beans.annotation.Predicate
import com.github.kburger.rdf4j.beans.annotation.Subject
import com.github.kburger.rdf4j.beans.annotation.Type
import com.github.kburger.rdf4j.beans.exception.BeanException

import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification

class BeanReaderSpec extends Specification {
    @Shared def analyzer = new BeanAnalyzer()
    def beanReader = new BeanReader()
    
    def "test converter registration"() {
        setup:
        def size = beanReader.converters.size()
        
        when:
        beanReader.registerConverter(Object, { it.toString() })
        
        then:
        with (beanReader) {
            converters.size() == size + 1
        }
    }
    
    def "test for exception handling of invalid input rdf"() {
        setup:
        def source = """\
                <foo> a :bar .
                """
        def analysis = analyzer.analyze(Object)
                
        when:
        beanReader.read(new StringReader(source), Object, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        thrown RuntimeException
    }
    
    def "test for exception handling on class instance creation"() {
        setup:
        def analysis = analyzer.analyze(Object)
        
        when:
        beanReader.read(new StringReader(""), List, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        thrown RuntimeException
    }
    
    def "test for exception handling of setter invocation"() {
        setup:
        def analysis = analyzer.analyze(SetterExceptionBean)
        
        when:
        beanReader.read(new StringReader(EXAMPLE_RDF_CONTENT), SetterExceptionBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        thrown BeanException
    }
    
    def "test for handling of unknown bean property types"() {
        setup:
        def analysis = analyzer.analyze(UnknownTypeBean)
        
        when:
        beanReader.read(new StringReader(EXAMPLE_RDF_CONTENT), UnknownTypeBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        thrown RuntimeException
    }
    
    def "test for handling of unknown bean property collection types"() {
        setup:
        def analysis = analyzer.analyze(UnknownCollectionTypeBean)
        
        when:
        beanReader.read(new StringReader(EXAMPLE_LIST_RDF_CONTENT), UnknownCollectionTypeBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        thrown BeanException
    }
    
    @Ignore
    def "test for handling incorrect rdf content"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                <http://wrong.example.com> a ex:Type .
                """
        def analysis = analyzer.analyze(LiteralValueBean)
        
        when:
        beanReader.read(new StringReader(source), LiteralValueBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        thrown BeanException
    }
    
    def "test for absent properties"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject ex:mandatory ex:foo .
                """
        def analysis = analyzer.analyze(AbsentPropertiesBean)
        
        when:
        def bean = beanReader.read(new StringReader(source), AbsentPropertiesBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (bean) {
            mandatory == URI.create("http://example.com/foo")
            optional == null
        }
    }
    
    def "test for deserialization of a string literal bean property"() {
        setup:
        def analysis = analyzer.analyze(LiteralValueBean)
        
        when:
        def bean = beanReader.read(new StringReader(EXAMPLE_RDF_CONTENT), LiteralValueBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (bean) {
            value == "test"
        }
    }
    
    def "test for deserialization of a list of string literal bean properties"() {
        def analysis = analyzer.analyze(StringListExampleBean)
        
        when:
        def bean = beanReader.read(new StringReader(EXAMPLE_LIST_RDF_CONTENT), StringListExampleBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (bean) {
            value == ["a", "b", "c"]
        }
    }
    
    def "test for deserialization of different collection types"() {
        when:
        def bean1 = beanReader.read(new StringReader(EXAMPLE_LIST_RDF_CONTENT), ListCollectionTypeBean, analyzer.analyze(ListCollectionTypeBean), EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        then:
        with (bean1) {
            value == ["a", "b", "c"]
        }
        
        when:
        def bean2 = beanReader.read(new StringReader(EXAMPLE_LIST_RDF_CONTENT), SetCollectionTypeBean, analyzer.analyze(SetCollectionTypeBean), EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        then:
        with (bean2) {
            value == ["a", "b", "c"] as Set
        }
    }
    
    def "test for deserialization of a datetime literal bean property"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                @prefix xsd: <${XMLSchema.NAMESPACE}> .
                ex:subject a ex:Type ;
                    ex:value "2017-01-01T12:00:00+01:00"^^xsd:dateTime .
                """
        def analysis = analyzer.analyze(ZonedDateTimeExampleBean)
        
        when:
        def bean = beanReader.read(new StringReader(source), ZonedDateTimeExampleBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (bean) {
            value == ZonedDateTime.parse("2017-01-01T12:00:00+01:00")
        }
    }
    
    def "test for deserialization of a iri bean property"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:value ex:foo .
                """
        def analysis = analyzer.analyze(UriValueBean)
        
        when:
        def bean = beanReader.read(new StringReader(source), UriValueBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (bean) {
            value == URI.create("http://example.com/foo")
        }
    }
    
    def "test for deserialization of a list of iri bean properties"() {
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:value ex:1, ex:2, ex:3 .
                """
        def analysis = analyzer.analyze(UriListExampleBean)
        
        when:
        def bean = beanReader.read(new StringReader(source), UriListExampleBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (bean) {
            value == [URI.create("http://example.com/1"), URI.create("http://example.com/2"), URI.create("http://example.com/3")]
        }
    }
    
    def "test for deserialization of a nested bean property"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:hasnested <http://example.com/nested> .
                
                <http://example.com/nested> a ex:Subtype ;
                    ex:value "hello" .
                """
        def analysis = analyzer.analyze(ParentExampleBean)
        
        when:
        def bean = beanReader.read(new StringReader(source), ParentExampleBean, analysis, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        with (bean.nested) {
//            subject == "nested"
            value == "hello"
        }
    }
}

@Type(EXAMPLE_TYPE)
class SetterExceptionBean {
    @Predicate(value = VALUE_PREDICATE, isLiteral = true) String value
    
    public void setValue(String value) { throw new IllegalStateException() }
}

@Type(EXAMPLE_TYPE)
class UnknownTypeBean {
    @Predicate(value = VALUE_PREDICATE) Object value
}

@Type(EXAMPLE_TYPE)
class UnknownCollectionTypeBean {
    @Predicate(VALUE_PREDICATE) Queue<String> value
}

class AbsentPropertiesBean {
    @Predicate("http://example.com/mandatory") URI mandatory
    @Predicate("http://example.com/optional") URI optional
}

@Type(EXAMPLE_TYPE)
class ListCollectionTypeBean {
    @Predicate(VALUE_PREDICATE) List<String> value
}

@Type(EXAMPLE_TYPE)
class SetCollectionTypeBean {
    @Predicate(VALUE_PREDICATE) Set<String> value
}

@Type(EXAMPLE_TYPE)
class StringListExampleBean {
    @Predicate(value = VALUE_PREDICATE, isLiteral = true) List<String> value
}

@Type(EXAMPLE_TYPE)
class ZonedDateTimeExampleBean {
    @Predicate(value = VALUE_PREDICATE, isLiteral = true, datatype = "http://www.w3.org/2001/XMLSchema#dateTime") ZonedDateTime value;
}

@Type(EXAMPLE_TYPE)
class UriListExampleBean {
    @Predicate(VALUE_PREDICATE) List<URI> value
}

@Type(EXAMPLE_TYPE)
class ParentExampleBean {
    @Predicate(NESTED_PREDICATE) NestedExampleBean nested
}

@Type(EXAMPLE_SUBTYPE)
class NestedExampleBean {
    @Subject(relative = true) String subject
    @Predicate(value = VALUE_PREDICATE, isLiteral = true) String value
}
