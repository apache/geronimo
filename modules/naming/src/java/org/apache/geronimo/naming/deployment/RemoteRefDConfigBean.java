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

import javax.enterprise.deploy.model.DDBean;

import org.apache.geronimo.xbeans.geronimo.naming.GerRemoteRefType;

/**
 *
 *
 * @version $Rev$ $Date$
 *
 * */
public class RemoteRefDConfigBean extends LocalRefDConfigBean {

    public RemoteRefDConfigBean(DDBean ddBean, GerRemoteRefType ref, String namePath) {
        super (ddBean, ref, namePath);
    }

    public String getServerName() {
        return ref.getServer();
    }

    public void setServerName(String serverName) {
        ref.setServer(serverName);
    }
}
