/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.naming.deployment;

import org.apache.xmlbeans.XmlObject;

/**
 * adapting wrapper for different xml objects generated for different schema incusions.
 *
 * @version $Revision: 1.2 $ $Date: 2004/03/10 09:59:08 $
 *
 * */
public interface RefAdapter {
    XmlObject getXmlObject();
    void setXmlObject(XmlObject xmlObject);

    String getRefName();
    void setRefName(String name);
    String getServerName();
    void setServerName(String serverName);
    String getKernelName();
    void setKernelName(String kernelName);
    String getTargetName();
    void setTargetName(String targetName);
    String getExternalUri();
    void setExternalUri(String externalURI);
}
