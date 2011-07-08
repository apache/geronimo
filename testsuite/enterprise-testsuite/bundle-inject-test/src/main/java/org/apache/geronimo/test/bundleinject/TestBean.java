/*
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
*/

package org.apache.geronimo.test.bundleinject;

import javax.annotation.Resource;
import javax.ejb.Singleton;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@Singleton
public class TestBean {

    @Resource
    private Bundle bundle;

    @Resource
    private BundleContext bundleContext;

    public String getOutput() {
        StringBuilder buf = new StringBuilder();
        
        buf.append("Bundle: " + bundle.getSymbolicName());
        buf.append(" ");
        buf.append("BundleContext: ").append((bundleContext != null) ? "ok" : "failed");
        
        return buf.toString();
    }
 
}
