<%--
 Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements.  See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License.  You may obtain a copy of the License at
 
      http://www.apache.org/licenses/LICENSE-2.0
 
  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="debugviews"/>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!-- JMX Manager portlet help page -->

<p><center><b><fmt:message key="keystore.jmxmanager.title"/></b></center></p>

<p><b><fmt:message key="jmxmanager.help.JMXTree"/></b></p>

<ul>
<fmt:message key="jmxmanager.help.JMXTreeExp1"/>
        <ul>
        <li>AppClientModule
        <li>EJBModule
        <li>EntityBean
        <li>J2EEApplication
        <li>J2EEDomain
        <li>J2EEServer
        <li>JavaMailResource
        <li>JCAConnectionFactory
        <li>JCAManagedConnectionFactory
        <li>JCAResource
        <li>JDBCDataSource
        <li>JDBCDriver
        <li>JDBCResource
        <li>JMSResource
        <li>JNDIResource
        <li>JTAResource
        <li>JVM
        <li>MessageDrivenBean
        <li>ResourceAdapter
        <li>ResourceAdapterModule
        <li>RMI_IIOPResource
        <li>Servlet
        <li>StatefulSessionBean
        <li>StatelessSessionBean
        <li>URLResource
        <li>WebModule
    </ul>
    <li><fmt:message key="jmxmanager.help.geronimoMBeansExp"/>:</li>
    <ul>
        <li>AppClient
        <li>ArtifactManager
        <li>ArtifactResolver
        <li>AttributeStore
        <li>ConfigBuilder
        <li>ConfigurationEntry
        <li>ConfigurationManager
        <li>ConfigurationStore
        <li>CORBABean
        <li>CORBACSS
        <li>CORBATSS
        <li>Deployer
        <li>DeploymentConfigurer
        <li>GBean
        <li>Host
        <li>JaasLoginService
        <li>JACCManager
        <li>JAXRConnectionFactory
        <li>JCAActivationSpec
        <li>JCAAdminObject
        <li>JCAConnectionManager
        <li>JCAConnectionTracker
        <li>JCAResourceAdapter
        <li>JCAWorkManager
        <li>JMSConnector
        <li>JMSPersistence
        <li>JMSServer
        <li>KeyGenerator
        <li>Keystore
        <li>LoginModule
        <li>LoginModuleUse
        <li>MEJB
        <li>ModuleBuilder
        <li>PersistentConfigurationList
        <li>RealmBridge
        <li>Repository
        <li>RoleMapper
        <li>SecurityRealm
        <li>ServiceModule
        <li>ServletTemplate
        <li>ServletWebFilterMapping
        <li>ServletWebServiceTemplate
        <li>SystemLog
        <li>TomcatValve
        <li>TransactionContextManager
        <li>TransactionLog
        <li>TransactionManager
        <li>URLPattern
        <li>URLWebFilterMapping
        <li>WebFilter
        <li>WSLink
        <li>XIDFactory
        <li>XIDImporter
        <li>XmlAttributeBuilder
        <li>XmlReferenceBuilder
    </ul>
    <li><fmt:message key="jmxmanager.help.searchMBeansExp"/></li>
</ul>

<p><b><fmt:message key="jmxmanager.help.attributesTab"/></b></p>

<fmt:message key="jmxmanager.help.attributesTabExp"/>


<p><b><fmt:message key="jmxmanager.help.operationsTab"/></b></p>

<fmt:message key="jmxmanager.help.operationsTabExp"/>


<p><b><fmt:message key="jmxmanager.help.infoTab"/></b></p>

<ul>
    <li><fmt:message key="jmxmanager.help.infoTabExp"/>:</li>
    <ul>
        <li><fmt:message key="jmxmanager.help.abstractName"/>
        <li><fmt:message key="jmxmanager.help.objectName"/>
        <li><fmt:message key="jmxmanager.help.className"/>
        <li><fmt:message key="jmxmanager.help.domain"/>
        <li><fmt:message key="jmxmanager.help.j2eeType"/>
    </ul>
</ul>

<p><b><fmt:message key="jmxmanager.help.statsTab"/></b></p>

<fmt:message key="jmxmanager.help.statsTabExp"/>


<p><b><fmt:message key="jmxmanager.help.searchTab"/></b></p>

<fmt:message key="jmxmanager.help.searchTabExp"/>

