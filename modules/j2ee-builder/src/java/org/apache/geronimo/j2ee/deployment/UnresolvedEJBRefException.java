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
package org.apache.geronimo.j2ee.deployment;

import org.apache.geronimo.deployment.DeploymentException;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class UnresolvedEJBRefException extends DeploymentException {
    private final String refName;
    private final EJBRefInfo ejbRefInfo;

    public UnresolvedEJBRefException(String refName, EJBRefInfo ejbRefInfo, boolean foundMultiple) {
        super(createMessage(refName, ejbRefInfo, foundMultiple));
        this.refName = refName;
        this.ejbRefInfo = ejbRefInfo;
    }

    public String getRefName() {
        return refName;
    }

    public EJBRefInfo getEjbRefInfo() {
        return ejbRefInfo;
    }

    private static String createMessage(String refName, EJBRefInfo ejbRefInfo, boolean foundMultiple) {
        StringBuffer msg = new StringBuffer();
        if (foundMultiple) {
            msg.append("Two or more EJBs were found");
        } else {
            msg.append("Could not find an EJB");
        }
        msg.append(" for reference ").append(refName).append(" to a ");
        msg.append((ejbRefInfo.isLocal() ? "local " : "remote "));
        msg.append((ejbRefInfo.isSession() ? "session " : "entity "));

        msg.append(" bean that has the home interface ").append(ejbRefInfo.getHomeIntf());
        msg.append(" and the ").append(ejbRefInfo.isLocal() ? "local" : "remote");
        msg.append(" interface ").append(ejbRefInfo.getBeanIntf());

        return msg.toString();
    }
}
