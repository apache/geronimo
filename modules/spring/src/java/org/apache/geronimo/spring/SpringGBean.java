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

import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.beans.factory.xml.XmlBeanFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * A GBean for creating graphs of Spring POJOs and auto-deploying them inside Geronimo as GBeans
 *
 * @version $Rev$
 */
public class SpringGBean
  implements GBeanLifecycle
{
  protected static final Log log = LogFactory.getLog(SpringGBean.class);

  // injected into ctor
  protected final Kernel      kernel;
  protected final String      objectName;
  protected final ClassLoader classLoader;
  protected final URI[]       classPath;
  protected final URL         configurationBaseUrl;
  protected final URI         configPath;

  protected DefaultListableBeanFactory factory;

  //----------------------------------------
  public static final GBeanInfo GBEAN_INFO;

  static
  {
    GBeanInfoBuilder infoBuilder = new GBeanInfoBuilder("Spring Application Context", SpringGBean.class);

    infoBuilder.addAttribute("kernel"               , Kernel.class      , false);
    infoBuilder.addAttribute("objectName"           , String.class      , false);
    infoBuilder.addAttribute("classLoader"          , ClassLoader.class , false);
    infoBuilder.addAttribute("classPath"            , URI[].class       , true);
    infoBuilder.addAttribute("configurationBaseUrl" , URL.class         , true);
    infoBuilder.addAttribute("configPath"           , URI.class         , true);

    infoBuilder.setConstructor(new String[]{
				 "kernel",
				 "objectName",
				 "classLoader",
				 "classPath",
				 "configurationBaseUrl",
				 "configPath"
			       });

    GBEAN_INFO = infoBuilder.getBeanInfo();
  }

  public static GBeanInfo getGBeanInfo() {return GBEAN_INFO;}

  //----------------------------------------


  public
    SpringGBean(Kernel kernel, String objectName, ClassLoader classLoader, URI[] classPath, URL configurationBaseUrl, URI configPath)
  {
    this.kernel               =kernel;
    this.objectName           =objectName;
    this.configPath           =configPath;
    this.classLoader          =classLoader;
    this.classPath            =classPath;
    this.configurationBaseUrl =configurationBaseUrl;
  }

  // GBeanLifecycle
  //-------------------------------------------------------------------------

  public void
    doStart()
    throws WaitingException, Exception
  {
    // set up classloader
    URI root = URI.create(configurationBaseUrl.toString());

    URL[] urls=new URL[classPath.length];

    for (int i=0; i<classPath.length; i++)
    {
      URL url=root.resolve(classPath[i]).toURL();
      log.info("classPath["+i+"]: "+url);
      urls[i]=url;
    }

    ClassLoader cl=new URLClassLoader(urls, classLoader);

    // delegate work to Spring framework...
    factory=new DefaultListableBeanFactory();
    XmlBeanDefinitionReader xbdr=new XmlBeanDefinitionReader(factory);
    xbdr.setBeanClassLoader(cl);
    xbdr.loadBeanDefinitions(new ClassPathResource(configPath.toString(), cl));

    // force lazy construction of every bean described...
    String[] ids=factory.getBeanDefinitionNames();
    int n=ids.length;
    for (int i=n; i>0; i--)
      factory.getBean(ids[i-1]);
    log.info("Deployed: "+n+" POJO"+(n==1?"":"s"));

    // James was going to use these to register each spring-bean as
    // a gbean - do we want to do that ?

    // factory.addBeanPostProcessor(new BeanPostProcessor() {
    // 	public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
    // 	  return beforeInitialization(bean, name);
    // 	}

    // 	public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
    // 	  return afterInitialization(bean, name);
    // 	}
    //       });
  }

  public void
    doStop()
    throws WaitingException, Exception
  {
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

  public void
    doFail()
  {
    factory.destroySingletons();
  }

  /**
   * Do we need to apply an interceptor to the bean?
   */
  protected Object
    beforeInitialization(Object bean, String name)
  {
    // we could add some interceptor stuff here if we like
    return bean;
  }

  /**
   * Create an GBean wrapper
   */
  protected Object afterInitialization(Object bean, String name)
    throws BeansException
  {
    //     try {
    //       ObjectName objectName = createObjectName(name);
    //       /*
    //  	springNameToObjectName.put(name, objectName);
    //       */
    //       GBeanMBean gbean = createGBean(bean, name);
    //       if (gbean == null) {
    //  	log.warn("No GBean available for name: " + name + " bean: " + bean);
    //       }
    //       else {
    //  	kernel.loadGBean(objectName, gbean);
    //       }
    //     }
    //     catch (Exception e) {
    //       throw new BeanDefinitionValidationException("Could not load the GBean for name: " + name + " bean: " + bean + ". Reason: " + e, e);
    //     }
    return bean;
  }

  /**
   * Factory method to create an ObjectName for the Spring bean
   *
   * @param name the name of the bean in the Spring config file
   * @return the ObjectName to use for the given Spring bean name
   */
//   protected ObjectName createObjectName(String name) throws MalformedObjectNameException {
//     Hashtable nameProps = new Hashtable(objectName.getKeyPropertyList());
//     nameProps.put("name", name);
//     return new ObjectName(objectName.getDomain(), nameProps);
//   }

  /**
   * @return a newly created GBeanMBean for the given bean
   */
//   protected GBeanMBean
//     createGBean(Object bean, String name)
//   {
//     // TODO
//     return null;
//   }
}
