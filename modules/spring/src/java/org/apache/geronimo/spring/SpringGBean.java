/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.spring;

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;
import java.util.Hashtable;

/**
 * A GBean for creating graphs of Spring POJOs and auto-deploying them inside Geronimo as GBeans
 *
 * @version $Rev$
 */
public class SpringGBean implements GBeanLifecycle {
    private static final Log log = LogFactory.getLog(SpringGBean.class);

    private ObjectName objectName;
    private String url;
    private Kernel kernel;
    private XmlBeanFactory factory;
    /*
    private String[] springNames;
    private Map springNameToObjectName = new HashMap();
    */

    public SpringGBean(Kernel kernel) {
        this.kernel = kernel;
    }

    // Properties
    //-------------------------------------------------------------------------
    public ObjectName getObjectName() {
        return objectName;
    }

    public void setObjectName(ObjectName objectName) {
        this.objectName = objectName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    // GBeanLifecycle
    //-------------------------------------------------------------------------

    public void doStart() throws WaitingException, Exception {
        if (url == null) {
            throw new Exception("No URL specified, cannot load the Spring XML config file");
        }
        if (objectName == null) {
            throw new Exception("No objectName injected for this GBean by the kernel!");
        }
        factory = new XmlBeanFactory(createResource());
        factory.addBeanPostProcessor(new BeanPostProcessor() {
            public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
                return beforeInitialization(bean, name);
            }

            public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
                return afterInitialization(bean, name);
            }
        });
    }

    public void doStop() throws WaitingException, Exception {
        factory.destroySingletons();
/*
        if (springNames != null) {
            for (int i = 0, size = springNames.length; i < size; i++ ) {
                String name = springNames[i];
                ObjectName objectName = (ObjectName) springNameToObjectName.get(name);
                // TODO - how do we invoke the 'close' in Spring??
            }
        }
*/
    }

    public void doFail() {
        factory.destroySingletons();
    }

    /**
     * Creates a resource from the current URL which by default will use the classpath
     *
     * @return
     */
    protected Resource createResource() {
        return new ClassPathResource(url);
    }

    /**
     * Do we need to apply an interceptor to the bean?
     */
    protected Object beforeInitialization(Object bean, String name) {
        // we could add some interceptor stuff here if we like
        return bean;
    }

    /**
     * Create an GBean wrapper
     */
    protected Object afterInitialization(Object bean, String name) throws BeansException {
        try {
            ObjectName objectName = createObjectName(name);
            /*
            springNameToObjectName.put(name, objectName);
            */
            GBeanMBean gbean = createGBean(bean, name);
            if (gbean == null) {
                log.warn("No GBean available for name: " + name + " bean: " + bean);
            }
            else {
                kernel.loadGBean(objectName, gbean);
            }
        }
        catch (Exception e) {
            throw new BeanDefinitionValidationException("Could not load the GBean for name: " + name + " bean: " + bean + ". Reason: " + e, e);
        }
        return bean;
    }

    /**
     * Factory method to create an ObjectName for the Spring bean
     *
     * @param name the name of the bean in the Spring config file
     * @return the ObjectName to use for the given Spring bean name
     */
    protected ObjectName createObjectName(String name) throws MalformedObjectNameException {
        Hashtable nameProps = new Hashtable(objectName.getKeyPropertyList());
        nameProps.put("name", name);
        return new ObjectName(objectName.getDomain(), nameProps);
    }

    /**
     * @return a newly created GBeanMBean for the given bean
     */
    protected GBeanMBean createGBean(Object bean, String name) {
        // TODO
        return null;
    }
}