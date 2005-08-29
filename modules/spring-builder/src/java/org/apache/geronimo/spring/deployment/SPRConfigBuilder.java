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
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.ConfigurationBuilder;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.config.ConfigurationData;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.spring.SpringApplicationImpl;
import org.apache.geronimo.spring.SpringGBean;

/**
 * @version $Rev: 126313 $ $Date: 2005-01-24 21:03:52 +0000 (Mon, 24 Jan 2005) $
 */
public class SPRConfigBuilder
  implements ConfigurationBuilder
{
  protected static final Log    _log=LogFactory.getLog(SPRConfigBuilder.class);
  protected static final String _defaultConfigPath="META-INF/spring.xml";

  protected final Kernel     _kernel;
  protected final Repository _repository;
  protected final URI[]        _defaultParentId;

  public
    SPRConfigBuilder(URI[] defaultParentId, Repository repository, Kernel kernel)
  {
    _kernel         =kernel;
    _repository     =repository;
    _defaultParentId=defaultParentId;
  }

  //----------------------------------------
  // RTTI

  public static final GBeanInfo GBEAN_INFO;

  static
  {
    GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(SPRConfigBuilder.class, NameFactory.CONFIG_BUILDER);
    infoFactory.addAttribute("defaultParentId" , URI[].class, true);
    infoFactory.addReference("Repository"      , Repository.class, NameFactory.GERONIMO_SERVICE);
    infoFactory.addAttribute("kernel"          , Kernel.class, false);
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

    _log.info("Planning: "+sprFile.getName());

    // N.B.
    // - we should check that META-INF/spring.xml exists here (we can't really validate it)
    // - we should check out META-INF/geronimo-spring.xml
    // - could we inject stuff about environment into BeanFactory ?

    return this;		// token passed to buildConfiguration()...
  }

    public URI getConfigurationID(Object plan, JarFile sprFile) throws IOException, DeploymentException {
        String uid=sprFile.getName();
        try {
            return new URI(uid);
        } catch (URISyntaxException e) {
            throw new DeploymentException("Unable to construct configuration ID "+sprFile.getName());
        }
    }

  public ConfigurationData
    buildConfiguration(Object plan, JarFile sprFile, File outfile)
    throws IOException, DeploymentException
  {
    if (!(plan instanceof SPRConfigBuilder)) // hacky...
      return null;

    String uid=sprFile.getName(); // should be overrideable in geronimo-spring.xml

    _log.info("Building: "+uid);

    SPRContext ctx=null;
    try
    {
      URI configId=new URI(sprFile.getName());	// could be overridden in META-INF/geronimo-spring.xml
      URI[] parentId=_defaultParentId; // could be overridden in META-INF/geronimo-spring.xml
      URI configPath=new URI(_defaultConfigPath);

      ctx=new SPRContext(outfile, configId, ConfigurationModuleType.SPR, parentId, _kernel);

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
      //ClassLoader cl=ctx.getClassLoader(_repository);

      // managed Object for this Spring Application
      {
	ObjectName name=new ObjectName("geronimo.config", "name", uid);
	GBeanData gbeanData=new GBeanData(name, SpringApplicationImpl.GBEAN_INFO);
	ctx.addGBean(gbeanData);
      }

      // the actual Application...
      {
	Hashtable props=new Hashtable();
        props.put("J2EEServer"      , "geronimo");
        props.put("J2EEApplication" , "null");
        props.put("j2eeType"        , "SpringModule");
        props.put("name"            , uid);
	ObjectName on=new ObjectName("geronimo.server", props);
	GBeanData gbeanData=new GBeanData(on, SpringGBean.GBEAN_INFO);
        gbeanData.setAttribute("classPath"  , classPath.toArray(new URI[classPath.size()]));
        gbeanData.setAttribute("configPath" , configPath);
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

    return ctx.getConfigurationData();
  }
}
