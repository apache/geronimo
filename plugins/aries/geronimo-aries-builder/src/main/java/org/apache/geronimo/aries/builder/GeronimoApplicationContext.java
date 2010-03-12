/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIESOR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.aries.builder;

import java.util.Set;

import org.apache.aries.application.management.ApplicationContext;
import org.apache.aries.application.management.AriesApplication;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleException;

public class GeronimoApplicationContext implements ApplicationContext {

    private ApplicationGBean applicationGBean;
    
    public GeronimoApplicationContext(ApplicationGBean applicationGBean) {
        this.applicationGBean = applicationGBean;
    }
    
    public AriesApplication getApplication() {
        return applicationGBean.getAriesApplication();
    }

    public Set<Bundle> getApplicationContent() {
        return applicationGBean.getApplicationContent();
    }

    public ApplicationState getApplicationState() {
        return applicationGBean.getApplicationState();
    }

    public void start() throws BundleException {
        applicationGBean.getBundle().start();
    }

    public void stop() throws BundleException {
        applicationGBean.getBundle().stop();
    }
  
    protected void uninstall() {
        applicationGBean.uninstall();
    }
}
