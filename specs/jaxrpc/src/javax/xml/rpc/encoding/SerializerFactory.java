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

import java.util.Iterator;

/**
 * The javax.xml.rpc.encoding.SerializerFactory is a factory of
 * the serializers. A SerializerFactory is registered with a
 * TypeMapping object as part of the TypeMappingRegistry.
 *
 * @version 1.0
 */
public interface SerializerFactory extends java.io.Serializable {

    /**
     * Returns a Serializer for the specified XML processing mechanism type.
     *
     * @param mechanismType - XML processing mechanism type [TBD: definition
     *              of valid constants]
     *
     * @return a <code>Serializer</code> for the specified XML processing
     *              mechanism type
     *
     * @throws javax.xml.rpc.JAXRPCException
     *              if <code>SerializerFactory</code> does not support the
     *              specified XML processing mechanism
     * @throws java.lang.IllegalArgumentException
     *              if an invalid mechanism type is specified
     */
    public Serializer getSerializerAs(String mechanismType);

    /**
     * Returns an Iterator over all XML processing mechanism types supported by
     * this <code>SerializerFactory</code>.
     *
     * @return an Iterator over the mechanism types (<Code>String</code>s?)
     */
    public Iterator getSupportedMechanismTypes();
}

