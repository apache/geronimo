/**
 *
 * Copyright 2004 The Apache Software Foundation
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
package org.apache.geronimo.spring.deployment;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.util.NestedJarFile;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.spring.SpringGBean;
import org.apache.geronimo.spring.SpringApplicationImpl;

/**
 * @version $Rev: 126313 $ $Date: 2005-01-24 21:03:52 +0000 (Mon, 24 Jan 2005) $
 */
public class SPRConfigBuilder
  implements ConfigurationBuilder
{
  protected static final Log log=LogFactory.getLog(SPRConfigBuilder.class);
  protected static final String defaultConfigPath="META-INF/spring.xml";

  protected final Kernel     kernel;
  protected final Repository repository;
  protected final URI        defaultParentId;

  public
    SPRConfigBuilder(URI defaultParentId, Repository repository, Kernel kernel)
  {
    this.kernel         =kernel;
    this.repository     =repository;
    this.defaultParentId=defaultParentId;
  }

  //----------------------------------------
  // RTTI

  public static final GBeanInfo GBEAN_INFO;

  static
  {
    GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(SPRConfigBuilder.class, NameFactory.CONFIG_BUILDER);
    infoFactory.addAttribute("defaultParentId", URI.class, true);
    infoFactory.addReference("Repository", Repository.class);
    infoFactory.addAttribute("kernel", Kernel.class, false);
    infoFactory.addInterface(ConfigurationBuilder.class);
    infoFactory.setConstructor(new String[]{"defaultParentId", "Repository", "kernel"});

    GBEAN_INFO = infoFactory.getBeanInfo();
  }

  public static GBeanInfo getGBeanInfo() {return GBEAN_INFO;}

  //----------------------------------------

  public Object
    getDeploymentPlan(File planFile, JarFile sprFile)
    throws DeploymentException
  {
    if (sprFile==null || !sprFile.getName().endsWith(".spr")) return null;

    log.info("Planning: "+sprFile.getName());

    // N.B.
    // - we should check that META-INF/spring.xml exists here (we can't really validate it)
    // - we should check out META-INF/geronimo-spring.xml
    // - could we inject stuff about environment into BeanFactory ?

    return this;		// token passed to buildConfiguration()...
  }

  public List
    buildConfiguration(Object plan, JarFile sprFile, File outfile)
    throws IOException, DeploymentException
  {
    if (!(plan instanceof SPRConfigBuilder)) // hacky...
      return null;

    log.info("Building: "+sprFile.getName());

    String moduleId="Spring-App-1"; // what should this look like ?

    SPRContext ctx=null;
    try
    {
      URI configId=new URI(sprFile.getName());	// could be overridden in META-INF/geronimo-spring.xml
      URI parentId=defaultParentId; // could be overridden in META-INF/geronimo-spring.xml
      URI configPath=new URI(defaultConfigPath);

      ctx=new SPRContext(outfile, configId, ConfigurationModuleType.SPR, parentId, kernel);

      // set up classpath and files that we want available in final
      // distribution...
      List classPath=new ArrayList();
      classPath.add(new URI("."));

      for (Enumeration e=sprFile.entries(); e.hasMoreElements();)
      {
       	ZipEntry entry = (ZipEntry) e.nextElement();
       	String name=entry.getName();
       	ctx.addFile(URI.create(name), sprFile, entry);

	if (name.endsWith(".jar"))
	  classPath.add(new URI(name));
      }

      // now we can get ClassLoader...
      //ClassLoader cl=ctx.getClassLoader(repository);

      // managed Object for this Spring Application
      {
	ObjectName name=new ObjectName("geronimo.config", "name", sprFile.getName());
	GBeanData gbeanData=new GBeanData(name, SpringApplicationImpl.GBEAN_INFO);
	ctx.addGBean(gbeanData);
      }

      // the actual Application...
      {
	ObjectName name=new ObjectName("geronimo.spring", "name", sprFile.getName());
	GBeanData gbeanData=new GBeanData(name, SpringGBean.GBEAN_INFO);
	gbeanData.setAttribute("classPath", classPath.toArray(new URI[classPath.size()]));
	gbeanData.setAttribute("configPath", configPath);
	ctx.addGBean(gbeanData);
      }
    }
    catch (Exception e)
    {
      throw new DeploymentException(e);
    }
    finally
    {
      if (ctx!=null) ctx.close();
    }

    return Collections.singletonList(moduleId);
  }
}
