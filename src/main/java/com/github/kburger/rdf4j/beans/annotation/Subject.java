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
 * Indicates the RDF subject of the annotated bean. This annotation is used to indicate the subject
 * of a nested bean. The annotated property or method can provide either an absolute URI or a
 * relative URI, indicated by the {@link #relative()} property.
 * 
 * <p>Usage examples for property annotation:
 * <pre>
 *    public class AbsoluteExample {
 *        &#64;Subject
 *        private URI subject;
 *        
 *        public URI getSubject() {
 *            return subject;
 *        }
 *    }
 *    
 *    public class RelativeExample {
 *        &#64;Subject(relative = true)
 *        private String subject;
 *        
 *        public String getSubject() {
 *            return subject;
 *        }
 *    }
 * </pre>
 * 
 * <p>Usage examples for method annotation:
 * <pre>
 *    public class AbsoluteExample {
 *        private URI subject;
 *        
 *        &#64;Subject
 *        public URI getSubject() {
 *            return subject;
 *        }
 *    }
 *    
 *    public class RelativeExample {
 *        private String subject;
 *        
 *        &#64;Subject(relative = true)
 *        public String getSubject() {
 *            return subject;
 *        }
 *    }
 * </pre>
 * 
 * <p><b>Note:</b> Failure to provide a relative URI when using {@code relative=false} could result
 * in a runtime exception.
 */
@Target({ ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Subject {
    /**
     * Indicates whether the URI held by the annotated property or returned by the annotated
     * method is relative or absolute. Defaults to an absolute URI.
     * @return {@code true} for a relative URI; {@code false} for an absolute URI.
     */
    boolean relative() default false;
}
