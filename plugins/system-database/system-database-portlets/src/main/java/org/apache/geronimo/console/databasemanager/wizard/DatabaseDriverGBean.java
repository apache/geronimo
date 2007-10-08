/**
 *
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
package org.apache.geronimo.console.databasemanager.wizard;

import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Implementation of DatabaseDriver that contains database driver information
 * contained in the console's deployment plan.
 *
 * @version $Rev$ $Date$
 */
public class DatabaseDriverGBean implements DatabaseDriver {
    private final static Pattern PARAM_PATTERN = Pattern.compile("\\{.+?\\}");
    private String name;
    private String URLPrototype;
    private String driverClassName;
    private int defaultPort;
    private boolean specific;
    private Artifact RAR;
    private Set<Artifact> dependencyFilters;

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

    public boolean isSpecific() {
        return specific;
    }

    public void setSpecific(boolean specific) {
        this.specific = specific;
    }

    public Artifact getRAR() {
        return RAR;
    }

    public void setDependencyFilterStrings(List<String> filterStrings) {
        dependencyFilters = new HashSet<Artifact>();
        for (String filterString: filterStrings) {
            Artifact filter = Artifact.createPartial(filterString);
            dependencyFilters.add(filter);
        }
    }

    public Set<Artifact> getDependencyFilters() {
        return dependencyFilters != null && !dependencyFilters.isEmpty()? dependencyFilters : null;
    }

    public void setRARName(String name) {
        RAR = Artifact.create(name);
    }

    public List<String> getURLParameters() {
        Matcher m = PARAM_PATTERN.matcher(URLPrototype);
        List<String> list = new ArrayList<String>();
        while(m.find()) {
            list.add(URLPrototype.substring(m.start()+1, m.end()-1));
        }
        return list;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic("Database Driver Info", DatabaseDriverGBean.class);
        infoFactory.addAttribute("name", String.class, true, true);
        infoFactory.addAttribute("URLPrototype", String.class, true, true);
        infoFactory.addAttribute("driverClassName", String.class, true, true);
        infoFactory.addAttribute("defaultPort", int.class, true, true);
        infoFactory.addAttribute("specific", boolean.class, true, true);
        infoFactory.addAttribute("RARName", String.class, true, true);
        infoFactory.addAttribute("dependencyFilterStrings", List.class, true, true);
        infoFactory.addAttribute("dependencyFilters", Set.class, false, false);
        infoFactory.addInterface(DatabaseDriver.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
