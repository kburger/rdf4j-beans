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
        beanReader.read(new StringReader(source), Object, analysis, RDFFormat.TURTLE)
        
        then:
        thrown RuntimeException
    }
    
    def "test for exception handling on class instance creation"() {
        setup:
        def analysis = analyzer.analyze(Object)
        
        when:
        beanReader.read(new StringReader(""), List, analysis, RDFFormat.TURTLE)
        
        then:
        thrown RuntimeException
    }
    
    def "test for exception handling of setter invocation"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:value "test" .
                """
        def analysis = analyzer.analyze(SetterExceptionBean)
        
        when:
        beanReader.read(new StringReader(source), SetterExceptionBean, analysis, RDFFormat.TURTLE)
        
        then:
        thrown BeanException
    }
    
    def "test for handling of unknown bean property types"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:value "test" .
                """
        def analysis = analyzer.analyze(UnknownTypeBean)
        
        when:
        beanReader.read(new StringReader(source), UnknownTypeBean, analysis, RDFFormat.TURTLE)
        
        then:
        thrown RuntimeException
    }
    
    def "test for handling of unknown bean property collection types"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:value "a", "b", "c" .
                """
        def analysis = analyzer.analyze(UnknownCollectionTypeBean)
        
        when:
        beanReader.read(new StringReader(source), UnknownCollectionTypeBean, analysis, RDFFormat.TURTLE)
        
        then:
        thrown BeanException
    }
    
    def "test for deserialization of different collection types"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:value "a", "b", "c" .
                """
        
        when:
        def bean1 = beanReader.read(new StringReader(source), ListCollectionTypeBean, analyzer.analyze(ListCollectionTypeBean), RDFFormat.TURTLE)
        then:
        with (bean1) {
            value == ["a", "b", "c"]
        }
        
        when:
        def bean2 = beanReader.read(new StringReader(source), SetCollectionTypeBean, analyzer.analyze(SetCollectionTypeBean), RDFFormat.TURTLE)
        then:
        with (bean2) {
            value == ["a", "b", "c"] as Set
        }
    }
    
    def "test for deserialization of a string literal bean property"() {
        setup:
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:value "hello world!" .
                """
        def analysis = analyzer.analyze(StringExampleBean)
        
        when:
        def bean = beanReader.read(new StringReader(source), StringExampleBean, analysis, RDFFormat.TURTLE)
        
        then:
        with (bean) {
            value == "hello world!"
        }
    }
    
    def "test for deserialization of a list of string literal bean properties"() {
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:value "a", "b", "c" .
                """
        def analysis = analyzer.analyze(StringListExampleBean)
        
        when:
        def bean = beanReader.read(new StringReader(source), StringListExampleBean, analysis, RDFFormat.TURTLE)
        
        then:
        with (bean) {
            value == ["a", "b", "c"]
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
        def bean = beanReader.read(new StringReader(source), ZonedDateTimeExampleBean, analysis, RDFFormat.TURTLE)
        
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
        def analysis = analyzer.analyze(UriExampleBean)
        
        when:
        def bean = beanReader.read(new StringReader(source), UriExampleBean, analysis, RDFFormat.TURTLE)
        
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
        def bean = beanReader.read(new StringReader(source), UriListExampleBean, analysis, RDFFormat.TURTLE)
        
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
        def bean = beanReader.read(new StringReader(source), ParentExampleBean, analysis, RDFFormat.TURTLE)
        
        then:
        with (bean.nested) {
//            subject == "nested"
            value == "hello"
        }
    }
}

@Type(EXAMPLE_TYPE)
class SetterExceptionBean {
    @Predicate(value = VALUE_PREDICATE, isLiteral = true) private String value
    
    public String getValue() { value }
    public void setValue(String value) { throw new IllegalStateException() }
}

@Type(EXAMPLE_TYPE)
class UnknownTypeBean {
    @Predicate(value = VALUE_PREDICATE) private Object value
    
    public Object getValue() { value }
    public void setValue(Object value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class UnknownCollectionTypeBean {
    @Predicate(VALUE_PREDICATE) private Queue<String> value
    
    public Queue<String> getValue() { value }
    public void setValue(Queue<String> value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class ListCollectionTypeBean {
    @Predicate(VALUE_PREDICATE) private List<String> value
    
    public List<String> getValue() { value }
    public void setValue(List<String> value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class SetCollectionTypeBean {
    @Predicate(VALUE_PREDICATE) private Set<String> value
    
    public Set<String> getValue() { value }
    public void setValue(Set<String> value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class StringExampleBean {
    @Predicate(value = VALUE_PREDICATE, isLiteral = true)
    private String value
    
    public String getValue() { value }
    public void setValue(String value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class StringListExampleBean {
    @Predicate(value = VALUE_PREDICATE, isLiteral = true)
    private List<String> value
    
    public List<String> getValue() { value }
    public void setValue(List<String> value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class ZonedDateTimeExampleBean {
    @Predicate(value = VALUE_PREDICATE, isLiteral = true, datatype = "http://www.w3.org/2001/XMLSchema#dateTime")
    private ZonedDateTime value;
    
    public ZonedDateTime getValue() { value }
    public void setValue(ZonedDateTime value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class UriExampleBean {
    @Predicate(VALUE_PREDICATE)
    private URI value
    
    public URI getValue() { value }
    public void setValue(URI value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class UriListExampleBean {
    @Predicate(VALUE_PREDICATE)
    private List<URI> value
    
    public List<URI> getValue() { value }
    public void setValue(List<URI> value) { this.value = value }
}

@Type(EXAMPLE_TYPE)
class ParentExampleBean {
    @Predicate(NESTED_PREDICATE)
    private NestedExampleBean nested
    
    public NestedExampleBean getNested() { nested }
    public void setNested(NestedExampleBean nested) { this.nested = nested }
}

@Type(EXAMPLE_SUBTYPE)
class NestedExampleBean {
    @Subject(relative = true) private String subject
    @Predicate(value = VALUE_PREDICATE, isLiteral = true) private String value
    
    public String getSubject() { subject }
    
    public String getValue() { value }
    public void setValue(String value) { this.value = value }
}
