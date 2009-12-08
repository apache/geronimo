<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<div class="portlet-section-header">Page Adminstrator Portlet Help</div>

<p class="portlet-font">
The Page Administrator Portlet is used to add and remove portlets from portal pages. The current
version of this application does not deploy the portlet application war or persist page
configurations that are held in pluto-portal-driver-config.xml.
</p>

<div class="portlet-section-subheader">Configuring a Portlet Application</div>
<p class="portlet-font">
The portlet application needs to be bundled in a war file as per the Java Portlet Specification. 
The war also needs to include proper PortletServlet servlet and servlet-mapping records in WEB-INF/web.xml.
An assembly process has been developed to add these records to web.xml using Maven 2 (the
pluto:assemble goal in maven-pluto-plugin) or Ant (AssembleTask). 
See the testsuite web.xml file for an example how the servlet and servlet-mapping
records should look like after assembly (other items that the developer adds to web.xml should be 
carried forward into the updated file). 
</p>

<p class="portlet-font">
A custom portlet war deployed into the bundled distribution of Pluto also needs a Tomcat context descriptor 
with the same name as the portlet app war name (a.k.a. context name). This context file needs to be 
in the META-INF directory of the war file. Here's an example of one for a portlet bundled for a 
HelloWorldPortlet context (file name is HelloWorldPortlet.xml): 
<pre>
&lt;Context path="/HelloWorldPortlet" docBase="HelloWorldPortlet" crossContext="true"/&gt; 
</pre>  
The crossContext attribute allows Pluto, which is deployed in its own Tomcat context, to work with this custom portlet. 
</p>

<div class="portlet-section-subheader">Deploying a Portlet Application</div>
<p class="portlet-font">
    The way to deploy a portlet application depends on the app server that Pluto is running in. In the bundled distribution
    Pluto is deployed in Tomcat. In this case, you can use the Tomcat manager app to deploy the portlet
    war. There is a 'Upload and deploy portlet war' link at the bottom of the Page Administrator portlet that points to 
    the manager app    in the bundled distribution (this link can be changed for other app servers -- see the 
    adjacent Help link). In the bundled distribution of Pluto, you can also simply drop the portlet application war into Tomcat's 
    webapp directory.
</p>

<div class="portlet-section-subheader">Adding Portlets to a Portal Pages</div>
<p class="portlet-font">
To Add portlets to a portal page using the Pluto Page Administrator portlet, take the following steps:
Select a portal page using the Portal Pages drop-down. 
Select a portlet application using the Portlet Applications drop-down 
Select a portlet in the adjacent drop down. 
Click the Add Portlet button.
</p>

<div class="portlet-section-subheader">Removing Portal Pages</div>
<p class="portlet-font">
To remove portlets from a portal page, take the following steps:
Select a portal page using the Portal Pages drop-down.
Select the portlet in the adjacent list.
Click the Remove Page button.
</p>


<div class="portlet-section-subheader">Manually Updating the Configuration File</div>
<p class="portlet-font">
The Page Administrator Portlet does not persist any portlet additions or portal page removals to 
the pluto-portal-driver-config.xml file. This must be done manually in the config file. Removal of portlets
from pages must also be done manually in pluto-portal-driver-config.xml.
</p>

<p class="portlet-font">
To manually add a portlet to a page in pluto-portal-driver-config.xml, a portlet child element must be added to the 
page element, which is a child of render-config. This element should look like this:
<pre>
&lt;portlet context="/HelloWorldPortlet" name="HelloWorldPortlet"/&gt;
</pre>
</p>

<p class="portlet-font">
New portal pages can be created by adding a page element under render-config in pluto-portal-driver-config.xml. 
The uri attribute of the page element points to a JSP page that will contain a portlet (or portlets) defined in 
its child (or children) portlet element(s). The default 'theme' lays out the portlets in two columns 
(see WEB-INF\themes\pluto-default-theme.jsp in the pluto webapp for details). 
</p>


<%-- Properties for link to app server deployer and help mode file --%>
<fmt:bundle basename="AdminPortlet">
    <fmt:message key="appserver.deployer.help.page" var="deployerHelp"/>
</fmt:bundle> 
<portlet:renderURL portletMode="help" var="deployerhelpURL">
    <portlet-el:param name="helpPage" value="${deployerHelp}"/>
</portlet:renderURL>

<p class="portlet-font">
<a href='<c:out value="${deployerhelpURL}"/>'>Upload and Deployment in App Server Help</a>
</p>

<p class="portlet-font">
<a href='<portlet:renderURL portletMode="view"/>'>Page Administrator Portlet</a> 
</p>
