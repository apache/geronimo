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

package org.apache.geronimo.web25.deployment.merge.webfragment;

import org.apache.openejb.jee.WebFragment;

/**
 * @version $Rev$ $Date$
 */
public class WebFragmentEntry {

    private String jarURL;

    //If the name element is defined in the web-fragment.xml, its value is the same with the webfragment's name. If not, a temporary unique name is assigned
    private String name;

    private WebFragment webFragment;

    private String webFragmentName;

    public WebFragmentEntry(String name, String webFragmentName, WebFragment webFragment, String jarURL) {
        this.name = name;
        this.jarURL = jarURL;
        this.webFragment = webFragment;
        this.webFragmentName = webFragmentName;
    }

    public String getJarURL() {
        return jarURL;
    }

    public String getName() {
        return name;
    }

    public WebFragment getWebFragment() {
        return webFragment;
    }

    public String getWebFragmentName() {
        return webFragmentName;
    }

    public void setJarURL(String jarURL) {
        this.jarURL = jarURL;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setWebFragment(WebFragment webFragment) {
        this.webFragment = webFragment;
    }

    public void setWebFragmentName(String webFragmentName) {
        this.webFragmentName = webFragmentName;
    }

}