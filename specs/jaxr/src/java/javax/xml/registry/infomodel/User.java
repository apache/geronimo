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

import java.net.URL;
import java.util.Collection;
import javax.xml.registry.JAXRException;

/**
 * @version $Revision$ $Date$
 */
public interface User extends RegistryObject {
    Collection getEmailAddresses() throws JAXRException;

    Organization getOrganization() throws JAXRException;

    PersonName getPersonName() throws JAXRException;

    Collection getPostalAddresses() throws JAXRException;

    Collection getTelephoneNumbers(String phoneType) throws JAXRException;

    String getType() throws JAXRException;

    URL getUrl() throws JAXRException;

    void setEmailAddresses(Collection addresses) throws JAXRException;

    void setPersonName(PersonName personName) throws JAXRException;

    void setPostalAddresses(Collection addresses) throws JAXRException;

    void setTelephoneNumbers(Collection phoneNumbers) throws JAXRException;

    void setType(String type) throws JAXRException;

    void setUrl(URL url) throws JAXRException;
}
