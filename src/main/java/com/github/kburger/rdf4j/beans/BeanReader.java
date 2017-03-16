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

import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.Value;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.util.Models;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.RDFParseException;
import org.eclipse.rdf4j.rio.Rio;

import com.github.kburger.rdf4j.beans.annotation.Predicate;
import com.github.kburger.rdf4j.beans.exception.BeanException;

/**
 * Deserializes an RDF document into a Java bean.
 */
public class BeanReader {
    private static final ValueFactory FACTORY = SimpleValueFactory.getInstance();
    
    private final Map<Class<?>, PropertyConverter<?>> converters = new HashMap<>();
    
    /**
     * Constructs a new BeanReader instance and sets up default {@link #converters}.
     */
    public BeanReader() {
        converters.put(String.class, Value::stringValue);
        converters.put(URI.class, v -> URI.create(v.stringValue()));
        converters.put(ZonedDateTime.class, v -> ZonedDateTime.parse(v.stringValue()));
    }
    
    /**
     * Registers a new {@link PropertyConverter} instance. Converters are used to translate RDF
     * properties from an RDF document into a Java object.
     * @param target conversion target type.
     * @param converter {@link PropertyConverter} instance.
     */
    public <T> void registerConverter(final Class<T> target, final PropertyConverter<T> converter) {
        converters.put(target, converter);
    }
    
    /**
     * Reads the RDF content from {@code reader} and produces a bean instance of type {@code clazz}.
     * @param reader {@link Reader} used to read the RDF content.
     * @param clazz target bean class.
     * @param analysis class analysis for the target {@code clazz}.
     * @param subject RDF triple subject of the bean in the RDF content.
     * @param format RDF format of the {@code reader} content.
     * @return a populated bean instance of type {@code clazz}.
     * @throws BeanException if anything goes wrong during reading.
     */
    public <T> T read(final Reader reader, final Class<T> clazz, final ClassAnalysis analysis,
            final String subject, final RDFFormat format) {
        final Model model;
        try {
            model = Rio.parse(reader, "", format);
        } catch (RDFParseException | IOException e) {
            throw new BeanException("Failed to read RDF source", e);
        }
        
        return readInternal(model, clazz, analysis, FACTORY.createIRI(subject));
    }
    
    /**
     * Internal method for reading bean properties. This method is used to read the root bean
     * properties, and reused for handling nested beans. 
     * @param model rdf4j model that holds the parsed rdf content.
     * @param clazz target bean class.
     * @param analysis class analysis for the target bean.
     * @param subject root bean RDF subject.
     * @return bean instance of the target [@code clazz}.
     * @throws BeanException if an RDF property conversion failed, or if a bean property setter
     *         method could not be invoked.
     */
    private <T> T readInternal(final Model model, final Class<T> clazz,
            final ClassAnalysis analysis, final IRI subject) {
        final T bean;
        try {
            bean = clazz.newInstance();
        } catch (IllegalAccessException | InstantiationException e) {
            throw new BeanException("Failed to create bean instance for class " + clazz, e);
        }
        
        for (final PropertyAnalysis<Predicate> property : analysis.getPredicates()) {
            final Predicate annotation = property.getAnnotation();
            final IRI predicate = FACTORY.createIRI(annotation.value());
            
            final Model triples = model.filter(subject, predicate, null);
            
            final Method setter = property.getSetter();
            // We can safely assume the BeanAnalyzer has picked a suitable setter method which
            // follows Java bean standards. Therefore we retrieve the single parameter of the setter
            // without any error checking.
            final Parameter setterArg = setter.getParameters()[0];
            
            final Object value;
            if (triples.isEmpty()) {
                continue;
            } else if (triples.size() > 1) {
                final ParameterizedType genericType =
                        (ParameterizedType)setterArg.getParameterizedType();
                final Class<?> collectionType = (Class<?>)genericType.getActualTypeArguments()[0];
                
                final Collection<Object> collection = createCollectionInstance(setterArg.getType());
                
                for (final Statement st : triples) {
                    final Object element =
                            readProperty(model, property, collectionType,st.getObject());
                    collection.add(element);
                }
                
                value = collection;
            } else {
                final Class<?> type = setterArg.getType();
                value = readProperty(model, property, type, Models.object(triples).get());
            }
            
            try {
                setter.invoke(bean, value);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                throw new BeanException("Failed to invoke bean property setter method", e);
            }
        }
        
        return bean;
    }
    
    /**
     * Internal method for concrete deserialization of a parsed RDF object into a Java type.
     * @param model rdf4j model that holds the parser rdf content.
     * @param property analyzed java bean property.
     * @param type target type of the java bean property value.
     * @param object parsed RDF triple object.
     * @return the deserialized triple object value.
     * @throws BeanException if the property type could not be converted into a Java type.
     */
    private Object readProperty(final Model model, final PropertyAnalysis<Predicate> property,
            final Class<?> type, final Value object) {
        final Object value;
        
        if (property.getNested().isPresent()) {
            final ClassAnalysis nested = property.getNested().get();
            
            value = readInternal(model, type, nested, (IRI)object);
        } else {
            if (converters.containsKey(type)) {
                value = converters.get(type).convert(object);
            } else {
                throw new BeanException("Unknown property type " + type);
            }
        }
        
        return value;
    }
    
    /**
     * Convenience method for creating Java Collections of the specified type.
     * @param type the target type for the Collection.
     * @return an empty Collection of the specified type.
     * @throws BeanException if the specified Collection type could not be created.
     */
    private Collection<Object> createCollectionInstance(final Class<?> type) {
        if (type.isAssignableFrom(List.class)) {
            return new ArrayList<>();
        } else if (type.isAssignableFrom(Set.class)) {
            return new HashSet<>();
        }
        
        throw new BeanException("Could not create unknown collection of type " + type);
    }
}
