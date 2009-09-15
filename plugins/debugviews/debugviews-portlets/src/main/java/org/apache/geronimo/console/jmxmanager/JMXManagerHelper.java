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

package org.apache.geronimo.console.jmxmanager;

import java.net.URI;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.management.ObjectName;
import javax.management.j2ee.statistics.BoundaryStatistic;
import javax.management.j2ee.statistics.BoundedRangeStatistic;
import javax.management.j2ee.statistics.CountStatistic;
import javax.management.j2ee.statistics.RangeStatistic;
import javax.management.j2ee.statistics.Statistic;
import javax.management.j2ee.statistics.Stats;
import javax.management.j2ee.statistics.TimeStatistic;

import org.apache.geronimo.console.util.Tree;
import org.apache.geronimo.console.util.TreeEntry;
import org.apache.geronimo.console.util.TimeUtils;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GOperationInfo;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.KernelRegistry;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

/**
 * The JMX manager helper
 */
@RemoteProxy
public class JMXManagerHelper {
    
    /** Types */
    public static final String All_TYPE = "All";
    public static final String JAVAEE_TYPE = "JavaEE";
    public static final String GERONIMO_TYPE = "Geronimo";
    public static final String GERONIMO_SERVICE_TYPE = "GeronimoService";
    public static final String STATS_PROVIDER_TYPE = "StatsProvider";
    public static final String SEARCHNODE_TYPE = "SearchNode";
    public static final String PLACEHOLDER_TYPE = "placeholder";
    
    /** Used to return all MBeans */
    private static final String ALL_MBEANS = "AllMBeans";
    private static final String SERVICEMODULE_KEY = "ServiceModule";
    private static final String GBEANINFO_ATTRIB = "GBeanInfo";
    private static final String STATSPROVIDER_ATTRIB = "statisticsProvider";
    private static final String STATS_ATTRIB = "stats";
    

    private final Kernel kernel;

    /**
     * Construct an JMX manager helper (default)
     */
    public JMXManagerHelper() {
        kernel = KernelRegistry.getSingleKernel();
    }

    /**
     * List MBeans using a domain
     */
    public Collection<String[]> listByDomain(String domain) {
        Collection<String[]> result = new ArrayList<String[]>();
        if ((domain == null) || (domain.trim().length() == 0)) {
            return result;
        }

        return listByPattern(domain + ":*");
    }

    /**
     * List MBeans containing a substring in its object name
     */
    @RemoteMethod
    public Collection<String[]> listBySubstring(String substring) {
        Collection<String[]> result = new ArrayList<String[]>();
        if ((substring == null) || (substring.trim().length() == 0)) {
            return result;
        }
        
        Collection<AbstractName> abstractNames = getAbstractNames(substring);
        for (Iterator<AbstractName> it = abstractNames.iterator(); it.hasNext();) {
            AbstractName abstractName = it.next();
            ObjectName objectName = abstractName.getObjectName();
            String[] pair = { abstractName.toString(), objectName.toString() };
            result.add(pair);
        }
        
        return result;
    }
    
    /**
     * List MBeans using a pattern (ObjectName)
     */
    @RemoteMethod
    public Collection<String[]> listByPattern(String pattern) {
        Collection<String[]> result = new ArrayList<String[]>();
        if ((pattern == null) || (pattern.trim().length() == 0)) {
            return result;
        }

        try {
            // TODO: Use AbstractNameQuery
            // Uses Object names for query pattern to support
            // domain searches. Can't find a way to do it using
            // AbstractNameQuery.
            //Map<ObjectName, AbstractName> abstractNames = getAbstractNames();
            ObjectName objnamePattern = new ObjectName(pattern);
            Set<AbstractName> beans = kernel.listGBeans(objnamePattern);
            for (Iterator<AbstractName> it = beans.iterator(); it.hasNext();) {
                AbstractName abstractName = (AbstractName) it.next();
                ObjectName objectName = abstractName.getObjectName();
                String[] pair = { abstractName.toString(), objectName.toString() };
                result.add(pair);
            }
        } catch (Exception e) {
            // Malformed object name, just return what you have
        }

        return result;
    }

    /**
     * List MBeans using J2EE type
     */
    @RemoteMethod
    public Collection<String[]> listByJ2EEType(String type) {
        Collection<String[]> result = new ArrayList<String[]>();
        Map<String, String> m = null;
        
        if ((type == null) || (type.trim().length() == 0)) {
            return result;
        } else {
            if (ALL_MBEANS.equalsIgnoreCase(type)) {
                m = Collections.EMPTY_MAP;
            } else {
                m = Collections.singletonMap(NameFactory.J2EE_TYPE, type);
            }
        }

        AbstractNameQuery query = new AbstractNameQuery(null, m, Collections.EMPTY_SET);
        Set<AbstractName> beans = kernel.listGBeans(query);
        for (Iterator<AbstractName> it = beans.iterator(); it.hasNext();) {
            AbstractName abstractName = (AbstractName) it.next();
            ObjectName objectName = abstractName.getObjectName();
            String[] pair = { abstractName.toString(), objectName.toString() };
            result.add(pair);
        }

        return result;
    }

    /**
     * Return all service modules
     */
    public Collection<String> getServiceModules() {
        Collection<String> serviceModules = new HashSet<String>();
        Collection<AbstractName> serviceModuleMBeans = this.getAbstractNames(SERVICEMODULE_KEY + "=");
        for (Iterator<AbstractName> it = serviceModuleMBeans.iterator(); it.hasNext();) {
            AbstractName abstractName = it.next();
            String serviceModule = abstractName.getNameProperty(SERVICEMODULE_KEY);
            if (!serviceModules.contains(serviceModule)) {
                serviceModules.add(serviceModule);
            }
        }
        return serviceModules;
    }

    /**
     * Return abstract names containing a substring
     */
    private Collection<AbstractName> getAbstractNames(String substring) {
        Collection<AbstractName> result = new ArrayList<AbstractName>();
        if ((substring == null) || (substring.trim().length() == 0)) {
            return result;
        }

        Map<ObjectName,AbstractName> abstractNameMap = getAbstractNames();
        for (Iterator<ObjectName> it = abstractNameMap.keySet().iterator(); it.hasNext();) {
            ObjectName objectName = it.next();
            if (objectName.toString().indexOf(substring) > 0) {
                AbstractName abstractName = abstractNameMap.get(objectName);
                result.add(abstractName);
            }
        }
        
        return result;
    }

    /**
     * Return all abstract names as a map
     */
    private Map<ObjectName,AbstractName> getAbstractNames() {
        Map<ObjectName,AbstractName> abstractNameMap = new HashMap<ObjectName,AbstractName>();
        // Create Map (Key = ObjectName, Value = AbstractName)
        AbstractNameQuery query = new AbstractNameQuery(null, Collections.EMPTY_MAP, Collections.EMPTY_SET);
        Set<AbstractName> allBeans = kernel.listGBeans(query);
        for (Iterator<AbstractName> it = allBeans.iterator(); it.hasNext();) {
            AbstractName abstractName = it.next();
            ObjectName objectName = abstractName.getObjectName();
            abstractNameMap.put(objectName, abstractName);
        }

        return abstractNameMap;
    }

    /**
     * Return MBean attributes
     */
    @RemoteMethod
    public Collection<Map<String, String>> getAttributes(String abstractNameString) {
        Map<String, Map<String, String>> attributes = new TreeMap<String, Map<String, String>>();
        try {
            AbstractName abstractName = new AbstractName(URI.create(abstractNameString));
            GBeanInfo gBeanInfo = kernel.getGBeanInfo(abstractName);
            Set<GAttributeInfo> attrs = gBeanInfo.getAttributes();
            for (Iterator<GAttributeInfo> i = attrs.iterator(); i.hasNext();) {
                GAttributeInfo gAttrInfo = i.next();
                String attrName = gAttrInfo.getName();
                if (!GBEANINFO_ATTRIB.equals(attrName)) {   // Don't include the 'GBeanInfo' attributes
                    Map<String, String> attrInfoMap = getAttrInfoAsMap(abstractName, gAttrInfo);
                    attributes.put(attrName, attrInfoMap);
                }
            }
        } catch (GBeanNotFoundException e) {
            // GBean not found, just ignore
        }

        return attributes.values();
    }

    /**
     * Return attribute info as map
     */
    private Map<String, String> getAttrInfoAsMap(AbstractName abstractName,
            GAttributeInfo attribInfo) {
        Map<String, String> map = new TreeMap<String, String>();
        String attribName = attribInfo.getName();
        map.put("name", attribName);
        map.put("getterName", attribInfo.getGetterName());
        map.put("setterName", attribInfo.getSetterName());
        map.put("type", attribInfo.getType());
        map.put("manageable", String.valueOf(attribInfo.isManageable()));
        map.put("persistent", String.valueOf(attribInfo.isPersistent()));
        map.put("readable", String.valueOf(attribInfo.isReadable()));
        map.put("writable", String.valueOf(attribInfo.isWritable()));
        if (attribInfo.isReadable()) {
            String attribValue = "";
            try {
                Object value = kernel.getAttribute(abstractName, attribName);
                if (value != null) {
                    if (value instanceof String[]) {
                        attribValue = Arrays.asList((String[]) value)
                                .toString();
                    } else {
                        attribValue = value.toString();
                    }
                }
            } catch (Exception e) {
                // GBean or attribute not found, just ignore
                attribValue = "** EXCEPTION: " + e;
            }
            map.put("value", attribValue);
        }
        return map;
    }

    /**
     * Return MBean operations
     */
    @RemoteMethod
    public Collection<Map<String, Object>> getOperations(String abstractName) {
        Map<String, Map<String, Object>> operations = new TreeMap<String, Map<String, Object>>();
        try {
            AbstractName aname = new AbstractName(URI.create(abstractName));
            GBeanInfo gBeaninfo = kernel.getGBeanInfo(aname);
            Set<GOperationInfo> opers = gBeaninfo.getOperations();
            for (Iterator<GOperationInfo> i = opers.iterator(); i.hasNext();) {
                GOperationInfo operInfo = i.next();
                Map<String, Object> operInfoMap = getOperInfoAsMap(operInfo);
                String operName = (String) operInfoMap.get("name");
                operations.put(operName, operInfoMap);
            }
        } catch (Exception e) {
            // GBean not found, just ignore
        }

        return operations.values();
    }

    /**
     * Return operation info as map
     */
    private Map<String, Object> getOperInfoAsMap(GOperationInfo operInfo) {
        Map<String, Object> map = new TreeMap<String, Object>();
        map.put("methodName", operInfo.getMethodName());
        map.put("name", operInfo.getName());
        map.put("parameterList", operInfo.getParameterList());
        return map;
    }

    /**
     * Return MBean basic info
     */
    @RemoteMethod
    public Collection<String[]> getMBeanInfo(String abstractName) {
        Collection<String[]> info = new ArrayList<String[]>();
        try {
            AbstractName aname = new AbstractName(URI.create(abstractName));
            info.add(new String[] { "abstractName", aname.toString() });
            ObjectName oname = aname.getObjectName();
            info.add(new String[] { "objectName", oname.toString() });
            GBeanInfo beanInfo = kernel.getGBeanInfo(aname);
            String className = beanInfo.getClassName();
            info.add(new String[] { "className", className });
            String domain = oname.getDomain();
            info.add(new String[] { "domain", domain });
            String j2eeType = beanInfo.getJ2eeType();
            info.add(new String[] { "j2eeType", j2eeType });
            // String sourceClass = beanInfo.getSourceClass();
            // info.add(new String[] { "sourceClass", sourceClass });
        } catch (Exception e) {
            // GBean not found, just ignore
        }

        return info;
    }

    /**
     * Return all MBeans that provide stats
     */
    @RemoteMethod
    public Collection<String[]> getStatsProviderMBeans() {
        Collection<String[]> result = new ArrayList<String[]>();

        Object[] allMBeans = listByPattern("*:*").toArray();
        for (int i = 0; i < allMBeans.length; i++) {
            try {
                String[] aPair = (String[]) allMBeans[i];
                AbstractName abstractName = new AbstractName(URI.create(aPair[0]));
                boolean isStatisticsProvider = ((Boolean)kernel.getAttribute(abstractName, "statisticsProvider")).booleanValue();
                if (isStatisticsProvider) {
                    result.add(aPair);
                }
            } catch (Exception e) {
                // ignore
            }
        }

        return result;
    }

    /**
     * Return MBean stats
     */
    @RemoteMethod
    public Collection<Collection<String[]>> getMBeanStats(String abstractName) {
        Map<String, Collection<String[]>> mbeanStats = new TreeMap<String, Collection<String[]>>();
        try {
            AbstractName aname = new AbstractName(URI.create(abstractName));
            Boolean statisticsProvider = (Boolean) kernel.getAttribute(aname,
                    STATSPROVIDER_ATTRIB);
            Stats stats = (Stats) kernel.getAttribute(aname, STATS_ATTRIB);
            if (statisticsProvider.booleanValue() == true && (stats != null)) {
                String[] statisticNames = stats.getStatisticNames();
                for (int i = 0; i < statisticNames.length; i++) {
                    Statistic statistic = stats.getStatistic(statisticNames[i]);

                    Collection<String[]> mbeanStat = new ArrayList<String[]>();
                    String name = statistic.getName();
                    mbeanStat.add(new String[] { "Name", name });
                    // String className = statistic.getClass().getName();
                    // mbeanStat.add(new String[] { "Type", className });
                    mbeanStat.add(new String[] { "Description", statistic.getDescription() });
                    mbeanStat.add(new String[] { "Unit", statistic.getUnit() });
                    Date startTime = new Date(statistic.getStartTime());
                    mbeanStat.add(new String[] { "Start Time", startTime.toString() });
                    Date lastSampleTime = new Date(statistic.getLastSampleTime());
                    mbeanStat.add(new String[] { "Last Sample Time", lastSampleTime.toString() });

                    if (statistic instanceof CountStatistic) {
                        CountStatistic cStat = (CountStatistic) statistic;
                        long count = cStat.getCount();
                        mbeanStat.add(new String[] { "Count", Long.toString(count) });
                    } else if (statistic instanceof TimeStatistic) {
                        TimeStatistic tStat = (TimeStatistic) statistic;
                        long count = tStat.getCount();
                        mbeanStat.add(new String[] { "Count", Long.toString(count) });
                        String maxTime = TimeUtils.formatDuration(tStat.getMaxTime());
                        mbeanStat.add(new String[] { "Max Time", maxTime });
                        String minTime = TimeUtils.formatDuration(tStat.getMinTime());
                        mbeanStat.add(new String[] { "Min Time", minTime });
                        long totalTime = tStat.getTotalTime();
                        mbeanStat.add(new String[] { "Total Time", Long.toString(totalTime) });
                    } else if (statistic instanceof BoundedRangeStatistic) {
                        BoundedRangeStatistic brStat = (BoundedRangeStatistic) statistic;
                        long upperBound = brStat.getUpperBound();
                        mbeanStat.add(new String[] { "Upper Bound", Long.toString(upperBound) });
                        long lowerBound = brStat.getLowerBound();
                        mbeanStat.add(new String[] { "Lower Bound", Long.toString(lowerBound) });
                        long highWaterMark = brStat.getHighWaterMark();
                        mbeanStat.add(new String[] { "High Water Mark", Long.toString(highWaterMark) });
                        long lowWaterMark = brStat.getLowWaterMark();
                        mbeanStat.add(new String[] { "Low Water Mark", Long.toString(lowWaterMark) });
                        long current = brStat.getCurrent();
                        mbeanStat.add(new String[] { "Current", Long.toString(current) });
                    } else if (statistic instanceof BoundaryStatistic) {
                        BoundaryStatistic bStat = (BoundaryStatistic) statistic;
                        long upperBound = bStat.getUpperBound();
                        mbeanStat.add(new String[] { "Upper Bound", Long.toString(upperBound) });
                        long lowerBound = bStat.getLowerBound();
                        mbeanStat.add(new String[] { "Lower Bound", Long.toString(lowerBound) });
                    } else if (statistic instanceof RangeStatistic) {
                        RangeStatistic rStat = (RangeStatistic) statistic;
                        long highWaterMark = rStat.getHighWaterMark();
                        mbeanStat.add(new String[] { "High Water Mark", Long.toString(highWaterMark) });
                        long lowWaterMark = rStat.getLowWaterMark();
                        mbeanStat.add(new String[] { "Low Water Mark", Long.toString(lowWaterMark) });
                        long current = rStat.getCurrent();
                        mbeanStat.add(new String[] { "Current", Long.toString(current) });
                    }

                    mbeanStats.put(name, mbeanStat);
                }
            }
        } catch (Exception e) {
            // GBean not found, just ignore
        }

        return mbeanStats.values();
    }

    /**
     * Invoke MBean operation with arguments
     */
    @RemoteMethod
    public String[] invokeOperWithArgs(String abstractName, String methodName,
            String[] args, String[] types) {
        String[] result = new String[2]; // return method name & result
        result[0] = methodName + "(...)";

        try {
            Object[] newArgs = processOperArgs(args, types);
            AbstractName aname = new AbstractName(URI.create(abstractName));
            Object res = kernel.invoke(aname, methodName, newArgs, types);
            if (res != null) {
                result[1] = res.toString();
            } else {
                result[1] = "<null>";
            }
        } catch (Exception e) {
            result[1] = e.toString();
        }

        return result;
    }

    /**
     * Invoke MBean operation without arguments
     */
    @RemoteMethod
    public String[] invokeOperNoArgs(String abstractName, String methodName) {
        String[] result = new String[2]; // return method name & result
        result[0] = methodName + "()";

        try {
            AbstractName aname = new AbstractName(URI.create(abstractName));
            Object res = kernel.invoke(aname, methodName);
            if (res != null) {
                result[1] = res.toString();
            } else {
                result[1] = "<null>";
            }
        } catch (Exception e) {
            result[1] = e.toString();
        }

        return result;
    }

    /**
     * Process MBean operation arguments
     */
    private Object[] processOperArgs(String[] args, String[] types)
            throws Exception {
        // TODO: Modify this algorithm and add other classes
        Object[] newArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            String type = types[i];
            String arg = args[i];
            newArgs[i] = createObject(arg, type);
        }

        return newArgs;
    }

    /**
     * Create MBean operation argument
     */
    private Object createObject(String arg, String type) throws Exception {
        Object newArg = new Object();
        if ("byte".equals(type) || "java.lang.Byte".equals(type)) {
            newArg = new Byte(arg);
        } else if ("short".equals(type) || "java.lang.Short".equals(type)) {
            newArg = new Short(arg);
        } else if ("int".equals(type) || "java.lang.Integer".equals(type)) {
            newArg = new Integer(arg);
        } else if ("long".equals(type) || "java.lang.Long".equals(type)) {
            newArg = new Long(arg);
        } else if ("float".equals(type) || "java.lang.Float".equals(type)) {
            newArg = new Float(arg);
        } else if ("double".equals(type) || "java.lang.Double".equals(type)) {
            newArg = new Double(arg);
        } else if ("char".equals(type) || "java.lang.Character".equals(type)) {
            newArg = new Character(arg.charAt(0));
        } else if ("boolean".equals(type) || "java.lang.Boolean".equals(type)) {
            newArg = new Boolean(arg);
        } else if ("java.lang.String".equals(type)) {
            newArg = arg;
        } else if ("java.lang.Object".equals(type)) {
            newArg = arg;
        } else if ("java.util.Date".equals(type)) {
            newArg = DateFormat.getInstance().parse(arg);
        } else if ("java.net.URL".equals(type)) {
            newArg = new URL(arg);
        } else if ("java.net.URI".equals(type)) {
            newArg = new URI(arg);
        } else if ("javax.management.ObjectName".equals(type)) {
            newArg = new ObjectName(arg);
        } else if ("org.apache.geronimo.gbean.AbstractName".equals(type)) {
            newArg = new AbstractName(URI.create(arg));
        } else {
            // Unknown type, throw exception
            String errorMsg = "Can't create instance of '" + type + "' using '"
                    + arg + "'.";
            throw new IllegalArgumentException(errorMsg);
        }

        return newArg;
    }

    /**
     * Set MBean attribute value
     */
    public String[] setAttribute(String abstractName, String attribName,
            String attribValue, String attribType) {
        String[] result = new String[2]; // return attribute name & result
        result[0] = attribName;
        result[1] = "<SUCCESS>"; 
        
        try {
            AbstractName aname = new AbstractName(URI.create(abstractName));
            Object newAttribValue = createObject(attribValue, attribType);
            kernel.setAttribute(aname, attribName, newAttribValue);
        } catch (Exception e) {
            result[1] = e.toString();
        }

        return result;
    }
    
    
    @RemoteMethod
    public Tree getJMXInformation() {
        
        //build "All MBeans" Node
        TreeEntry allMBeansEntry = new TreeEntry("All MBeans");
        allMBeansEntry.addChild(new TreeEntry("geronimo", All_TYPE));
        allMBeansEntry.addChild(new TreeEntry("geronimo.config", All_TYPE));
        //add place holder
        this.addPlaceholder(allMBeansEntry.getChildren());
        
        
        //build "JavaEE MBean" Node
        TreeEntry javaEEMBeansEntry = new TreeEntry("J2EE MBeans");
        javaEEMBeansEntry.addChild(new TreeEntry("AppClientModule", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("EJBModule", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("EntityBean", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("J2EEApplication", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("J2EEDomain", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("J2EEServer", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JavaMailResource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JCAConnectionFactory", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JCAManagedConnectionFactory", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JCAResource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JDBCDataSource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JDBCDriver", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JDBCResource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JMSResource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JNDIResource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JTAResource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("JVM", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("MessageDrivenBean", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("PersistenceUnit", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("ResourceAdapter", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("ResourceAdapterModule", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("RMI_IIOPResource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("Servlet", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("StatefulSessionBean", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("StatelessSessionBean", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("URLResource", JAVAEE_TYPE));
        javaEEMBeansEntry.addChild(new TreeEntry("WebModule", JAVAEE_TYPE));
        //add place holder
        this.addPlaceholder(javaEEMBeansEntry.getChildren());
        
        //build "Geronimo MBean" Node
        TreeEntry geronimoMBeansEntry = new TreeEntry("Geronimo MBeans" );
        geronimoMBeansEntry.addChild(new TreeEntry("AppClient", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ArtifactManager", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ArtifactResolver", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("AttributeStore", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ConfigBuilder", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ConfigurationEntry", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ConfigurationManager", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ConfigurationStore", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("CORBABean", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("CORBACSS", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("CORBATSS", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("Deployer", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("DeploymentConfigurer", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("GBean", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("Host", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JaasLoginService", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JACCManager", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JAXRConnectionFactory", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JCAActivationSpec", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JCAAdminObject", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JCAConnectionManager", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JCAConnectionTracker", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JCAResourceAdapter", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JCAWorkManager", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JMSConnector", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JMSPersistence", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("JMSServer", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("KeyGenerator", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("Keystore", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("LoginModule", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("LoginModuleUse", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("MEJB", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ModuleBuilder", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("PersistentConfigurationList", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("RealmBridge", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("Repository", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("RoleMapper", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("SecurityRealm", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ServiceModule", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ServletTemplate", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ServletWebFilterMapping", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("ServletWebServiceTemplate", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("SystemLog", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("TomcatValve", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("TransactionContextManager", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("TransactionLog", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("TransactionManager", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("URLPattern", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("URLWebFilterMapping", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("WebFilter", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("WSLink", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("XIDFactory", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("XIDImporter", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("XmlAttributeBuilder", GERONIMO_TYPE));
        geronimoMBeansEntry.addChild(new TreeEntry("XmlReferenceBuilder", GERONIMO_TYPE));
        //add place holder
        this.addPlaceholder(geronimoMBeansEntry.getChildren());
            
        
        //build "Geronimo Service Module MBeans" node
        TreeEntry geronimoServiceEntry = new TreeEntry("Geronimo Service Module MBeans" );
        Collection<String> serviceModules = this.getServiceModules();
        Iterator<String> it = serviceModules.iterator();
        while (it.hasNext()){
            String abstractName = it.next();
            geronimoServiceEntry.addChild(new TreeEntry(abstractName, GERONIMO_SERVICE_TYPE));
        }
        //add place holder
        this.addPlaceholder(geronimoServiceEntry.getChildren());
        
        
        //build "Stats Provider MBeans" node
        TreeEntry statsProviderEntry = new TreeEntry("Stats Provider MBeans", STATS_PROVIDER_TYPE);
        this.addPlaceholder(statsProviderEntry);
        
        //build "Search Results" node
        TreeEntry searchNodeEntry = new TreeEntry("Search Results", SEARCHNODE_TYPE);
        this.addPlaceholder(searchNodeEntry);
        
        //build the tree
        Tree tree = new Tree(null, "name");     //id = null means the id will be auto-generated
        tree.addItem(allMBeansEntry);
        tree.addItem(javaEEMBeansEntry);
        tree.addItem(geronimoMBeansEntry);    
        tree.addItem(geronimoServiceEntry);
        tree.addItem(statsProviderEntry);
        tree.addItem(searchNodeEntry);
        return tree;
    }
    
    private void addPlaceholder(TreeEntry entry){
        entry.addChild(new TreeEntry("null", PLACEHOLDER_TYPE));
    }
    
    private void addPlaceholder(List<TreeEntry> entries){
        Iterator<TreeEntry> it = entries.iterator();
        while(it.hasNext()){
            TreeEntry entry = it.next();
            this.addPlaceholder(entry);
        }
    }
    

}
