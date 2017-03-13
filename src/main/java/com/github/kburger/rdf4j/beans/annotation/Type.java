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
 * Indicates the {@code rdf:type} value of the annotated bean. The provided {@link #value() value}
 * should be an absolute URI. The {@code @Type} annotations can be put in three different positions:
 * 
 * <p>First, the bean class itself can be annotated. A single annotation on class level will
 * define the bean type for all bean instances. For example:
 * <pre>
 *    &#64;Type("http://example.com/Type")
 *    public class MyBeanClass {
 *        ...
 *    }
 * </pre>
 * 
 * <p>Second, a bean property can be annotated. A single annotation on property level can specify a
 * different bean type for each bean instance. For example:
 * <pre>
 *    public class MyBeanClass {
 *        &#64;Type("")
 *        private URI type;
 *        
 *        public URI getType() {
 *            return type;
 *        }
 *    }
 * </pre>
 * 
 * Third, a property getter can be annotated. As with property level annotations, a single
 * annotation on method level can specify a different bean type for each bean instance. For example:
 * <pre>
 *    public class MyBeanClass {
 *        private URI type;
 *        
 *        &#64;Type("")
 *        public URI getType() {
 *            return type;
 *        }
 *    }
 * </pre>
 * 
 * <p><b>Note:</b> For property and method level annotations, an empty {@link #value()} has to be
 * provided. This will be fixed in a later version.
 */
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Type {
    /**
     * The {@code rdf:type} indication for this bean.
     * @return an absolute URI.
     */
    String value();
}
