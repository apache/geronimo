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
package org.apache.geronimo.console.securitymanager.realm;

import java.io.Serializable;
import java.io.InputStream;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Comparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Describes an available login module, including how to create and configure it.
 * Reads the list of available login modules from a properties file on the class path.
 *
 * @version $Rev$ $Date$
 */
public class MasterLoginModuleInfo implements Serializable {
    private static final Logger log = LoggerFactory.getLogger(MasterLoginModuleInfo.class);
    private static MasterLoginModuleInfo[] allModules;
    private String name;
    private String className;
    private boolean testable = true;
    private OptionInfo[] options = new OptionInfo[0];

    private MasterLoginModuleInfo(String name, String className) {
        this.name = name;
        this.className = className;
    }

    public OptionInfo[] getOptions() {
        return options;
    }

    public Map getOptionMap() {
        Map map = new HashMap();
        for (int i = 0; i < options.length; i++) {
            OptionInfo info = options[i];
            map.put(info.getName(), info);
        }
        return map;
    }

    public String getName() {
        return name;
    }

    public String getClassName() {
        return className;
    }

    public boolean isTestable() {
        return testable;
    }

    private void setTestable(boolean testable) {
        this.testable = testable;
    }

    private void setOptions(OptionInfo[] options) {
        this.options = options;
    }

    public static MasterLoginModuleInfo[] getAllModules() {
        if(allModules == null) {
            allModules = loadModules();
        }
        return allModules;
    }

    private static MasterLoginModuleInfo[] loadModules() {
        List list = new ArrayList();
        Map map = new HashMap(), fieldMap = new HashMap();
        InputStream in = MasterLoginModuleInfo.class.getResourceAsStream("/login-modules.properties");
        if(in == null) {
            log.error("Unable to locate login module properties file");
            return null;
        }
        Properties props = new Properties();
        try {
            props.load(in);
        } catch (IOException e) {
            log.error("Unable to read login module properties file", e);
        } finally {
            try {
                in.close();
            } catch (java.io.IOException ignored) {
                // ignore
            }
        }
        for (Iterator it = props.keySet().iterator(); it.hasNext();) {
            String key = (String) it.next();
            if(key.startsWith("module.")) {
                String name = key.substring(7, key.indexOf('.', 7));
                MasterLoginModuleInfo info = (MasterLoginModuleInfo) map.get(name);
                if(info == null) {
                    info = new MasterLoginModuleInfo(props.getProperty("module."+name+".name"),
                            props.getProperty("module."+name+".class"));
                    String test = props.getProperty("module."+name+".testable");
                    if(test != null) {
                        info.setTestable(new Boolean(test.trim()).booleanValue());
                    }
                    map.put(name, info);
                    list.add(info);
                }
                String prefix = "module."+name+".field.";
                if(key.startsWith(prefix)) {
                    String fieldName = key.substring(prefix.length(), key.indexOf('.', prefix.length()));
                    List fields = (List) fieldMap.get(name);
                    if(fields == null) {
                        fields = new ArrayList();
                        fieldMap.put(name, fields);
                    }
                    OptionInfo option = null;
                    for (int i = 0; i < fields.size(); i++) {
                        OptionInfo opt = (OptionInfo) fields.get(i);
                        if(opt.getName().equals(fieldName)) {
                            option = opt;
                            break;
                        }
                    }
                    if(option == null) {
                        option = new OptionInfo(fieldName, props.getProperty(prefix+fieldName+".displayName"),
                                props.getProperty(prefix+fieldName+".description"));
                        String test = props.getProperty(prefix+fieldName+".password");
                        if(test != null) {
                            option.setPassword(true);
                        }
                        test = props.getProperty(prefix+fieldName+".length");
                        if(test != null) {
                            option.setLength(Integer.parseInt(test.trim()));
                        }
                        test = props.getProperty(prefix+fieldName+".displayOrder");
                        if(test != null) {
                            option.setDisplayOrder(Integer.parseInt(test.trim()));
                        }
                        test = props.getProperty(prefix+fieldName+".blankAllowed");
                        if(test != null) {
                            option.setBlankAllowed("true".equalsIgnoreCase(test.trim()));
                        }
                        fields.add(option);
                    }
                }
            }
        }
        for (Iterator it = map.keySet().iterator(); it.hasNext();) {
            String name = (String) it.next();
            MasterLoginModuleInfo info = (MasterLoginModuleInfo) map.get(name);
            List fields = (List) fieldMap.get(name);
            if(fields != null) {
                Collections.sort(fields);
                info.setOptions((OptionInfo[]) fields.toArray(new OptionInfo[fields.size()]));
            }
        }
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                MasterLoginModuleInfo m1 = (MasterLoginModuleInfo) o1, m2 = (MasterLoginModuleInfo) o2;
                if(m1.getName().equals("Other")) {
                    return 1;
                } else if(m2.getName().equals("Other")) {
                    return -1;
                } else {
                    return m1.getName().compareTo(m2.getName());
                }
            }
        });
        return (MasterLoginModuleInfo[]) list.toArray(new MasterLoginModuleInfo[list.size()]);
    }

    public final static class OptionInfo implements Serializable, Comparable {
        private final String name;
        private final String displayName;
        private final String description;
        private boolean password = false;
        private int length = 30;
        private int displayOrder = 1;
        private boolean blankAllowed = false;

        public OptionInfo(String name, String displayName, String description) {
            this.name = name;
            this.displayName = displayName;
            this.description = description;
        }

        public String getName() {
            return name;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }

        public boolean isPassword() {
            return password;
        }

        public void setPassword(boolean password) {
            this.password = password;
        }

        public int getLength() {
            return length;
        }

        public void setLength(int length) {
            this.length = length;
        }

        public int getDisplayOrder() {
            return displayOrder;
        }

        public void setDisplayOrder(int displayOrder) {
            this.displayOrder = displayOrder;
        }

        public int compareTo(Object o) {
            return displayOrder - ((OptionInfo)o).displayOrder;
        }
        
        public boolean isBlankAllowed() {
            return this.blankAllowed;
        }
        
        public void setBlankAllowed(boolean blankAllowed) {
            this.blankAllowed = blankAllowed;
        }
    }
}
