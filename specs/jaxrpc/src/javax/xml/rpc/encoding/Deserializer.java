/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.xml.rpc.encoding;

/**
 * The javax.xml.rpc.encoding.Deserializer interface defines a
 * base interface for deserializers. A Deserializer converts
 * an XML representation to a Java object using a specific XML
 * processing mechanism and based on the specified type
 * mapping and encoding style.
 *
 * @version 1.0
 */
public interface Deserializer extends java.io.Serializable {

    /**
     * Gets the type of the XML processing mechanism and
     * representation used by this Deserializer.
     *
     * @return XML processing mechanism type
     */
    public String getMechanismType();
}

