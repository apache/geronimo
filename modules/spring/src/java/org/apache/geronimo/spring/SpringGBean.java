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

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import net.sf.cglib.proxy.InterfaceMaker;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
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

  protected ClassLoader                _appClassLoader;
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

  class GeronimoBeanFactory
    extends DefaultListableBeanFactory
  {
    GeronimoBeanFactory(){super();}
  }

  public void
    doStart()
    throws Exception
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

    _appClassLoader=new URLClassLoader(urls, _classLoader);

    // delegate work to Spring framework...
    _factory=new GeronimoBeanFactory();
    XmlBeanDefinitionReader xbdr=new XmlBeanDefinitionReader(_factory);
    xbdr.setBeanClassLoader(_appClassLoader);
    xbdr.loadBeanDefinitions(new ClassPathResource(_configPath.toString(), _appClassLoader));

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
    // can we put this as a spring aspect around each POJO ? would be better...
    String pattern=_jmxName.getDomain()+":J2EEApplication="+_jmxName.getKeyProperty("J2EEApplication")+",J2EEServer="+_jmxName.getKeyProperty("J2EEServer")+",SpringModule="+_jmxName.getKeyProperty("name")+",j2eeType=SpringBean,*";
    ObjectName on=new ObjectName(pattern);

    // can't we do it like this - much less typo-prone...
    //     Hashtable props  =new Hashtable(_jmxName.getKeyPropertyList());
    //     props.put("SpringModule" , props.get("name"));
    //     props.put("j2eeType"     , "SpringBean");
    //     props.remove("name");
    //     props.put("*", "");
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
	_log.info("proxying: "+bean);
	_log.info("loading: "+gd.getName());
	//      	_kernel.loadGBeanProxy(bean, gd, _appClassLoader);
      	_kernel.loadGBean(gd, _appClassLoader);
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

  protected int _count=0;	// we should use a Spring generated unique name here - TODO

  public static class InvocationHandler
    implements java.lang.reflect.InvocationHandler, java.io.Serializable
  {
    protected Object _pojo;

    public
      InvocationHandler(Object pojo) {_pojo=pojo;}

    public Object
      invoke(Object proxy, Method method, Object[] args)
      throws Throwable
    {
      return _pojo.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(_pojo, args);
    }
  }

  protected synchronized GBeanData
    createPOJOGBeanData(Object bean, String name)
      throws MalformedObjectNameException
  {
    Class c=createProxyClass(bean);
    GBeanInfoBuilder gbif = new GBeanInfoBuilder(c, "POJO["+(_count++)+"]");

    gbif.addAttribute("invocationHandler", java.lang.reflect.InvocationHandler.class, true);
    gbif.setConstructor(new String[]{"invocationHandler"});
    // describe the rest of the POJOs public API
    Set pm=new HashSet();
    Method[] methods=c.getMethods();
    for (int i=0;i<methods.length;i++)
    {
      Method m=methods[i];
      String n=m.getName();
      Class[] pt=m.getParameterTypes();
      Class rt=m.getReturnType();

      // ugly way of collecting Bean property names - maybe we can get this from Spring ?
      if ((n.startsWith("get") && pt.length==0) || (n.startsWith("set") && pt.length==1 && rt==Void.TYPE))
	pm.add(n.substring(3,4).toLowerCase()+n.substring(4));
    }

    //    pm.remove("class"); // do we want this available ?
    gbif.addInterface(c, (String[])pm.toArray(new String[pm.size()]));
    //gbif.addInterface(c);
    GBeanData gbd=new GBeanData(createObjectName(name), gbif.getBeanInfo());
    // ensure the injection of the InvocationHandler into the newly instantiated Proxy
    gbd.setAttribute("invocationHandler"  , new InvocationHandler(bean));

    return gbd;
  }

  // We have to create a proxy here because the kernel only accepts
  // classes from which to instantiate GBeanInstance targets, not
  // instances, which is what we get from Spring. So we create a proxy
  // that can be instantiated and injected with an InvocationHandler
  // which will delegate all calls onto the corresponding method on
  // the bean that we wanted to pass in in the first place. Complex
  // syntactic sugar - eh...!
  protected Class
    createProxyClass(Object pojo)
  {
    InterfaceMaker im=new InterfaceMaker();
    im.add(pojo.getClass());	// add all class' public methods...

    // if POJO does not implement GBeanLifeCycle should we implement
    // it and dump/reroute it, to be safe ?

    Class c=im.create();
    return Proxy.getProxyClass(c.getClassLoader(), new Class[] {c});
  }
}
