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
import javax.xml.registry.LifeCycleManager;

/**
 * @version $Revision$ $Date$
 */
public interface RegistryObject extends ExtensibleObject {
    void addAssociation(Association association) throws JAXRException;

    void addAssociations(Collection associations) throws JAXRException;

    void addClassification(Classification classification) throws JAXRException;

    void addClassifications(Collection classifications) throws JAXRException;

    void addExternalIdentifier(ExternalIdentifier externalIdentifier) throws JAXRException;

    void addExternalIdentifiers(Collection externalIdentifiers) throws JAXRException;

    void addExternalLink(ExternalLink externalLink) throws JAXRException;

    void addExternalLinks(Collection externalLinks) throws JAXRException;

    Collection getAssociatedObjects() throws JAXRException;

    Collection getAssociations() throws JAXRException;

    Collection getAuditTrail() throws JAXRException;

    Collection getClassifications() throws JAXRException;

    InternationalString getDescription() throws JAXRException;

    Collection getExternalIdentifier() throws JAXRException;

    Collection getExternalLinks() throws JAXRException;

    Key getKey() throws JAXRException;

    LifeCycleManager getLifeCycleManager() throws JAXRException;

    InternationalString getName() throws JAXRException;

    Concept getObjectType() throws JAXRException;

    Collection getRegistryPackage() throws JAXRException;

    Organization getSubmittingOrganization() throws JAXRException;

    void removeAssociation(Association association) throws JAXRException;

    void removeAssociations(Collection associations) throws JAXRException;

    void removeClassification(Classification classification) throws JAXRException;

    void removeClassifications(Collection classifications) throws JAXRException;

    void removeExternalIdentifier(ExternalIdentifier externalIdentifier) throws JAXRException;

    void removeExternalIdentifiers(Collection externalIdentifiers) throws JAXRException;

    void removeExternalLink(ExternalLink externalLink) throws JAXRException;

    void removeExternalLinks(Collection externalLinks) throws JAXRException;

    void setAssociations(Collection associations) throws JAXRException;

    void setClassifications(Collection classifications) throws JAXRException;

    void setExternalIdentifiers(Collection externalIdentifiers) throws JAXRException;

    void setExternalLinks(Collection externalLinks) throws JAXRException;

    void setKey(Key key) throws JAXRException;

    void setName(InternationalString name) throws JAXRException;

    String toXML() throws JAXRException;
}
