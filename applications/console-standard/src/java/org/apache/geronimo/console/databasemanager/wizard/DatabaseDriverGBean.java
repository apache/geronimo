/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.databasemanager.wizard;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.config.PersistentConfigurationList;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;

/**
 * replaceme
 *
 * @version $Rev: 368994 $ $Date: 2006-01-14 02:07:18 -0500 (Sat, 14 Jan 2006) $
 */
public class DatabaseDriverGBean implements DatabaseDriver {
    private final static Pattern PARAM_PATTERN = Pattern.compile("\\{.+?\\}");
    private String name;
    private String URLPrototype;
    private String driverClassName;
    private int defaultPort;
    private boolean XA;
    private Artifact RAR;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getURLPrototype() {
        return URLPrototype;
    }

    public void setURLPrototype(String URLPrototype) {
        this.URLPrototype = URLPrototype;
    }

    public String getDriverClassName() {
        return driverClassName;
    }

    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }

    public int getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(int defaultPort) {
        this.defaultPort = defaultPort;
    }

    public boolean isXA() {
        return XA;
    }

    public void setXA(boolean XA) {
        this.XA = XA;
    }

    public Artifact getRAR() {
        return RAR;
    }

    public void setRARName(String name) {
        RAR = Artifact.create(name);
    }

    public String[] getURLParameters() {
        Matcher m = PARAM_PATTERN.matcher(URLPrototype);
        List list = new ArrayList();
        while(m.find()) {
            list.add(URLPrototype.substring(m.start()+1, m.end()-1));
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Database Driver Info", DatabaseDriverGBean.class);
        infoFactory.addAttribute("name", String.class, true, true);
        infoFactory.addAttribute("URLPrototype", String.class, true, true);
        infoFactory.addAttribute("driverClassName", String.class, true, true);
        infoFactory.addAttribute("defaultPort", int.class, true, true);
        infoFactory.addAttribute("XA", boolean.class, true, true);
        infoFactory.addAttribute("RARName", String.class, true, true);
        infoFactory.addInterface(DatabaseDriver.class);

        infoFactory.setConstructor(new String[0]);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
