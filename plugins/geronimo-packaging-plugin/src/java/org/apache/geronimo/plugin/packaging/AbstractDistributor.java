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

package org.apache.geronimo.plugin.packaging;

import java.io.File;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * Base class for Distributors defining common attributes.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractDistributor {
    private String user;
    private String password;
    private String url;
    private File artifact;
    protected ObjectName storeName;

    public String getUser() {
        return user;
    }

    /**
     * Set the username used to connect to the server.
     *
     * @param user the username
     */
    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    /**
     * Set the password used to connect to the server.
     *
     * @param password the password
     */
    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    /**
     * Set the URL of the server.
     *
     * @param url the URL of the server
     */
    public void setUrl(String url) {
        this.url = url;
    }

    public File getArtifact() {
        return artifact;
    }

    /**
     * Set the artifact to distribute.
     *
     * @param artifact the artifact to distribute
     */
    public void setArtifact(File artifact) {
        this.artifact = artifact;
    }

    public String getStoreName() {
        return storeName.toString();
    }

    /**
     * Set the name of the ConfigurationStore in the server that the artifact
     * should be installed in. This allows for server's that have multiple
     * stores, although typical installation may only have one.
     *
     * @param storeName the name of the ConfigurationStore to distribute to
     */
    public void setStoreName(String storeName) {
        try {
            this.storeName = new ObjectName(storeName);
        } catch (MalformedObjectNameException e) {
            throw new IllegalArgumentException("Invalid storeName: " + storeName);
        }
    }

    public abstract void execute() throws Exception;
}
