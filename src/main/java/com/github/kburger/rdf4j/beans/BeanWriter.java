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

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.kburger.rdf4j.beans.annotation.Predicate;
import com.github.kburger.rdf4j.beans.annotation.Subject;
import com.github.kburger.rdf4j.beans.annotation.Type;
import com.github.kburger.rdf4j.beans.exception.BeanException;

/**
 * Serializes a java bean based on the rdf4j-beans annotations.
 */
public class BeanWriter {
    private static final Logger logger = LoggerFactory.getLogger(BeanWriter.class);
    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
    
    /**
     * Writes the {@code bean} using the {@code writer}. The subject of the triples is determined by
     * the {@code subject} argument.
     * @param writer {@link Writer} used to output the serialized bean.
     * @param analysis class analysis for the bean.
     * @param bean java bean to serialize.
     * @param subject triple subject for the java bean.
     * @param format one of the {@link RDFFormat} types.
     */
    public void write(final Writer writer, final ClassAnalysis analysis, final Object bean,
            final String subject, final RDFFormat format) {
        final Model model = new LinkedHashModel();
        
        writeInternal(model, analysis, bean, FACTORY.createIRI(subject));
        
        Rio.write(model, writer, format);
    }
    
    /**
     * Internal method for handling bean properties. This method is used to write the root bean
     * properties, and reused for handling nested beans. Concrete triple output is delegated to the
     * {@link #writeContent(PropertyAnalysis, Object, Model, IRI, IRI) writeContent} method.
     * @param model rdf4j model that holds the statements.
     * @param analysis class analysis for the bean.
     * @param bean java bean to serialize.
     * @param subject triple subject for the java bean.
     */
    private void writeInternal(final Model model, final ClassAnalysis analysis, final Object bean,
            final IRI subject) {
        final PropertyAnalysis<Type> type = analysis.getType();
        model.add(subject, RDF.TYPE, FACTORY.createIRI(type.getAnnotation().value()));
        
        for (final PropertyAnalysis<Predicate> property : analysis.getPredicates()) {
            final Predicate annotation = property.getAnnotation();
            
            final Object content;
            try {
                content = property.getGetter().invoke(bean);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                logger.warn("Could not invoke getter on {}: {}", bean, e.getMessage());
                // TODO add flag for strict writing
                continue;
            }
            
            final IRI predicate = FACTORY.createIRI(annotation.value());
            
            if (content instanceof Iterable) {
                for (final Object element : (Iterable<?>)content) {
                    writeContent(property, element, model, subject, predicate);
                }
            } else {
                writeContent(property, content, model, subject, predicate);
            }
        }
    }
    
    /**
     * Internal method for concrete serialization of properties into triples.
     * @param property java bean property analysis.
     * @param content bean property value.
     * @param model rdf4j model that holds the statements.
     * @param subject triple subject for this property.
     * @param predicate triple predicate for this property.
     */
    private void writeContent(final PropertyAnalysis<Predicate> property, final Object content,
            final Model model, final IRI subject, final IRI predicate) {
        if (property.getNested().isPresent()) {
            final ClassAnalysis nested = property.getNested().get();
            
            // TODO add check and logic for relative urls
            final PropertyAnalysis<Subject> subjectProperty = nested.getSubject();
            final Subject annotation = subjectProperty.getAnnotation();
            
            Object subjectValue;
            try {
                subjectValue = nested.getSubject().getGetter().invoke(content);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                // throw stuff if strict? ignore if lenient?
                logger.warn("Could not invoke subject getter on {}: {}", nested, e);
                throw new BeanException("Failed to invoke bean property getter method", e);
            }
            
            // check for relative url and create the absolute url if needed
            if (annotation.relative()) {
                subjectValue = subject.toString() + subjectValue.toString();
            }
            
            final IRI nestedSubject = FACTORY.createIRI(subjectValue.toString());
            
            model.add(subject, predicate, nestedSubject);
            
            writeInternal(model, nested, content, nestedSubject);
        } else if (property.getAnnotation().isLiteral()) {
            final IRI datatype = FACTORY.createIRI(property.getAnnotation().datatype());
            
            model.add(subject, predicate, FACTORY.createLiteral(content.toString(), datatype));
        } else {
            model.add(subject, predicate, FACTORY.createIRI(content.toString()));
        }
    }
}
