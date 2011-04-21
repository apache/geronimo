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

package org.apache.geronimo.myfaces.config.resource;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.myfaces.config.element.FacesConfig;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class ConfigurationResource implements Serializable {

    private String configurationResourcePath;

    private String jarFilePath;

    private transient FacesConfig facesConfig;

    public ConfigurationResource(String jarFilePath, String configurationResourcePath) {
        this.jarFilePath = jarFilePath;
        this.configurationResourcePath = configurationResourcePath.startsWith("/") ? configurationResourcePath : "/" + configurationResourcePath;
    }

    public String getConfigurationResourcePath() {
        return configurationResourcePath;
    }

    public String getJarFilePath() {
        return jarFilePath;
    }

    public void setJarFilePath(String jarFilePath) {
        this.jarFilePath = jarFilePath;
    }

    public void setConfigurationResourcePath(String configurationResourcePath) {
        this.configurationResourcePath = configurationResourcePath;
    }

    public URL getConfigurationResourceURL(Bundle bundle) throws MalformedURLException {
        if (jarFilePath == null) {
            return bundle.getEntry(configurationResourcePath);
        }
        return new URL("jar:" + bundle.getEntry(jarFilePath) + "!" + configurationResourcePath);
    }

    public void setFacesConfig(FacesConfig facesConfig) {
        this.facesConfig = facesConfig;
    }

    public FacesConfig getFacesConfig() {
        return facesConfig;
    }

    @Override
    public String toString() {
        return "ConfigurationResource [configurationResourcePath=" + configurationResourcePath + ", jarFilePath=" + jarFilePath + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((configurationResourcePath == null) ? 0 : configurationResourcePath.hashCode());
        result = prime * result + ((jarFilePath == null) ? 0 : jarFilePath.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ConfigurationResource other = (ConfigurationResource) obj;
        if (configurationResourcePath == null) {
            if (other.configurationResourcePath != null)
                return false;
        } else if (!configurationResourcePath.equals(other.configurationResourcePath))
            return false;
        if (jarFilePath == null) {
            if (other.jarFilePath != null)
                return false;
        } else if (!jarFilePath.equals(other.jarFilePath))
            return false;
        return true;
    }
}
