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

import java.net.URL;
import java.net.URLClassLoader;
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
public class POJOWSGBean implements GBeanLifecycle {
    private static Log log = LogFactory.getLog(POJOWSGBean.class);

    private static final GBeanInfo GBEAN_INFO;

    private final String objectName;
    private final URL moduleURL;
    private final Collection classList;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("POJOWSGBean",
                POJOWSGBean.class);

        // attributes
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("moduleURL", URL.class, true);
        infoFactory.addAttribute("classList", Collection.class, true);
        // operations
        infoFactory.setConstructor(new String[]{"objectName","moduleURL","classList"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    
    public POJOWSGBean(String objectName,URL moduleURL,Collection classList) {
        this.objectName = objectName;
        this.moduleURL = moduleURL;
        this.classList = classList;
    }


    public void doFail() {
    }

    public void doStart() throws Exception {
        log.info("POJO WS starting");
        ClassLoader cl = new URLClassLoader(new URL[]{moduleURL});
        for (Iterator it = classList.iterator(); it.hasNext();) {
            String className = (String) it.next();
            ClassUtils.setClassLoader(className, cl);
        }
        AxisGeronimoUtils.addEntryToAxisDD(cl.getResourceAsStream("deploy.wsdd"));
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
