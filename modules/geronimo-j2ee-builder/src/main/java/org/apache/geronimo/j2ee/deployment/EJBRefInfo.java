/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
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

import java.io.Serializable;

/**
 * @version $Revision$ $Date$
 */
public final class EJBRefInfo implements Serializable {
    private final boolean isLocal;
    private final boolean isSession;
    private final String homeIntf;
    private final String beanIntf;

    public EJBRefInfo(boolean local, boolean session, String homeIntf, String beanIntf) {
        assert homeIntf != null: "homeIntf is null";
        assert beanIntf != null: "beanIntf is null";
        isLocal = local;
        isSession = session;
        this.homeIntf = homeIntf;
        this.beanIntf = beanIntf;
    }

    public boolean isLocal() {
        return isLocal;
    }

    public boolean isSession() {
        return isSession;
    }

    public String getHomeIntf() {
        return homeIntf;
    }

    public String getBeanIntf() {
        return beanIntf;
    }

    public boolean equals(Object object) {
        if (!(object instanceof EJBRefInfo)) {
            return false;
        }

        // match isSession
        EJBRefInfo ejbRefInfo = (EJBRefInfo) object;
        return ejbRefInfo.isLocal == isLocal &&
                ejbRefInfo.isSession == isSession &&
                ejbRefInfo.homeIntf.equals(homeIntf) &&
                ejbRefInfo.beanIntf.equals(beanIntf);
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + (isLocal ? 1 : 0);
        result = 37 * result + (isSession ? 1 : 0);
        result = 37 * result + homeIntf.hashCode();
        result = 37 * result + beanIntf.hashCode();
        return result;
    }
}
