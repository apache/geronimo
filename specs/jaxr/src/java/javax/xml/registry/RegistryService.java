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

import javax.xml.registry.infomodel.ClassificationScheme;

/**
 * @version $Revision$ $Date$
 */
public interface RegistryService {
    BulkResponse getBulkResponse(String requestId) throws JAXRException, InvalidRequestException;

    BusinessLifeCycleManager getBusinessLifeCycleManager() throws JAXRException;

    BusinessQueryManager getBusinessQueryManager() throws JAXRException;

    CapabilityProfile getCapabilityProfile() throws JAXRException;

    DeclarativeQueryManager getDeclarativeQueryManager() throws JAXRException, UnsupportedCapabilityException;

    ClassificationScheme getDefaultPostalScheme() throws JAXRException;

    String makeRegistrySpecificRequest(String request) throws JAXRException;
}
