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
package javax.xml.registry;

import java.util.Collection;
import javax.xml.registry.infomodel.ClassificationScheme;
import javax.xml.registry.infomodel.Concept;
import javax.xml.registry.infomodel.Key;

/**
 * @version $Revision$ $Date$
 */
public interface BusinessQueryManager extends QueryManager {
    BulkResponse findAssociations(Collection findQualifiers, String sourceObjectId, String targetObjectId, Collection associationTypes) throws JAXRException;

    BulkResponse findCallerAssociations(Collection findQualifiers, Boolean confirmedByCaller, Boolean confirmedByOtherParty, Collection associationTypes) throws JAXRException;

    ClassificationScheme findClassificationSchemeByName(Collection findQualifiers, String namePatters) throws JAXRException;

    BulkResponse findClassificationSchemes(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalLinks) throws JAXRException;

    Concept findConceptByPath(String path) throws JAXRException;

    BulkResponse findConcepts(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalIdentifiers, Collection externalLinks) throws JAXRException;

    BulkResponse findOrganizations(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection specifications, Collection externalIdentifiers, Collection externalLinks) throws JAXRException;

    BulkResponse findRegistryPackages(Collection findQualifiers, Collection namePatterns, Collection classifications, Collection externalLinks) throws JAXRException;

    BulkResponse findServiceBindings(Key serviceKey, Collection findQualifiers, Collection classifications, Collection specificationa) throws JAXRException;

    BulkResponse findServices(Key orgKey, Collection findQualifiers, Collection namePattersn, Collection classifications, Collection specificationa) throws JAXRException;


}
