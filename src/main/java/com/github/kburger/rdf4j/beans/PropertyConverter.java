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

import org.eclipse.rdf4j.model.Value;

/**
 * Converts an RDF triple object into the target type.
 * @param <T> result type of the conversion.
 */
@FunctionalInterface
public interface PropertyConverter<T> {
    /**
     * Converts {@code value} into the equivalent instance of type {@code T}.
     * @param value RDF triple object.
     * @return the converted value for the triple object.
     */
    T convert(Value value);
}
