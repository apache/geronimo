/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.geronimo.system.serverinfo;

import java.io.File;
import java.net.URI;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;

/**
 * Supply a geronimo ServerInfo based on the karaf ServerInfo.
 * Note that geronimo's idea of home and base are backwards from karaf's idea.
 *
 * Geronimo: base is the install directory, home is where e.g. var is
 * Karaf: home is the install directory, base is where e.g. etc, system, are.
 *
 * Thus the karaf methods called may look wrong.
 *
 * @version $Rev:$ $Date:$
 */
@Component(immediate = true, metatype = true)
@Service
public class ServerInfoWrapper implements ServerInfo {

    @Reference(cardinality = ReferenceCardinality.MANDATORY_UNARY)
    private org.apache.karaf.info.ServerInfo delegate;

    public void setServerInfo(org.apache.karaf.info.ServerInfo delegate) {
        this.delegate = delegate;
    }

    public void unsetServerInfo(org.apache.karaf.info.ServerInfo delegate) {
        if (delegate == this.delegate) {
            this.delegate = null;
        }
    }

    @Override
    public String resolvePath(String filename) {
        return delegate.resolveHomePath(filename);
    }

    @Override
    public String resolveServerPath(String filename) {
        return delegate.resolveBasePath(filename);
    }

    @Override
    public File resolve(String filename) {
        return delegate.resolveHome(filename);
    }

    @Override
    public File resolveServer(String filename) {
        return delegate.resolveBase(filename);
    }

    @Override
    public URI resolve(URI uri) {
        return delegate.resolveHome(uri);
    }

    @Override
    public URI resolveServer(URI uri) {
        return delegate.resolveBase(uri);
    }

    @Override
    public String getBaseDirectory() {
        return delegate.getHomeDirectory().getAbsolutePath();
    }

    @Override
    public String getCurrentBaseDirectory() {
        return delegate.getHomeDirectory().getAbsolutePath();
    }

    @Override
    public String[] getArgs() {
        return delegate.getArgs();
    }

    @Override
    public String getVersion() {
        return null;
    }

    @Override
    public String getBuildDate() {
        return null;
    }

    @Override
    public String getBuildTime() {
        return null;
    }

    @Override
    public String getCopyright() {
        return null;
    }
}
