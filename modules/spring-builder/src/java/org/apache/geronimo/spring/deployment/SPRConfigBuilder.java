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
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.deployment.DeploymentContext;
import org.apache.geronimo.deployment.util.NestedJarFile; // could this be useful ?
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Repository;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.core.io.ClassPathResource;

/**
 * @version $Rev: 126313 $ $Date: 2005-01-24 21:03:52 +0000 (Mon, 24 Jan 2005) $
 */
public class SPRConfigBuilder
  implements ConfigurationBuilder
{
  private final Kernel     kernel;
  private final Repository repository;
  private final URI        defaultParentId;

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

    System.out.println("PLANNING: "+sprFile.getName());

    // N.B.
    // - we should check out META-INF/geronimo-spring.xml
    // - we can't validate META-INF/spring.xml, as it could include stuff and use bad classes etc..
    // - could we inject stuff about environment into BeanFactory ?

    DefaultListableBeanFactory dlbf=new DefaultListableBeanFactory();
    XmlBeanDefinitionReader xbdr=new XmlBeanDefinitionReader(dlbf);

    // we should check that META-INF/spring.xml exists here (we can't really validate it)
    // we could load and check META-INF/geronimo-spring.xml here

    return xbdr;
  }

  public List
    buildConfiguration(Object plan, JarFile sprFile, File outfile)
    throws IOException, DeploymentException
  {
    if (!(plan instanceof XmlBeanDefinitionReader))
      return null;

    XmlBeanDefinitionReader xbdr=(XmlBeanDefinitionReader)plan;

    System.out.println("BUILDING: "+sprFile.getName());

    try
    {
      URI configId=new URI(sprFile.getName());	// could be overridden in META-INF/geronimo-spring.xml
      URI parentId=defaultParentId; // could be overridden in META-INF/geronimo-spring.xml

      SPRContext ctx=new SPRContext(outfile,
				    configId,
				    ConfigurationModuleType.SPR,
				    parentId,
				    kernel);

      // not sure of the purpose behind this - nested jars ?
      for (Enumeration e=sprFile.entries(); e.hasMoreElements();)
      {
	ZipEntry entry = (ZipEntry) e.nextElement();
	ctx.addFile(URI.create(entry.getName()), sprFile, entry);
      }

      // set up classpath
      ctx.addIncludeAsPackedJar(new URI("/tmp/foo.spr"), sprFile); // what should we pass for targetPath ?
      // now we can get ClassLoader...
      ClassLoader cl=ctx.getClassLoader(repository);

      xbdr.setBeanClassLoader(cl);
      xbdr.loadBeanDefinitions(new ClassPathResource("META-INF/spring.xml", cl));

      // force lazy construction of every bean described...
      DefaultListableBeanFactory dlbf=(DefaultListableBeanFactory)xbdr.getBeanFactory();
      String[] ids=dlbf.getBeanDefinitionNames();
      for (int i=ids.length; i>0; i--)
	dlbf.getBean(ids[i-1]);

      ObjectName name=new ObjectName("geronimo.config", "name", sprFile.getName());
      GBeanData gbeanData = new GBeanData(name, org.apache.geronimo.j2ee.management.impl.SpringApplicationImpl.GBEAN_INFO);

      // setting 'kernel' results in a NotSerializableException: after
      // returning from this method...

      // gbeanData.setAttribute("kernel", kernel);
      // gbeanData.setReferencePattern("objectName", name);

      ctx.addGBean(gbeanData);
      ctx.close();
    }
    catch (Exception e)
    {
      e.printStackTrace();
      throw new DeploymentException(e);
    }

    return new LinkedList(); // what should we return in this list ?
  }
}
