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
        return delegate.resolveBasePath(filename);
    }

    @Override
    public String resolveServerPath(String filename) {
        return delegate.resolveHomePath(filename);
    }

    @Override
    public File resolve(String filename) {
        return delegate.resolveBase(filename);
    }

    @Override
    public File resolveServer(String filename) {
        return delegate.resolveHome(filename);
    }

    @Override
    public URI resolve(URI uri) {
        return delegate.resolveBase(uri);
    }

    @Override
    public URI resolveServer(URI uri) {
        return delegate.resolveHome(uri);
    }

    @Override
    public String getBaseDirectory() {
        return delegate.getBaseDirectory().getAbsolutePath();
    }

    @Override
    public String getCurrentBaseDirectory() {
        return delegate.getBaseDirectory().getAbsolutePath();
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
