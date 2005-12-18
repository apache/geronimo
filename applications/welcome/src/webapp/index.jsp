<%--
    Copyright 2005 The Apache Software Foundation

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
--%>
<!-- $Rev$ $Date$ -->
<!doctype html public "-//w3c//dtd html 4.0 transitional//en" "http://www.w3.org/TR/REC-html40/strict.dtd">
<%@ page session="false" %>
<html>
    <head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <title>Apache Geronimo</title>
    <link rel="stylesheet" href="main.css" type="text/css"/>
</head>

<body>

<!-- Header -->
<table width="100%">
  <tr>
    <td>
      <table width="100%" height="86"  border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td height="86" class="WelcomeLogo" border="0"></td>
          <td height="86" class="Top"  border="0">&nbsp;</TD>
        </tr>
        <tr>
          <td algin="right" border="0">&nbsp;</td>
          <td align="right" border="0"><b><%= application.getServerInfo() %>&nbsp;&nbsp;&nbsp;&nbsp;</b></td>
        </tr>
      </table>
    </td>
  </tr>
</table>

<br />

<table>
    <tr>

        <!-- Table of Contents -->
        <td valign="top">
            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td bgcolor="#5FA3D6" bordercolor="#000000" align="left" nowrap>
                        <font face="Verdana" size="+1">Documentation&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" bordercolor="#000000" nowrap>
                        <a href="http://geronimo.apache.org/faq.html">FAQ</a><br />
                        <a href="http://geronimo.apache.org/redirects/wiki.html">Wiki</a><br />
                        <a href="http://geronimo.apache.org/documentation.html">Geronimo Documentation</a><br />
                        <a href="http://geronimo.apache.org/redirects/additionalDocumentation.html">Additional Documentation</a><br />
                        &nbsp;
                    </td>
                </tr>
            </table>
            <br />
            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td bgcolor="#5FA3D6" bordercolor="#000000" align="left" nowrap>
                        <font face="Verdana" size="+1">Geronimo Online&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" bordercolor="#000000" nowrap>
                        <a href="http://geronimo.apache.org/">The Geronimo Home Page</a><br />
                        <a href="http://geronimo.apache.org/redirects/issues.html">Problem Tracking Database</a><br />
                        <a href="http://geronimo.apache.org/redirects/userMailingList.html">Users Mailing List Archive</a><br />
                        <a href="http://geronimo.apache.org/redirects/developerMailingList.html">Developers Mailing List Archive</a><br />
                        <a href="irc://irc.freenode.net/#geronimo">Geronimo IRC chat</a><br />
                        &nbsp;
                    </td>
                </tr>
            </table>

            <br />
            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td bgcolor="#5FA3D6" bordercolor="#000000" align="left" nowrap>
                        <font face="Verdana" size="+1">Geronimo Examples&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" bordercolor="#000000" nowrap>
                        <a href="/servlets-examples/">Servlet Examples </a><br />
                        <a href="/jsp-examples/">JSP Examples </a><br />
                        <a href="/ldap-demo/">LDAP Demo</a><br />
                        <a href="http://geronimo.apache.org/redirects/additionalSamples.html">Additional Samples</a><br />
                    </td>
                </tr>
            </table>
        </td>

        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

        <!-- Body -->
        <td align="left" valign="top">
            <p><center><b>If you're seeing this page via a web browser, it means you've setup<br />
            Apache Geronimo&#8482; successfully. Congratulations!</b></center></p>

            <p>As you may have guessed by now, this is the default home page for Geronimo. If you're seeing this page,
                and you don't think you should be, then either you're either a user who has arrived at new installation
                of Geronimo, or you're an administrator who hasn't got his/her setup quite right.  If you need help
                setting up or administering Geronimo, see the
                <a href="http://geronimo.apache.org/documentation.html">Geronimo Documentation</a>.</p>

            <p>If you'd like to get started configuring Geronimo and the applications and services available
              in Geronimo, you might want to start with the Geronimo
              <a href="/console">Admin Console</a> (if this is your first time using Geronimo, the admin username
              is probably "system" with password "manager", though if you used the installer you got to choose
              your own during the installation).</p>

            <div style="margin-left: 50px; margin-right: 50px; padding: 10px; background-color:#eee">
                <p><b>Would you like your application to appear at this URL?</b><br />
                To set the context root for a web application, you can write a Geronimo deployment
                plan that uses the <tt>context-root</tt> element to specify the URL prefix used to
                reach that application.  If you specify a context root of <tt>&quot;/&quot;</tt> then the
                application will appear at this URL.  However, you'll need to stop this small
                Welcome application first!</p>
                <p>Your web deployment plan should look like this, and you can either pack it into
                the WAR at <tt>WEB-INF/geronimo-web.xml</tt> or provide it as a separate argument
                to the deploy tool.</p>
<pre>&lt;web-app xmlns="http://geronimo.apache.org/xml/ns/j2ee/web-1.0"
         configId="MyApplication"&gt;
    &lt;context-root&gt;/&lt;/context-root&gt;
    &lt;context-priority-classloader&gt;false&lt;/context-priority-classloader&gt;
&lt;/web-app&gt;</pre>
                <p>Then you can stop this application and deploy yours from the <a href="/console">Admin Console</a> or from the command line with a sequence of commands like this:</p>
                <% boolean jetty = application.getServerInfo().toLowerCase().indexOf("jetty") > -1; %>
<pre>java -jar bin/deployer.jar stop geronimo/welcome-<% if(jetty) {%>jetty<%} else {%>tomcat<%}%>/1.0/car
java -jar bin/deployer.jar deploy MyWebApp.war</pre>
            </div>

            <p>Geronimo mailing lists are available at the Geronimo project web site:</p>

           <ul>
               <li><b><a href="mailto:user-subscribe@geronimo.apache.org">user@geronimo.apache.org</a></b> for general questions related to configuring and using Geronimo</li>
               <li><b><a href="mailto:dev-subscribe@geronimo.apache.org">dev@geronimo.apache.org</a></b> for developers working on Geronimo</li>
           </ul>

            <p>Thanks for using Geronimo!</p>

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
                        <td><i>Optional</i>; JMX debugging web application</td>
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


            <p align="right"><font size=-1>
            <img src="<%=request.getContextPath()%>/images/powered_by_100x30.gif"/>
            </font><br />
            &nbsp;
            <font size=-1>Copyright &copy; 1999-2005 Apache Software Foundation</font><br />
            <font size=-1>All Rights Reserved</font> <br />
            &nbsp;</p>
            <p align="right">&nbsp;</p>

        </td>

    </tr>
</table>

</body>
</html>
