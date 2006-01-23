/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.naming.deployment.jsr88;

import java.io.Serializable;

/**
 * Holds the elements that make up an ObjectName.  This class exists
 * so that the bundle of elements can be get, set, and edited together
 * separate from any other elements that may be on the parent.
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public class ObjectNameGroup implements Serializable {
    private String application;
    private String domain;
    private String module;
    private String name;
    private String server;
    private String type;

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean empty() {
        return (application == null || application.trim().equals("")) &&
                (domain == null || domain.trim().equals("")) &&
                (module == null || module.trim().equals("")) &&
                (name == null || name.trim().equals("")) &&
                (server == null || server.trim().equals("")) &&
                (type == null || type.trim().equals(""));
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ObjectNameGroup group = (ObjectNameGroup) o;

        if (application != null ? !application.equals(group.application) : group.application != null) return false;
        if (domain != null ? !domain.equals(group.domain) : group.domain != null) return false;
        if (module != null ? !module.equals(group.module) : group.module != null) return false;
        if (name != null ? !name.equals(group.name) : group.name != null) return false;
        if (server != null ? !server.equals(group.server) : group.server != null) return false;
        if (type != null ? !type.equals(group.type) : group.type != null) return false;

        return true;
    }

    public int hashCode() {
        int result;
        result = (application != null ? application.hashCode() : 0);
        result = 29 * result + (domain != null ? domain.hashCode() : 0);
        result = 29 * result + (module != null ? module.hashCode() : 0);
        result = 29 * result + (name != null ? name.hashCode() : 0);
        result = 29 * result + (server != null ? server.hashCode() : 0);
        result = 29 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
