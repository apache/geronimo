/*
 * Copyright 2001-2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.geronimo.axis;

import org.apache.axis.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.jmx.JMXUtil;

import javax.management.ObjectName;

import java.util.Collection;
import java.util.Iterator;

/**
 * @version $Rev: $ $Date: $
 */
public class EJBWSGBean implements GBeanLifecycle {
    private static Log log = LogFactory.getLog(EJBWSGBean.class);
    /**
     * Field name
     */
    private final String name;

    /**
     * Field GBEAN_INFO
     */
    private static final GBeanInfo GBEAN_INFO;

    /**
     * Field objectName
     */
    private final ObjectName objectName;
    private Configuration ejbConfig;
    private Collection classList;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("EJBWSGBean",
                EJBWSGBean.class);


        // attributes
        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addReference("ejbConfig", Configuration.class);
        infoFactory.addAttribute("classList", Collection.class, true);
        // operations
        infoFactory.setConstructor(new String[]{"Name",
                                                "objectName"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    /**
     * Constructor AxisGbean
     *
     * @param name
     * @param objectName
     */
    public EJBWSGBean(String name, String objectName) {
        this.name = name;
        this.objectName = JMXUtil.getObjectName(objectName);
    }

    /**
     * Method doFail
     */
    public void doFail() {
        log.info("Axis GBean has failed");
    }

    /**
     * Method doStart
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStart() throws WaitingException, Exception {
        System.out.println(name + "has started");
        ClassLoader cl = ejbConfig.getClassLoader();
        for (Iterator it = classList.iterator(); it.hasNext();) {
            String className = (String) it.next();
            ClassUtils.setClassLoader(className, cl);
        }
        AxisGeronimoUtils.addEntryToAxisDD(cl.getResourceAsStream("deploy.wsdd"));
        log.info(objectName);
    }

    /**
     * Method doStop
     *
     * @throws WaitingException
     * @throws Exception
     */
    public void doStop() throws WaitingException, Exception {
        log.info("WebServiceGBean has stoped");
    }

    /**
     * Method getGBeanInfo
     *
     * @return
     */
    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    /**
     * Method getName
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @return
     */
    public Collection getClassList() {
        return classList;
    }

    /**
     * @return
     */
    public Configuration getEjbConfig() {
        return ejbConfig;
    }

    /**
     * @param collection
     */
    public void setClassList(Collection collection) {
        classList = collection;
    }

    /**
     * @param configuration
     */
    public void setEjbConfig(Configuration configuration) {
        ejbConfig = configuration;
    }
}
