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

import java.io.Reader;
import java.io.Writer;

import org.eclipse.rdf4j.rio.RDFFormat;

/**
 * Bean mapper that ties the {@link BeanAnalyzer} and {@link BeanWriter} classes together. This
 * class is the main usage entry point. Behaviour can be modified by setting a custom {@code
 * BeanWriter}.
 * 
 * <p>Usage example:
 * <pre>
 *    // bean usage example class
 *    &#64;Type("http://example.com/Type")
 *    public class ExampleBean {
 *        &#64;Predicate("http://purl.org/dc/terms/creator")
 *        private URI creator;
 *        &#64;Predicate(value = "http://purl.org/dc/terms/title", isLiteral = true)
 *        private String title;
 *        
 *        public URI getCreator() {
 *            return creator;
 *        }
 *        
 *        public String getTitle() {
 *            return title;
 *        }
 *    }
 *    
 *    // mapper usage example
 *    BeanMapper mapper = new BeanMapper();
 *    mapper.write(new StringWriter(), new ExampleBean(), "http://example.com/subject", RDFFormat.TURTLE);
 *    
 *    // stringwriter output now holds:
 *    &lt;http://example.com/subject&gt; a &lt;http://example.com/Type&gt; ;
 *        &lt;http://purl.org/dc/terms/creator&gt; &lt;...&gt; ;
 *        &lt;http://purl.org/dc/terms/title&gt; "..." .
 * </pre>
 */
public class BeanMapper {
    private final BeanAnalyzer analyzer = new BeanAnalyzer();
    private BeanWriter beanWriter = new BeanWriter();
    private BeanReader beanReader = new BeanReader();
    
    public void setWriter(BeanWriter writer) {
        this.beanWriter = writer;
    }
    
    public void setReader(BeanReader reader) {
        this.beanReader = reader;
    }
    
    /**
     * Writes a java bean, annotated with the rdf4j-beans annotations, to the writer using the
     * given format.
     * @param writer {@link Writer} used to output the serialized bean.
     * @param bean java bean to serialze.
     * @param subject triple subject for the java bean.
     * @param format determines the serialization format.
     */
    public void write(Writer writer, Object bean, String subject, RDFFormat format) {
        final Class<?> clazz = bean.getClass();
        final ClassAnalysis analysis = analyzer.analyze(clazz);
        beanWriter.write(writer, analysis, bean, subject, format);
    }
    
    public <T> T read(Reader reader, Class<T> clazz, RDFFormat format) {
        final ClassAnalysis analysis = analyzer.analyze(clazz);
        return beanReader.read(reader, clazz, analysis, format);
    }
}
