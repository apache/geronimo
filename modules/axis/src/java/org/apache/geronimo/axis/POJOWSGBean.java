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
import org.apache.geronimo.kernel.Kernel;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Iterator;

/**
 * Class AxisGbean
 */
public class POJOWSGBean implements GBeanLifecycle {
    private static Log log = LogFactory.getLog(POJOWSGBean.class);
    private final String name;
    private static final GBeanInfo GBEAN_INFO;
    private URL moduleURL;
    private Collection classList;

    static {
        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder("POJOWSGBean",
                POJOWSGBean.class);

        // attributes
        infoFactory.addAttribute("Name", String.class, true);
        infoFactory.addAttribute("kernel", Kernel.class, false);
        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("moduleURL", URL.class, true);
        infoFactory.addAttribute("classList", Collection.class, true);
        // operations
        infoFactory.setConstructor(new String[]{"kernel", "Name",
                                                "objectName"});
        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public POJOWSGBean(Kernel kernel, String name, String objectName) {
        this.name = name;
    }

    public void doFail() {
    }

    public void doStart() throws WaitingException, Exception {
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
     * @return
     */
    public URL getModuleURL() {
        return moduleURL;
    }

    /**
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * @param url
     */
    public void setModuleURL(URL url) {
        moduleURL = url;
    }

    /**
     * @return
     */
    public Collection getClassList() {
        return classList;
    }

    /**
     * @param collection
     */
    public void setClassList(Collection collection) {
        classList = collection;
    }

}
