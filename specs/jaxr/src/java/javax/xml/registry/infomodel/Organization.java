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
public interface Organization extends RegistryObject {
    void addChildOrganization(Organization organization) throws JAXRException;

    void addChildOrganizations(Collection organizations) throws JAXRException;

    void addService(Service service) throws JAXRException;

    void addServices(Collection services) throws JAXRException;

    void addUser(User user) throws JAXRException;

    void addUsers(Collection users) throws JAXRException;

    int getChildOrganizationCount() throws JAXRException;

    Collection getChildOrganizations() throws JAXRException;

    Collection getDescendantOrganizations() throws JAXRException;

    Organization getParentOrganization() throws JAXRException;

    PostalAddress getPostalAddress() throws JAXRException;

    User getPrimaryContact() throws JAXRException;

    Organization getRootOrganization() throws JAXRException;

    Collection getServices() throws JAXRException;

    Collection getTelephoneNumbers(String phoneType) throws JAXRException;

    Collection getUsers() throws JAXRException;

    void removeChildOrganization(Organization organization) throws JAXRException;

    void removeChildOrganizations(Collection organizations) throws JAXRException;

    void removeService(Service service) throws JAXRException;

    void removeServices(Collection services) throws JAXRException;

    void removeUser(User user) throws JAXRException;

    void removeUsers(Collection users) throws JAXRException;

    void setPostalAddress(PostalAddress address) throws JAXRException;

    void setPrimaryContact(User primaryContact) throws JAXRException;

    void setTelephoneNumbers(Collection phoneNumbers) throws JAXRException;
}
