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

package org.apache.geronimo.tomcat;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.KernelFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.security.SecurityServiceImpl;
import org.apache.geronimo.security.deploy.Principal;
import org.apache.geronimo.security.jaas.GeronimoLoginConfiguration;
import org.apache.geronimo.security.jaas.JaasLoginModuleUse;
import org.apache.geronimo.security.jaas.JaasLoginService;
import org.apache.geronimo.security.jaas.LoginModuleGBean;
import org.apache.geronimo.security.realm.GenericSecurityRealm;
import org.apache.geronimo.system.serverinfo.BasicServerInfo;
import org.apache.geronimo.tomcat.app.MockWebServiceContainer;
import org.apache.geronimo.webservices.WebServiceContainer;

import sun.misc.BASE64Encoder;

/**
* @version $Rev$ $Date$
*/
public class ContainerTest extends TestCase {
   private ClassLoader cl = this.getClass().getClassLoader();
   private Kernel kernel;
   private GBeanData container;
   private ObjectName containerName;
   private Set containerPatterns;
   private ObjectName connectorName;
   private GBeanData connector;
   private ObjectName engineName;
   private GBeanData engine;
   private ObjectName hostName;
   private GBeanData host;
   private J2eeContext moduleContext = new J2eeContextImpl("tomcat.test", "test", "null", NameFactory.WEB_MODULE, "tomcatTest", null, null);
   private ObjectName serverInfoName;
   private GBeanData serverInfoGBean;
   private GBeanData securityServiceGBean;
   private ObjectName securityServiceName;
   private ObjectName loginServiceName;
   private GBeanData loginServiceGBean;
   private GBeanData loginConfigurationGBean;
   private ObjectName loginConfigurationName;
   private GBeanData propertiesLMGBean;
   private ObjectName propertiesLMName;
   private ObjectName propertiesRealmName;
   private GBeanData propertiesRealmGBean;


   public void testWebServiceHandler() throws Exception {

       setUpWeb();

       assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(connectorName));
       assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(containerName));

       String contextPath = "/foo/webservice.ws";
       MockWebServiceContainer webServiceInvoker = new MockWebServiceContainer();
       kernel.invoke(containerName, "addWebService", new Object[] {contextPath, webServiceInvoker, null, null, null, null, cl}, new String[] {String.class.getName(), WebServiceContainer.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), ClassLoader.class.getName()});

       HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080" + contextPath).openConnection();
       try {
           BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
           assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
           assertEquals("Hello World", reader.readLine());
       } finally {
           connection.disconnect();
       }
       kernel.invoke(containerName, "removeWebService", new Object[] {contextPath}, new String[] {String.class.getName()});
       connection = (HttpURLConnection) new URL("http://localhost:8080" + contextPath).openConnection();
       try {
           connection.getInputStream();
           fail();
       } catch (Exception e) {
           // see if we removed the ws.
           assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
           connection.disconnect();
       }
    
       tearDownWeb();
   }
   
   public void testSecureWebServiceHandler() throws Exception {

       setUpWeb();

       assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(connectorName));
       assertEquals(State.RUNNING_INDEX, kernel.getGBeanState(containerName));

       setUpSecurity();
       
       String contextPath = "/foo/webservice.ws";
       MockWebServiceContainer webServiceInvoker = new MockWebServiceContainer();
       kernel.invoke(containerName, "addWebService", new Object[] {contextPath, webServiceInvoker, "Geronimo", "Geronimo", "NONE", "BASIC",cl}, new String[] {String.class.getName(), WebServiceContainer.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), String.class.getName(), ClassLoader.class.getName()});

       //Veryify its secured
       HttpURLConnection connection = (HttpURLConnection) new URL("http://localhost:8080" + contextPath).openConnection();
       try {
           connection.getInputStream();
           fail();
       } catch (Exception e) {
           assertEquals(HttpURLConnection.HTTP_UNAUTHORIZED, connection.getResponseCode());
       } finally {
           connection.disconnect();
       }
       
       //Authenticate
       connection = (HttpURLConnection) new URL("http://localhost:8080" + contextPath).openConnection();
       String authentication = (new BASE64Encoder()).encode(("alan:starcraft").getBytes());
       connection.setRequestProperty("Authorization", "Basic " + authentication);
       try {
           BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
           assertEquals(HttpURLConnection.HTTP_OK, connection.getResponseCode());
           assertEquals("Hello World", reader.readLine());
       } finally {
           connection.disconnect();
       }
       kernel.invoke(containerName, "removeWebService", new Object[] {contextPath}, new String[] {String.class.getName()});
       connection = (HttpURLConnection) new URL("http://localhost:8080" + contextPath).openConnection();
       try {
           connection.getInputStream();
           fail();
       } catch (Exception e) {
           // see if we removed the ws.
           assertEquals(HttpURLConnection.HTTP_NOT_FOUND, connection.getResponseCode());
           connection.disconnect();
       }
       
       tearDownSecurity();
       tearDownWeb();
   }

   private void start(GBeanData instance) throws Exception {
       kernel.loadGBean(instance, cl);
       kernel.startGBean(instance.getName());
   }

   private void stop(ObjectName name) throws Exception {
       kernel.stopGBean(name);
       kernel.unloadGBean(name);
   }

   protected void setUpSecurity() throws Exception {

       loginConfigurationName = new ObjectName("geronimo.security:type=LoginConfiguration");
       loginConfigurationGBean = new GBeanData(loginConfigurationName, GeronimoLoginConfiguration.getGBeanInfo());
       Set configurations = new HashSet();
       configurations.add(new ObjectName("geronimo.server:j2eeType=SecurityRealm,*"));
       configurations.add(new ObjectName("geronimo.server:j2eeType=ConfigurationEntry,*"));
       loginConfigurationGBean.setReferencePatterns("Configurations", configurations);

       securityServiceName = new ObjectName("geronimo.server:j2eeType=SecurityService");
       securityServiceGBean = new GBeanData(securityServiceName, SecurityServiceImpl.GBEAN_INFO);
       securityServiceGBean.setReferencePattern("ServerInfo", serverInfoName);
       securityServiceGBean.setAttribute("policyConfigurationFactory", "org.apache.geronimo.security.jacc.GeronimoPolicyConfigurationFactory");
       securityServiceGBean.setAttribute("policyProvider", "org.apache.geronimo.security.jacc.GeronimoPolicy");

       loginServiceName = JaasLoginService.OBJECT_NAME;
       loginServiceGBean = new GBeanData(loginServiceName, JaasLoginService.GBEAN_INFO);
       loginServiceGBean.setReferencePattern("Realms", new ObjectName("geronimo.server:j2eeType=SecurityRealm,*"));
       loginServiceGBean.setAttribute("algorithm", "HmacSHA1");
       loginServiceGBean.setAttribute("password", "secret");

       propertiesLMName = new ObjectName("geronimo.security:type=LoginModule,name=Geronimo");
       propertiesLMGBean = new GBeanData(propertiesLMName, LoginModuleGBean.GBEAN_INFO);
       propertiesLMGBean.setAttribute("loginModuleClass", "org.apache.geronimo.security.realm.providers.PropertiesFileLoginModule");
       propertiesLMGBean.setAttribute("serverSide", Boolean.TRUE);
       Properties options = new Properties();
       options.setProperty("usersURI", "src/test-resources/data/users.properties");
       options.setProperty("groupsURI", "src/test-resources/data/groups.properties");
       propertiesLMGBean.setAttribute("options", options);
       propertiesLMGBean.setAttribute("loginDomainName", "geronimo-properties-realm");

       ObjectName testUseName = new ObjectName("geronimo.security:type=LoginModuleUse,name=properties");
       GBeanData lmUseGBean = new GBeanData(testUseName, JaasLoginModuleUse.getGBeanInfo());
       lmUseGBean.setAttribute("controlFlag", "REQUIRED");
       lmUseGBean.setReferencePattern("LoginModule", propertiesLMName);

       propertiesRealmName = new ObjectName("geronimo.server:j2eeType=SecurityRealm,name=geronimo-properties-realm");
       propertiesRealmGBean = new GBeanData(propertiesRealmName, GenericSecurityRealm.GBEAN_INFO);
       propertiesRealmGBean.setReferencePattern("ServerInfo", serverInfoName);
       propertiesRealmGBean.setAttribute("realmName", "Geronimo");
       propertiesRealmGBean.setReferencePattern("LoginModuleConfiguration", testUseName);
       Principal.PrincipalEditor principalEditor = new Principal.PrincipalEditor();
       principalEditor.setAsText("metro=org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal");
       propertiesRealmGBean.setAttribute("defaultPrincipal", principalEditor.getValue());

       start(loginConfigurationGBean);
       start(securityServiceGBean);
       start(loginServiceGBean);
       start(propertiesLMGBean);
       start(lmUseGBean);
       start(propertiesRealmGBean);

   }

   protected void tearDownSecurity() throws Exception {
       stop(propertiesRealmName);
       stop(propertiesLMName);
       stop(loginServiceName);
       stop(securityServiceName);
       stop(loginConfigurationName);
   }
   
   private void setUpWeb() throws Exception{
       containerName = NameFactory.getWebComponentName(null, null, null, null, "tomcatContainer", "WebResource", moduleContext);
       connectorName = NameFactory.getWebComponentName(null, null, null, null, "tomcatConnector", "WebResource", moduleContext);
       containerPatterns = new HashSet();
       containerPatterns.add(containerName);
       connectorName = new ObjectName("geronimo.tomcat:role=Connector");
       connectorName = NameFactory.getWebComponentName(null, null, null, null, "tomcatConnector", "WebResource", moduleContext);
       engineName = NameFactory.getWebComponentName(null, null, null, null, "tomcatEngine", "WebResource", moduleContext);
       hostName = NameFactory.getWebComponentName(null, null, null, null, "tomcatHost", "WebResource", moduleContext);
       kernel = KernelFactory.newInstance().createKernel("test.kernel");
       kernel.boot();
 
       //ServerInfo
       serverInfoName = new ObjectName("geronimo.system:role=ServerInfo");
       serverInfoGBean = new GBeanData(serverInfoName, BasicServerInfo.GBEAN_INFO);
       serverInfoGBean.setAttribute("baseDirectory", ".");
       start(serverInfoGBean);
       
       Map initParams = new HashMap();

       //Default Host
       initParams.clear();
       initParams.put("workDir","work");
       initParams.put("name","localhost");
       initParams.put("appBase","");
       host = new GBeanData(hostName, HostGBean.GBEAN_INFO);
       host.setAttribute("className", "org.apache.catalina.core.StandardHost");
       host.setAttribute("initParams", initParams);
       start(host);       

       //Default Engine
//       ReferenceCollection hosts = new TestReferenceCollection();
//       hosts.add(host);
       
       initParams.clear();
       initParams.put("name","Geronimo");
       initParams.put("defaultHost","localhost");
       engine = new GBeanData(engineName, EngineGBean.GBEAN_INFO);
       engine.setAttribute("className", "org.apache.geronimo.tomcat.TomcatEngine");
       engine.setAttribute("initParams", initParams);
       engine.setReferencePattern("Hosts", hostName);
       start(engine);

       container = new GBeanData(containerName, TomcatContainer.GBEAN_INFO);
       container.setAttribute("classLoader", cl);
       container.setAttribute("catalinaHome", "target/var/catalina");
       container.setReferencePattern("EngineGBean", engineName);
       container.setReferencePattern("ServerInfo", serverInfoName);
       start(container);

       connector = new GBeanData(connectorName, ConnectorGBean.GBEAN_INFO);
       connector.setAttribute("name", "HTTP");
       connector.setAttribute("port", new Integer(8080));
       connector.setReferencePattern("TomcatContainer", containerName);
       start(connector);
   }
   
   private void tearDownWeb() throws Exception {
       stop(connectorName);
       stop(containerName);
       stop(hostName);
       stop(engineName);
       stop(serverInfoName);
       kernel.shutdown();
   }

   private static class TestReferenceCollection extends ArrayList implements ReferenceCollection {

       ReferenceCollectionListener referenceCollectionListener;

       public void addReferenceCollectionListener(ReferenceCollectionListener listener) {
           this.referenceCollectionListener = listener;
       }

       public void removeReferenceCollectionListener(ReferenceCollectionListener listener) {
           this.referenceCollectionListener = null;
       }

       public boolean add(Object o) {
           boolean result = super.add(o);
           if (referenceCollectionListener != null) {
               referenceCollectionListener.memberAdded(new ReferenceCollectionEvent(null, o));
           }
           return result;
       }

       public boolean remove(Object o) {
           boolean result = super.remove(o);
           if (referenceCollectionListener != null) {
               referenceCollectionListener.memberRemoved(new ReferenceCollectionEvent(null, o));
           }
           return result;
       }

   }   
}
