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
import java.util.Iterator;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.WaitingException;
import org.apache.geronimo.kernel.Kernel;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * A GBean for creating graphs of Spring POJOs and auto-deploying them inside Geronimo as GBeans
 *
 * @version $Rev$
 */
public class SpringGBean
  implements GBeanLifecycle
{
  protected static final Log _log = LogFactory.getLog(SpringGBean.class);

  // injected into ctor
  protected final Kernel      _kernel;
  protected final String      _objectName;
  protected final ClassLoader _classLoader;
  protected final URI[]       _classPath;
  protected final URL         _configurationBaseUrl;
  protected final URI         _configPath;

  protected ObjectName                 _jmxName;
  protected DefaultListableBeanFactory _factory;

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
    _kernel               =kernel;
    _objectName           =objectName;
    _configPath           =configPath;
    _classLoader          =classLoader;
    _classPath            =classPath;
    _configurationBaseUrl =configurationBaseUrl;
  }

  //----------------------------------------
  // GBeanLifecycle
  //----------------------------------------

  public void
    doStart()
    throws WaitingException, Exception
  {
    _jmxName=new ObjectName(_objectName);

    // set up classloader
    URI root = URI.create(_configurationBaseUrl.toString());

    URL[] urls=new URL[_classPath.length];

    for (int i=0; i<_classPath.length; i++)
    {
      URL url=root.resolve(_classPath[i]).toURL();
      _log.info("_classPath["+i+"]: "+url);
      urls[i]=url;
    }

    ClassLoader cl=new URLClassLoader(urls, _classLoader);

    // delegate work to Spring framework...
    _factory=new DefaultListableBeanFactory();
    XmlBeanDefinitionReader xbdr=new XmlBeanDefinitionReader(_factory);
    xbdr.setBeanClassLoader(cl);
    xbdr.loadBeanDefinitions(new ClassPathResource(_configPath.toString(), cl));

    // install aspects around Spring Bean initialisation...
    _factory.addBeanPostProcessor(new BeanPostProcessor() {
     	public Object postProcessBeforeInitialization(Object bean, String name) throws BeansException {
     	  return beforeInitialization(bean, name);
     	}

	public Object postProcessAfterInitialization(Object bean, String name) throws BeansException {
	  return afterInitialization(bean, name);
	}
      });

    // force lazy construction of every bean described... - is there a better way - ROB ?
    String[] ids=_factory.getBeanDefinitionNames();
    int n=ids.length;
    for (int i=n; i>0; i--)
      _factory.getBean(ids[i-1]);

    _log.info("Deployed: "+n+" POJO"+(n==1?"":"s"));
  }


  public void
    doStop()
    throws Exception
  {
    tidyUp();
  }

  public void
    doFail()
  {
    try
    {
      tidyUp();
    }
    catch (Exception e)
    {
      _log.warn("problem decommissioning Spring module: "+_jmxName, e);
    }
  }

  protected void
    tidyUp()
    throws Exception
  {
    String pattern=_jmxName.getDomain()+":J2EEApplication="+_jmxName.getKeyProperty("J2EEApplication")+",J2EEServer="+_jmxName.getKeyProperty("J2EEServer")+",SpringModule="+_jmxName.getKeyProperty("name")+",j2eeType=SpringBean,*";
    ObjectName on=new ObjectName(pattern);

    // can't we do it like this - much less typo-prone...
    //     Hashtable props  =new Hashtable(_jmxName.getKeyPropertyList());
    //     props.put("SpringModule" , props.get("name"));
    //     props.put("j2eeType"     , "SpringBean");
    //     props.remove("name");
    //     props.put("*"           , null);
    //     ObjectName on=new ObjectName(_jmxName.getDomain(), props);

    Set peers=_kernel.listGBeans(on);
    for (Iterator i=peers.iterator(); i.hasNext();)
    {
      ObjectName tmp=(ObjectName)i.next();
      try
      {
	_log.info("stopping: "+tmp);
	_kernel.stopGBean(tmp);
	_log.info("unloading: "+tmp);
	_kernel.unloadGBean(tmp);
      }
      catch (Exception e)
      {
	_log.warn("problem decommissioning POJO peer GBean: "+tmp, e);
      }
    }

    _factory.destroySingletons();
  }

  //----------------------------------------
  // aspects around Spring Bean creation...
  //----------------------------------------

  /**
   * Hook to perform action before Spring Bean initialisation.
   */
  protected Object
    beforeInitialization(Object bean, String name)
  {
    return bean;
  }

  /**
   * Hook to perform action after Spring Bean initialisation.
   */
  protected Object
    afterInitialization(Object bean, String name)
    throws BeansException
  {
    // create a GBean peer...
    try
    {
      GBeanData gd=createPOJOGBeanData(bean, name);
      if (gd==null)
      	_log.warn("No GBean available for name: " + name + " bean: " + bean);
      else
      {
	_log.info("loading: "+gd.getName());
      	_kernel.loadGBean(gd, _classLoader);
	_log.info("starting: "+gd.getName());
	_kernel.startGBean(gd.getName());
      }
    }
    catch (Exception e)
    {
      throw new BeanDefinitionValidationException("Could not load the GBean for name: " + name + " bean: " + bean + ". Reason: " + e, e);
    }
    return bean;
  }

  //----------------------------------------
  // utils...
  //----------------------------------------

  /**
   * Factory method to create an ObjectName for the Spring bean
   *
   * @param name the name of the bean in the Spring config file
   * @return the ObjectName to use for the given Spring bean name
   */
  protected ObjectName
    createObjectName(String name)
    throws MalformedObjectNameException
  {
    Hashtable props  =new Hashtable(_jmxName.getKeyPropertyList());
    props.put("SpringModule" , props.get("name"));
    props.put("j2eeType"     , "SpringBean");
    props.put("name"         , name);
    return new ObjectName(_jmxName.getDomain(), props);
  }

  protected GBeanData
    createPOJOGBeanData(Object bean, String name)
    throws MalformedObjectNameException
  {
    GBeanData gbeanData=new GBeanData(createObjectName(name), POJOGBean.GBEAN_INFO);
    gbeanData.setAttribute("peer", bean);

    return gbeanData;
  }
}
