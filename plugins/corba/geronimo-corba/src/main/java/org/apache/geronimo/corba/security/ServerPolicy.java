/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.security;

import java.io.Serializable;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;

import org.apache.geronimo.corba.security.config.tss.TSSConfig;


/**
 * @version $Rev: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class ServerPolicy extends LocalObject implements Policy {

    private final TSSConfig TSSConfig;
    private final ClassLoader classloader;

    public ServerPolicy(Config config) {
        this.TSSConfig = config.getTSSConfig();
        this.classloader = config.getClassloader();
    }

    protected ServerPolicy(TSSConfig config, ClassLoader classLoader) {
         this.TSSConfig = config;
         this.classloader = classLoader;
    }

    public TSSConfig getConfig() {
        return TSSConfig;
    }

    public ClassLoader getClassloader() {
        return classloader;
    }

    public int policy_type() {
        return ServerPolicyFactory.POLICY_TYPE;
    }

    public void destroy() {
    }

    public Policy copy() {
        return new ServerPolicy(TSSConfig, classloader);
    }

    public static class Config implements Serializable {
        private final TSSConfig TSSConfig;
        private final transient ClassLoader classloader;

        public Config(TSSConfig TSSConfig, ClassLoader classloader) {
            this.TSSConfig = TSSConfig;
            this.classloader = classloader;
        }

        public final TSSConfig getTSSConfig() {
            return TSSConfig;
        }

        public final ClassLoader getClassloader() {
            return classloader;
        }
    }
}
