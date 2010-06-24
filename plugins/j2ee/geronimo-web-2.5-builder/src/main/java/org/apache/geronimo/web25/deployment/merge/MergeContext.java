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

package org.apache.geronimo.web25.deployment.merge;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.web25.deployment.merge.webfragment.WebFragmentEntry;
import org.apache.openejb.jee.WebFragment;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class MergeContext {

    private Map<String, Object> attributes = new ConcurrentHashMap<String, Object>();

    private Bundle bundle;

    private EARContext earContext;

    private WebFragmentEntry webFragmentEntry;

    public Object getAttribute(String attributeName) {
        return attributes.get(attributeName);
    }

    public Bundle getBundle() {
        return bundle;
    }

    public WebFragment getCurrentWebFragment() {
        return webFragmentEntry.getWebFragment();
    }

    public String getCurrentJarUrl() {
        return webFragmentEntry.getJarURL();
    }

    public EARContext getEarContext() {
        return earContext;
    }

    public void setAttribute(String attributeName, Object value) {
        attributes.put(attributeName, value);
    }

    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }

    public void setEarContext(EARContext earContext) {
        this.earContext = earContext;
    }

    public void setWebFragmentEntry(WebFragmentEntry webFragmentEntry) {
        this.webFragmentEntry = webFragmentEntry;
    }

    public boolean containsAttribute(String name) {
        return attributes.containsKey(name);
    }

    public void removeAttribute(String name) {
        attributes.remove(name);
    }

    public void clearup() {
        attributes.clear();
    }
}
