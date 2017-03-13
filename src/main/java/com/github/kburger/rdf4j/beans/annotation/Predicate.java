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
package com.github.kburger.rdf4j.beans.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the predicate for a bean property. The value of the bean property will be the object of
 * the triple. The annotation can also provide meta-information about the object through the
 * {@link #isLiteral()} and {@link #datatype()} properties.
 * 
 * <p>Usage examples:
 * <pre>
 *    public class Example {
 *        &#64;Predicate("http://example.com/foo")
 *        private URI foo;
 *        
 *        &#64;Predicate(value = "http://example.com/bar", isLiteral = true)
 *        private String bar;
 *        
 *        public URI getFoo() {
 *            return foo;
 *        }
 *        
 *        public String getBar() {
 *            return bar;
 *        }
 *    }
 * </pre>
 * 
 * <p>The {@link #datatype()} property can be used to define the exact datatype for a literal. For
 * example:
 * <pre>
 *    public class Example {
 *        &#64;Predicate(value = "http://example.com/created", isLiteral = true,
 *              dataType = "http://www.w3.org/2001/XMLSchema#dateTime")
 *        private ZonedDateTime created;
 *        
 *        public ZonedDateTime getCreated() {
 *            return created;
 *        }
 *    }
 * </pre>
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Predicate {
    /**
     * Defines the predicate URI.
     * @return the predicate URI.
     */
    String value();
    
    /**
     * Indicates whether the object is a literal or an IRI. Defaults to {@code false}.
     * @return {@code true} if the object is a literal; {@code false} if it is an IRI.
     */
    boolean isLiteral() default false;
    
    /**
     * Defines the RDF datatype of the triple's object. Defaults to {@code xsd:string}.
     * @return the triple's object datatype.
     */
    String datatype() default "http://www.w3.org/2001/XMLSchema#string";
}
