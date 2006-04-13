/**
 *
 * Copyright 2004, 2005 The Apache Software Foundation or its licensors, as applicable.
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

package org.apache.geronimo.console.webmanager;

import java.io.Serializable;

import javax.management.ObjectName;

import org.apache.geronimo.kernel.management.State;

public class ConnectorInfo implements Serializable {
    private String description;
    private String displayName;
    private String objectName;
    private String protocol;
    private int port;
    private int state;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getObjectName() {
        return objectName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getStateName() {
        return State.toString(state);
    }

}