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
package org.apache.geronimo.common;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class UnresolvedEJBRefException extends DeploymentException {
    private final String refName;
    private final boolean local;
    private final boolean session;
    private final String homeInterface;
    private final String beanInterface;

    public UnresolvedEJBRefException(String refName, boolean local, boolean session, String homeInterface,
                                     String beanInterface, boolean foundMultiple) {
        super(createMessage(refName, local, session, homeInterface, beanInterface, foundMultiple));
        this.refName = refName;
        this.local = local;
        this.session = session;
        this.homeInterface = homeInterface;
        this.beanInterface = beanInterface;
    }

    public String getRefName() {
        return refName;
    }

    public boolean isLocal() {
        return local;
    }

    public boolean isSession() {
        return session;
    }

    public String getHomeInterface() {
        return homeInterface;
    }

    public String getBeanInterface() {
        return beanInterface;
    }

    private static String createMessage(String refName, boolean local, boolean session, String homeInterface,
                                     String beanInterface, boolean foundMultiple) {
        StringBuffer msg = new StringBuffer();
        if (foundMultiple) {
            msg.append("Two or more EJBs were found");
        } else {
            msg.append("Could not find an EJB");
        }
        msg.append(" for reference ").append(refName).append(" to a ");
        msg.append((local ? "local " : "remote "));
        msg.append((session ? "session " : "entity "));

        msg.append(" bean that has the home interface ").append(homeInterface);
        msg.append(" and the ").append(local ? "local" : "remote");
        msg.append(" interface ").append(beanInterface);

        return msg.toString();
    }
}
