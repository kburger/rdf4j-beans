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
import com.github.kburger.rdf4j.beans.annotation.Type

import spock.lang.Specification

class BeanMapperSpec extends Specification {
    def beanMapper = new BeanMapper()
    
    def "check for bean writer invocation"() {
        setup:
        def beanWriter = Spy(BeanWriter)
        beanMapper.writer = beanWriter
        def writer = new StringWriter()
        def bean = new TestBean()
        bean.value = URI.create "http://example.com/value/1"
        
        when:
        beanMapper.write(writer, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        then:
        1 * beanWriter.write(writer, _, bean, EXAMPLE_SUBJECT, RDFFormat.TURTLE)
        
        with (writer.toString()) {
            contains """\
                    <http://example.com> a <http://example.com/Type> ;
                    \t<http://example.com/predicate> <http://example.com/value/1> .
                    """.stripIndent()
        }
    }
    
    def "check for bean reader invocation"() {
        setup:
        def beanReader = Spy(BeanReader)
        beanMapper.reader = beanReader
        
        def source = """\
                @prefix ex: <http://example.com/> .
                ex:subject a ex:Type ;
                    ex:predicate <http://example.com/value/1> .
                """
        
        when:
        def bean = beanMapper.read(new StringReader(source), TestBean, RDFFormat.TURTLE)
        
        then:
        1 * beanReader.read(_, TestBean, _, RDFFormat.TURTLE)
        with (bean) {
            value == URI.create("http://example.com/value/1")
        }
    }
}

@Type(EXAMPLE_TYPE)
class TestBean {
    @Predicate(EXAMPLE_PREDICATE) private URI value
    
    public URI getValue() { value }
    public void setValue(URI value) { this.value = value }
}
