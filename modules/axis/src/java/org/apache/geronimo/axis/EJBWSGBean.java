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

import java.util.Collection;
import java.util.Iterator;

import org.apache.axis.utils.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;


/**
 * @version $Rev: $ $Date: $
 */
public class EJBWSGBean implements GBeanLifecycle {
    private static Log log = LogFactory.getLog(EJBWSGBean.class);
    private static final GBeanInfo GBEAN_INFO;

    //GBean Attributes
    private final String objectName;
    private final Configuration ejbConfig;
    private Collection classList;
    

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("EJBWSGBean",
                EJBWSGBean.class);
        // attributes
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addReference("ejbConfig", Configuration.class, null);
        infoFactory.addAttribute("classList", Collection.class, true);
        
        // operations
        infoFactory.setConstructor(new String[]{"objectName","ejbConfig","classList"});
        
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    /**
     * Constructor AxisGbean
     */
    public EJBWSGBean(String objectName,Configuration ejbConfig,Collection classList) {
        this.objectName = objectName;
        this.ejbConfig = ejbConfig;
        this.classList = classList;
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
     * @throws Exception
     */
    public void doStart() throws Exception {
        log.info(objectName + "has started");
        ClassLoader cl = ejbConfig.getConfigurationClassLoader();
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
     * @throws Exception
     */
    public void doStop() throws Exception {
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

}
