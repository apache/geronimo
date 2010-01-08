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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>


<table style="width:100%"> <!-- an IE rendering fix -->
    <tr>

        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <p><font face="Verdana" size="+1"><center><b><fmt:message key="welcome.welcomeMaximized.title"/></b></center></font></p>

            <p><fmt:message key="welcome.welcomeNormal.about1"/></p>

            <p><fmt:message key="welcome.welcomeNormal.about2"/></p>

            <p><fmt:message key="welcome.welcomeNormal.about3"/></p>

            <p><fmt:message key="welcome.welcomeNormal.about4"/></p>

           <ul>
               <li><b><a href="mailto:user-subscribe@geronimo.apache.org">user@geronimo.apache.org</a></b>
                   (<a href="http://geronimo.apache.org/redirects/userMailingList.html"><fmt:message key="welcome.welcomeNormal.archive"/></a>)
                   <fmt:message key="welcome.welcomeNormal.forUsers"/></li>
               <li><b><a href="mailto:dev-subscribe@geronimo.apache.org">dev@geronimo.apache.org</a></b>
                   (<a href="http://geronimo.apache.org/redirects/developerMailingList.html"><fmt:message key="welcome.welcomeNormal.archive"/></a>)
                   <fmt:message key="welcome.welcomeNormal.forDevelopers"/></li>
           </ul>

            <p><fmt:message key="welcome.welcomeNormal.about5"/></p>

            <p style="text-align: center"><b><fmt:message key="welcome.welcomeNormal.thanks"/></b></p>


        </td>

        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

        <!-- Geronimo Links -->
        <td valign="top">
        <%if(request.isUserInRole("admin")){ %>

            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="welcome.welcomeNormal.commonActions"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <img src='/console/images/ico_list_16x16.gif' alt=""/><a href="/console/portal/3-8/Applications/Deploy New"><fmt:message key="welcome.welcomeNormal.deployNew"/></a><br />
                        <img src='/console/images/ico_servcomp_16x16.gif' alt=""/><a href="/console/portal/1-5/Server/Web Server"><fmt:message key="welcome.welcomeNormal.webServerPorts"/></a><br />
                        <img src='/console/images/ico_look_16x16.gif' alt=""/><a href="/console/portal/1-1/Server/Information"><fmt:message key="welcome.welcomeNormal.serverInfo"/></a><br />
                        &nbsp;<br />
                    </td>
                </tr>
            </table>
        <%} %>

            <br />
            <br />

            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="welcome.welcomeNormal.online"/></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <a href="http://geronimo.apache.org/"><fmt:message key="welcome.welcomeNormal.homePage"/></a><br />
                        <a href="http://geronimo.apache.org/redirects/issues.html"><fmt:message key="welcome.welcomeNormal.problemTracker"/></a><br />
                        <a href="http://geronimo.apache.org/redirects/userMailingList.html"><fmt:message key="welcome.welcomeNormal.userMaillist"/></a><br />
                        <a href="http://geronimo.apache.org/redirects/developerMailingList.html"><fmt:message key="welcome.welcomeNormal.devMaillist"/></a><br />
                        <a href="irc://irc.freenode.net/#geronimo"><fmt:message key="welcome.welcomeNormal.irc"/></a><br />
                        &nbsp;<br />
                    </td>
                </tr>
            </table>

            <br />
            <br />

            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><fmt:message key="welcome.welcomeNormal.doc"/>&nbsp;&nbsp;&nbsp;</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <a href="http://geronimo.apache.org/faq.html"><fmt:message key="welcome.welcomeNormal.faq"/></a><br />
                        <a href="http://geronimo.apache.org/redirects/wiki.html"><fmt:message key="welcome.welcomeNormal.wiki"/></a><br />
                        <a href="http://geronimo.apache.org/documentation.html"><fmt:message key="welcome.welcomeNormal.doc"/></a><br />
                        <a href="http://geronimo.apache.org/redirects/additionalDocumentation.html"><fmt:message key="welcome.welcomeNormal.addiDoc"/></a><br />
                        &nbsp;<br />
                    </td>
                </tr>
            </table>

            <br />
            <br />

            <p align="right"><font size=-1>
            <img src="/console/images/powered_by_100x30.gif" alt="Powered by Apache Geronimo"/>
            </font><br />
            &nbsp;
            <font size=-1>Copyright &copy; 2003-2009 Apache Software Foundation</font><br />
            <font size=-1>All Rights Reserved</font></p>

        </td>

    </tr>
</table>


