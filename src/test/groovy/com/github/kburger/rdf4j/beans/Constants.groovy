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

import com.github.kburger.rdf4j.beans.annotation.Predicate
import com.github.kburger.rdf4j.beans.annotation.Type

class Constants {
    public static final String EXAMPLE_SUBJECT = "http://example.com/subject"
    public static final String EXAMPLE_TYPE = "http://example.com/Type"
    public static final String EXAMPLE_SUBTYPE = "http://example.com/Subtype"
    public static final String EXAMPLE_PREDICATE = "http://example.com/predicate"
    public static final String VALUE_PREDICATE = "http://example.com/value"
    public static final String NESTED_PREDICATE = "http://example.com/hasnested"
    
    static final String EXAMPLE_RDF_CONTENT = """\
            @prefix ex: <http://example.com/> .
            ex:subject a ex:Type ;
                ex:value "test" .
            """
    
    static final String EXAMPLE_LIST_RDF_CONTENT = """\
            @prefix ex: <http://example.com/> .
            ex:subject a ex:Type ;
                ex:value "a", "b", "c" .
            """
}

@Type(Constants.EXAMPLE_TYPE)
public class LiteralValueBean {
    @Predicate(value = Constants.VALUE_PREDICATE, isLiteral = true)
    private String value
    
    public String getValue() { value }
    public void setValue(String value) { this.value = value }
}

@Type(Constants.EXAMPLE_TYPE)
public class UriValueBean {
    @Predicate(Constants.VALUE_PREDICATE)
    private URI value
    
    public URI getValue() { value }
    public void setValue(URI value) { this.value = value }
}