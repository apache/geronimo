/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.xml.registry.infomodel;

import java.util.Collection;
import javax.xml.registry.JAXRException;

/**
 * @version $Revision$ $Date$
 */
public interface ClassificationScheme extends RegistryEntry {
    public static final int VALUE_TYPE_UNIQUE = 0;
    public static final int VALUE_TYPE_EMBEDDED_PATH = 1;
    public static final int VALUE_TYPE_NON_UNIQUE = 2;

    void addChildConcpet(Concept concept) throws JAXRException;

    void addChildConcepts(Collection concepts) throws JAXRException;

    int getChildConceptCount() throws JAXRException;

    Collection getChildrenConcepts() throws JAXRException;

    Collection getDescendentConcepts() throws JAXRException;

    int getValueType() throws JAXRException;

    boolean isExternal() throws JAXRException;

    void removeChildConcpet(Concept concept) throws JAXRException;

    void removeChildConcepts(Collection concepts) throws JAXRException;

    void setValueType(int valueType) throws JAXRException;
}
