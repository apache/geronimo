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
 * The javax.xml.rpc.encoding.DeserializerFactory is a factory of
 * deserializers. A DeserializerFactory is registered with a
 * TypeMapping instance as part of the TypeMappingRegistry.
 *
 * @version 1.0
 */
public interface DeserializerFactory extends java.io.Serializable {

    /**
     * Returns a Deserializer for the specified XML processing mechanism type.
     *
     * @param mechanismType XML processing mechanism type [TBD: definition of
     *              valid constants]
     *
     * @return a Deserializer for the specified XML processing mechanism type
     *
     * @throws javax.xml.rpc.JAXRPCException if DeserializerFactory does not
     *             support the specified XML processing mechanism
     */
    public Deserializer getDeserializerAs(String mechanismType);

    /**
     * Returns an <code>Iterator</code> over the list of all XML processing
     * mechanism types supported by this <code>DeserializerFactory</code>.
     *
     * @return an <code>Iterator</code> over the unique identifiers for the
     *              supported XML processing mechanism types (as
     *              <code>String</code>s?)
     */
    public java.util.Iterator getSupportedMechanismTypes();
}

