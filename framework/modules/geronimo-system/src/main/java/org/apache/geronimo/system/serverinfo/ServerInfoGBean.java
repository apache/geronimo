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

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.wrapper.AbstractServiceWrapper;
import org.osgi.framework.Bundle;

/**
 * @version $Rev:$ $Date:$
 */
@GBean
public class ServerInfoGBean  extends AbstractServiceWrapper<ServerInfo> implements ServerInfo {

    public ServerInfoGBean(@ParamSpecial(type = SpecialAttributeType.bundle)final Bundle bundle) {
       super(bundle, ServerInfo.class);
    }

    @Override
    public String getBaseDirectory() {
        return get().getBaseDirectory();
    }

   @Override
    public String getBuildDate() {
        return get().getBuildDate();
    }

    @Override
    public String getBuildTime() {
        return get().getBuildTime();
    }

    @Override
    public String getCopyright() {
        return get().getCopyright();
    }

    @Override
    public String getCurrentBaseDirectory() {
        return get().getCurrentBaseDirectory();
    }

    @Override
    public String[] getArgs() {
        return get().getArgs();
    }

    @Override
    public String getVersion() {
        return get().getVersion();
    }

    @Override
    public File resolve(String filename) {
        return get().resolve(filename);
    }

    @Override
    public URI resolve(URI uri) {
        return get().resolve(uri);
    }

    @Override
    public String resolvePath(String filename) {
        return get().resolvePath(filename);
    }

    @Override
    public File resolveServer(String filename) {
        return get().resolveServer(filename);
    }

    @Override
    public URI resolveServer(URI uri) {
        return get().resolveServer(uri);
    }

    @Override
    public String resolveServerPath(String filename) {
        return get().resolveServerPath(filename);
    }
}

