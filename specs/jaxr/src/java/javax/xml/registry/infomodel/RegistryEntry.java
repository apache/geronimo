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

import java.util.Date;
import javax.xml.registry.JAXRException;

/**
 * @version $Revision$ $Date$
 */
public interface RegistryEntry extends RegistryObject, Versionable {
    public static final int STABILITY_DYNAMIC = 0;
    public static final int STABILITY_DYNAMIC_COMPATIBLE = 1;
    public static final int STABILITY_STATIC = 2;

    public static final int STATUS_SUBMITTED = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_DEPRECATED = 2;
    public static final int STATUS_WITHDRAWN = 3;

    Date getExpiration() throws JAXRException;

    int getStability() throws JAXRException;

    int getStatus() throws JAXRException;

    void setExpiration(Date expiration) throws JAXRException;

    void setStability(int stability) throws JAXRException;
}
