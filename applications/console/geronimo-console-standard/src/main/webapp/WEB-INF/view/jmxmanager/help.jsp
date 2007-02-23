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

<%@ page language="java" contentType="text/html; charset=ISO-8859-1" pageEncoding="ISO-8859-1"%>

<!-- JMX Manager portlet help page -->

<p><center><b>JMX Viewer Portlet Help</b></center></p>

<p><b>JMX Tree</b></p>

<ul>
    <li>You can use this tree to view the different types of MBeans</li>
    <li>Each MBean will be represented as a tree node that shows its object name representation</li>
    <li>'All MBeans' will give you a list of MBeans grouped by its domain</li>
    <li>'J2EE MBeans' will give you a list of MBeans grouped by JSR 77 MBean types:</li>
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
    <li>'Geronimo MBeans' will give you a list of MBeans grouped by Geronimo specific MBean types:</li>
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
    <li>'Search MBeans' will give you a list of MBeans matching a pattern from the 'Search' tab</li>
</ul>

<p><b>Attributes Tab</b></p>

<ul>
    <li>Lists the MBean attributes</li>
    <li>Column headers can be clicked to sort by column</li>
    <li>Each table row can be mark by clicking it for easier viewing</li>
    <li>It's possible to set an attribute's value by clicking the Setter button. Results will be displayed.</li>
    <li><b>Note:</b> Be careful when setting an MBean's attribute value because it might affect how the server runs.</li>
</ul>

<p><b>Operations Tab</b></p>

<ul>
    <li>Lists the MBean operations</li>
    <li>You can execute an MBean operation by providing the different parameter values and clicking the operation button. Results will be displayed.</li>
    <li><b>Note:</b> Be careful when invoking an MBean's operation because it might affect how the server runs.</li>
</ul>

<p><b>Info Tab</b></p>

<ul>
    <li>Lists the MBean basic information:</li>
    <ul>
        <li>Abstract Name
        <li>Object Name
        <li>Class Name
        <li>Domain
        <li>J2EE Type
    </ul>
</ul>

<p><b>Stats Tab</b></p>

<ul>
    <li>Lists the statistics provided by a managed object. Statistics can be any of the following types:</li>
    <ul>
        <li>Count Statistic - specifies standard count measurements
        <li>Time Statistic - specifies standard timing measurements for a given operation
        <li>Boundary Statistic - specifies standard measurements of the upper and lower limits of the value of an attribute
        <li>Range Statistic - specifies standard measurements of the lowest and highest values an attribute has held as well as its current value
        <li>Bounded Range Statistic - provides standard measurements of a range that has fixed limits
    </ul>
</ul>

<p><b>Search Tab</b></p>

<ul>
    <li>You can use this tab to query MBeans matching a particular pattern</li>
    <li>Matching MBeans will be added under the 'Search MBeans' tree node. You might need to scroll down the JMX tree to view results.</li>
    <li>Examples of Object Name Patterns:</li>
    <ul>
        <li>"*:*" - will return all the MBeans
        <li>"geronimo:*" - will return all MBeans with "geronimo" domain
        <li>"*:j2eeType=GBean,*" - will return all GBeans
    </ul>
</ul>
