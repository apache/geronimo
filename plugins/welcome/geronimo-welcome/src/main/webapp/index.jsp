<!doctype html>
<%@ page language="java" contentType="text/html; charset=UTF-8" session="false" %>
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
<!-- $Rev$ $Date$ -->

<html lang="en">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Apache Geronimo</title>
<link rel="SHORTCUT ICON" href="<%=request.getContextPath()%>/favicon.ico" type="image/x-icon"/>
<link rel="stylesheet" href="main.css" type="text/css"/>
<link rel="stylesheet" href="pluto.css" type="text/css"/>
</head>

<body style="margin:0px">

<!-- Header -->
<table class="Banner" cellpadding="0" cellspacing="0" border="0">
    <tr>
        <td class="Logo" border="0">
        Welcome using Geronimo! (<%=application.getServerInfo()%> integrated)
        </td>
    </tr>
</table>

<table width="100%" border="0" cellspacing="0" cellpadding="10">
    <tr>
        <!-- Table of Contents -->
        <td valign="top" width="300px">
            
            <table width="100%" class="DarkTableLine">
                <tr>
                    <td class="ReallyDarkBackground" nowrap>
                        Administration
                    </td>
                </tr>
                <tr>
                    <td class="MediumBackground" nowrap>
                        &nbsp;-&nbsp;<a href="/console">Console</a><br />
                    </td>
                </tr>
            </table>
            <br />
            
            <table width="100%" class="DarkTableLine">
                <tr>
                    <td class="ReallyDarkBackground" nowrap>
                        Documentation
                    </td>
                </tr>
                <tr>
                    <td class="MediumBackground" nowrap>
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/redirects/faq.html">FAQ</a><br />
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/redirects/wiki.html">Wiki</a><br />
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/documentation.html">Geronimo Documentation</a><br />
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/redirects/additionalDocumentation.html">Additional Documentation</a><br />
                    </td>
                </tr>
            </table>
            <br />
            
            <table width="100%" class="DarkTableLine">
                <tr>
                    <td class="ReallyDarkBackground" nowrap>
                        Geronimo Online
                    </td>
                </tr>
                <tr>
                    <td class="MediumBackground" nowrap>
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/">The Geronimo Home Page</a><br />
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/redirects/issues.html">Problem Tracking Database</a><br />
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/redirects/userMailingList.html">Users Mailing List Archive</a><br />
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/redirects/developerMailingList.html">Developers Mailing List Archive</a><br />
                        &nbsp;-&nbsp;<a href="irc://irc.freenode.net/#geronimo">Geronimo IRC chat</a><br />
                    </td>
                </tr>
            </table>
            <br />
            
            <table width="100%" class="DarkTableLine">
                <tr>
                    <td class="ReallyDarkBackground" nowrap>
                        Geronimo Examples
                    </td>
                </tr>
                <tr>
                    <td class="MediumBackground" nowrap>
                        <!--<a href="/servlets-examples/">Servlet Examples </a><br />-->
                        <!--<a href="/jsp-examples/">JSP Examples </a><br />-->
                        <!--<a href="/ldap-demo/">LDAP Demo</a><br />-->
                        &nbsp;-&nbsp;<a href="http://geronimo.apache.org/redirects/additionalSamples.html">Geronimo Sample Applications</a><br />
                    </td>
                </tr>
            </table>
            
            <br />
            <br />
            <img src="<%=request.getContextPath()%>/images/powered_by_100x30.gif" alt="Powered by Geronimo"/><br />
            Copyright &copy; 2003-2011 <br />
            Apache Software Foundation <br />
            All Rights Reserved <br />

        </td>


        <!-- Body -->
        <td align="left" valign="top">
            <p><center><b>If you're seeing this page via a Web browser, it means you've setup<br />
            Apache Geronimo&#8482; successfully. Congratulations!</b></center></p>

            <p>As you may have guessed by now, this is the default home page for Geronimo. If you're seeing this page,
                and you don't think you should be, then either you're a user who has arrived at new installation
                of Geronimo, or you're an administrator who hasn't got his/her setup quite right.  If you need help
                setting up or administering Geronimo, see the
                <a href="http://geronimo.apache.org/documentation.html">Geronimo Documentation</a>.</p>

            <p>If you'd like to get started configuring Geronimo and the applications and services available
              in Geronimo, you might want to start with the Geronimo
              <a href="/console">Admin Console</a> (if you have not yet configured this the admin username
              is "system" with password "manager").</p>

            <div style="margin-left: 50px; margin-right: 50px; padding: 10px; background-color:#eee">
                <p><b>Would you like your application to appear at this URL?</b><br />
                To set the context root for a Web application, you can write a Geronimo deployment
                plan that uses the <tt>context-root</tt> element to specify the URL prefix used to
                reach that application.  If you specify a context root of <tt>&quot;/&quot;</tt> then the
                application will appear at this URL.  However, you'll need to stop this small
                Welcome application first!</p>
                <p>Your Web deployment plan should look like this, and you can either pack it into
                the WAR at <tt>WEB-INF/geronimo-web.xml</tt> or provide it as a separate argument
                to the deploy tool.</p>
<pre>&lt;web-app xmlns="http://geronimo.apache.org/xml/ns/j2ee/web-2.0" 
        xmlns:dep="http://geronimo.apache.org/xml/ns/deployment-1.2"&gt;

    &lt;dep:environment&gt;
        &lt;dep:moduleId&gt;
            &lt;dep:groupId&gt;com.MyCompany&lt;/dep:groupId&gt;
            &lt;dep:artifactId&gt;MyWebApp&lt;/dep:artifactId&gt;
            &lt;dep:version&gt;1.0&lt;/dep:version&gt;
        &lt;/dep:moduleId&gt;
    &lt;/dep:environment&gt;
    &lt;context-root&gt;/&lt;/context-root&gt;
&lt;/web-app&gt;</pre>
                <p>Then you can stop this application and deploy yours from the <a href="/console">Admin Console</a> or from the command line with a sequence of commands like this:</p>
                <% boolean jetty = application.getServerInfo().toLowerCase().indexOf("jetty") > -1; %>
<pre>deploy.[bat|sh] stop org.apache.geronimo.configs/welcome-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/{geronimoVersion}/car
deploy.[bat|sh] deploy MyWebApp-1.0.war</pre>
            </div>

            <p>Geronimo mailing lists are available at the Geronimo project Web site:</p>

           <ul>
               <li><b><a href="mailto:user-subscribe@geronimo.apache.org">user@geronimo.apache.org</a></b> for general questions related to configuring and using Geronimo</li>
               <li><b><a href="mailto:dev-subscribe@geronimo.apache.org">dev@geronimo.apache.org</a></b> for developers working on Geronimo</li>
           </ul>

            
            <%-- Needs to be updated if we want to keep using it
            <div style="margin-left: 50px; margin-right: 50px; padding: 10px; background-color:#eee">
                <p><b>Would you like a slimmer Geronimo installation?</b><br />
                 Geronimo ships with a number of sample applications and demonstration services
                 running.  The table below lists the default configurations and whether they're
                 required or optional for a J2EE application server configuration.</p>
                <p>Optional configurations can be disabled from the <a href="/console">Admin Console</a> or by running a command like this:</p>
                <pre>java -jar bin/deployer.jar stop <i>configuration-name</i></pre>
                <table border="1">
                    <tr><th>Name</th><th>Comments</th></tr>
                    <tr><th colspan="2">Application EARs (<a href="/console/portal/apps/apps_ear">list</a>)</th></tr>
                    <tr>
                        <td>geronimo/daytrader-derby-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><i>Optional</i>; day trader sample application</td>
                    </tr>
                    <tr>
                        <td>geronimo/uddi-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><i>Optional</i>; JAXR/UDDI repository</td>
                    </tr>
                    <tr>
                        <td>geronimo/webconsole-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><b>Recommended</b>; Web management console</td>
                    </tr>
                    <tr><th colspan="2">Web Application WARs (<a href="/console/portal/apps/apps_war">list</a>)</th></tr>
                    <tr>
                        <td>geronimo/jmxdebug-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><i>Optional</i>; JMX debugging Web application</td>
                    </tr>
                    <tr>
                        <td>geronimo/jsp-examples-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><i>Optional</i>; JSP examples</td>
                    </tr>
                    <tr>
                        <td>geronimo/ldap-demo-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><i>Optional</i>; Directions and tests for sample LDAP security realm</td>
                    </tr>
                    <tr>
                        <td>geronimo/remote-deploy-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><b>Recommended</b>; support for running deployment commands from a remote machine</td>
                    </tr>
                    <tr>
                        <td>geronimo/servlets-examples-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><i>Optional</i>; Servlet examples</td>
                    </tr>
                    <tr>
                        <td>geronimo/welcome-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car</td>
                        <td><i>Optional</i>; this welcome site</td>
                    </tr>
                    <tr><th colspan="2">J2EE Connector RARs (<a href="/console/portal/apps/apps_rar">list</a>)</th></tr>
                    <tr>
                        <td>geronimo/activemq/1.0/car</td>
                        <td><b>Required</b>; Standard ActiveMQ configuration and connection factories</td>
                    </tr>
                    <tr>
                        <td>geronimo/system-database/1.0/car</td>
                        <td><b>Required</b>; Embedded Derby database supporting key system functions</td>
                    </tr>
                    <tr><th colspan="2">Application Client JARs (<a href="/console/portal/apps/apps_client">list</a>)</th></tr>
                    <tr>
                        <td>geronimo/daytrader-derby-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>-streamer-client/1.0/car</td>
                        <td><i>Optional</i>; Client for daytrader sample application</td>
                    </tr>
                    <tr><th colspan="2">System Modules (<a href="/console/portal/apps/apps_system">list</a>)</th></tr>
                    <tr>
                        <td>geronimo/directory/1.0/car</td>
                        <td><i>Optional</i>; Embedded Apache Directory LDAP server</td>
                    </tr>
                    <tr>
                        <td>geronimo/ldap-realm/1.0/car</td>
                        <td><i>Optional</i>; Sample security realm using embedded LDAP server</td>
                    </tr>
                    <tr>
                        <td>geronimo/hot-deployer/1.0/car</td>
                        <td><b>Recommended</b>; Supports hot deployment for </td>
                    </tr>
                    <tr>
                        <td>geronimo/j2ee-corba/1.0/car</td>
                        <td><i>Optional</i>; Support for code in Geronimo acting as a CORBA client or server</td>
                    </tr>
                    <tr>
                        <td>geronimo/javamail/1.0/car</td>
                        <td><i>Optional</i>; Supports JavaMail resources</td>
                    </tr>
                    <tr>
                        <td><i>Other System Modules</i></td>
                        <td><b>Do Not Change</b>; The other system configurations that are running should not
                          be stopped, and the other system configurations that are not running should not be
                          started.</td>
                    </tr>
                </table>
            </div>
            --%>


        </td>
    </tr>
</table>

</body>
</html>
