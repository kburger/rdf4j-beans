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
 * Bean mapper that ties the {@link BeanAnalyzer}, {@link BeanWriter}, and {@link BeanReader}
 * classes together. This class is the main usage entry point. Behaviour can be modified by setting
 * a custom {@code BeanWriter} and/or {@code BeanReader}.
 * 
 * <p>Usage example:
 * <pre>
 *    // bean usage example class
 *    &#64;Type("http://example.com/Type")
 *    public class StringExampleBean {
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
 *    mapper.write(new StringWriter(), new StringExampleBean(), "http://example.com/subject", RDFFormat.TURTLE);
 *    
 *    // stringwriter output now holds:
 *    &lt;http://example.com/subject&gt; a &lt;http://example.com/Type&gt; ;
 *        &lt;http://purl.org/dc/terms/creator&gt; &lt;...&gt; ;
 *        &lt;http://purl.org/dc/terms/title&gt; "..." .
 * </pre>
 * 
 * <p>Given the previous RDF output, it can be converted back into a Java bean using the following
 * snippet:
 * <pre>
 *    StringExampleBean bean = mapper.read(new StringReader(content), StringExampleBean.class, RDFFormat.TURTLE);
 * </pre>
 */
public class BeanMapper {
    private final BeanAnalyzer analyzer = new BeanAnalyzer();
    private BeanWriter beanWriter = new BeanWriter();
    private BeanReader beanReader = new BeanReader();
    
    public void setWriter(final BeanWriter writer) {
        this.beanWriter = writer;
    }
    
    public void setReader(final BeanReader reader) {
        this.beanReader = reader;
    }
    
    /**
     * Writes a Java bean, annotated with the rdf4j-beans annotations, to the {@code writer} using
     * the given {@code format}.
     * @param writer {@link Writer} used to output the serialized bean.
     * @param bean java bean to serialze.
     * @param subject triple subject for the java bean.
     * @param format determines the serialization format.
     * @throws BeanException if an error occurred during serialization.
     */
    public void write(final Writer writer, final Object bean, final String subject,
            final RDFFormat format) {
        final Class<?> clazz = bean.getClass();
        final ClassAnalysis analysis = analyzer.analyze(clazz);
        beanWriter.write(writer, analysis, bean, subject, format);
    }
    
    /**
     * Reads a Java bean from the RDF content provided by the {@code reader} in the given {@code
     * format}.
     * @param reader {@link Reader} instance that provides the RDF content.
     * @param clazz target bean class.
     * @param subject bean subject in the RDF content.
     * @param format RDF content format.
     * @return a populated bean instance of type {@code clazz}.
     * @throws BeanException if an error occured during deserialization.
     */
    public <T> T read(final Reader reader, final Class<T> clazz, final String subject,
            final RDFFormat format) {
        final ClassAnalysis analysis = analyzer.analyze(clazz);
        return beanReader.read(reader, clazz, analysis, subject, format);
    }
}
