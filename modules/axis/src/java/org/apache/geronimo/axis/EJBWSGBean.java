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
import java.util.Collection;
import java.util.Iterator;

/**
 * @version $Rev: $ $Date: $
 */
public class EJBWSGBean implements GBeanLifecycle {
    private static Log log = LogFactory.getLog(EJBWSGBean.class);
    
    public static final GBeanInfo GBEAN_INFO;
    private final String objectName; 
    
    private Collection classList;
    private final Configuration ejbConfig;


    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("EJBWSGBean",
                EJBWSGBean.class);

        // attributes
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("classList", Collection.class, true);
        
        infoFactory.addReference("EjbConfig", Configuration.class);

        // operations
        infoFactory.setConstructor(new String[]{"objectName"});
        infoFactory.setConstructor(new String[]{"objectName","EjbConfig"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public EJBWSGBean(String objectName) {
        this.objectName = objectName;
        this.ejbConfig = null;
    }

    public EJBWSGBean(String objectName,Configuration ejbConfig) {
        this.objectName = objectName;
        System.out.println(ejbConfig);
        this.ejbConfig = (Configuration)ejbConfig;
    }


    
    public void doFail() {
    }

    public void doStart() throws WaitingException, Exception {
        ClassLoader cl = ejbConfig.getClassLoader();
        for (Iterator it = classList.iterator(); it.hasNext();) {
            String className = (String) it.next();
            ClassUtils.setClassLoader(className, cl);
        }
        AxisGeronimoUtils.addEntryToAxisDD(cl.getResourceAsStream("deploy.wsdd"));
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

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }



    public Collection getClassList() {
        return classList;
    }

    public void setClassList(Collection collection) {
        classList = collection;
    }

//    public Configuration getEjbConfig() {
//        return ejbConfig;
//    }
//    public void setEjbConfig(Configuration configuration) {
//        ejbConfig = configuration;
//    }
}
