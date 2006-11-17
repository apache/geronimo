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
package org.apache.geronimo.console.gbean;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * A GBean that implements ContextForward.  This way any Geronimo module can
 * add forwards to the console.
 *
 * @version $Rev$ $Date$
 */
public class ContextForwardGBean implements ContextForward {
    private String portalPathPrefix;
    private String portletContextPath;
    private String portletServletPath;

    public ContextForwardGBean(String portalPathPrefix, String portletContextPath, String portletServletPath) {
        this.portalPathPrefix = portalPathPrefix;
        this.portletContextPath = portletContextPath;
        this.portletServletPath = portletServletPath;
    }

    public String getPortalPathPrefix() {
        return portalPathPrefix;
    }

    public String getPortletContextPath() {
        return portletContextPath;
    }

    public String getPortletServletPath() {
        return portletServletPath;
    }

    public static final GBeanInfo GBEAN_INFO;
    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ContextForwardGBean.class);
        infoFactory.addAttribute("portalPathPrefix", String.class, true, true);
        infoFactory.addAttribute("portletContextPath", String.class, true, true);
        infoFactory.addAttribute("portletServletPath", String.class, true, true);
        infoFactory.addInterface(ContextForward.class);
        infoFactory.setConstructor(new String[]{"portalPathPrefix","portletContextPath","portletServletPath"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
