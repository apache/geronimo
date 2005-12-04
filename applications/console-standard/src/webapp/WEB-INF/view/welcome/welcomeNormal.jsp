<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>


<table>
    <tr>

        <!-- Body -->
        <td width="90%" align="left" valign="top">
            <p><font face="Verdana" size="+1"><center><b>Welcome to the Apache Geronimo&#8482;<BR>Administration Console!</b></center></font></p>

            <p>The administration console provides a convenient, user friendly way to administer many aspects of the Geronimo Server.  It is currently a work in progress, and will continue to evolve over time.  The navigation panel on the lefthand side of the screen provides easy access to the individual tasks.  It is always present and allows easy transition from task to task.</p>

            <p>This space is the main content area where the real work happens.  Each view contains one or more portlets (self contained view fragments) that typically include a link for help in the header.  Look at the top of this portlet for an example and try it out.</p>

            <p>The references on the right are provided so that you can learn more about Apache Geronimo, its capabilities, and what might be coming in future releases.</p>

            <p>Mailing lists are available to get involved in the development of Apache Geronimo or to ask questions of the community:</p>

           <ul>
               <li><b><a href="mailto:user-subscribe@geronimo.apache.org">user@geronimo.apache.org</a></b>
                   (<a href="http://marc.theaimsgroup.com/?l=geronimo-user&r=1&w=2">archives</a>)
                   for general questions related to configuring and using Geronimo</li>
               <li><b><a href="mailto:dev-subscribe@geronimo.apache.org">dev@geronimo.apache.org</a></b>
                   (<a href="http://marc.theaimsgroup.com/?l=geronimo-dev&r=1&w=2">archives</a>)
                   for developers working on Geronimo</li>
           </ul>

            <p>So share your experiences with us and let us know how we can make Geronimo even better.</p>

            <p><CENTER><B>Thanks for using Geronimo!</B></CENTER></p>


        </td>

        <td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>

        <!-- Geronimo Links -->
        <td valign="top">

            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><i>Common Console Actions</i></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <%-- Is there any way to not hardcode /console?  The problem is, this
                             code runs from /console-standard, so the context root is not correct.
                             Perhaps there's some tag to point to a different portlet, but what
                             about the images?  --%>
                        <img src="/console/images/ico_db_16x16.gif" /><a href="/console/portal/services/services_jdbc">Database Pools</a><br />
                        <img src="/console/images/ico_lock_16x16.gif" /><a href="/console/portal/Security/Security_realms">Security Realms</a><br />
                        <img src="/console/images/ico_beanjar_16x16.gif" /><a href="/console/portal/services/services_jms">JMS Resources</a><br />
                        <img src="/console/images/ico_list_16x16.gif" /><a href="/console/portal/apps/apps_all">Deploy Applications</a><br />
                        <img src="/console/images/ico_servcomp_16x16.gif" /><a href="/console/portal/server/server_web">Web Server Ports</a><br />
                        <img src="/console/images/ico_look_16x16.gif" /><a href="/console/portal/server/server_info">Memory &amp; Uptime</a><br />
                        &nbsp;<br />
                    </td>
                </tr>
            </table>

            <br />
            <br />

            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><i>Geronimo Online</i></font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <a href="http://geronimo.apache.org/">The Geronimo Home Page</a><br />
                        <a href="http://nagoya.apache.org/jira/secure/BrowseProject.jspa?id=10220">Problem Tracking Database</a><br />
                        <a href="http://mail-archives.apache.org/mod_mbox/geronimo-user/">Users Mailing List</a><br />
                        <a href="http://mail-archives.apache.org/mod_mbox/geronimo-dev/">Developers Mailing List</a><br />
                        <a href="irc://irc.freenode.net/#geronimo">Geronimo IRC chat</a><br />
                        &nbsp;<br />
                    </td>
                </tr>
            </table>

            <br />
            <br />

            <table width="100%" border="1" cellspacing="0" cellpadding="3" bordercolor="#000000">
                <tr>
                    <td class="DarkBackground" align="left" nowrap>
                        <font face="Verdana" size="+1"><i>Geronimo Documentation</i>&nbsp;&nbsp;&nbsp;</font>
                    </td>
                </tr>
                <tr>
                    <td bgcolor="#FFFFFF" nowrap>
                        &nbsp;<br />
                        <a href="http://geronimo.apache.org/faq.html">FAQ</a><br />
                        <a href="http://wiki.apache.org/geronimo">Wiki</a><br />
                        <a href="http://geronimo.apache.org/documentation.html">Geronimo Documentation</a><br />
                        <a href="http://opensource2.atlassian.com/confluence/oss/display/GERONIMO/Home">Additional Documentation</a><br />
                        &nbsp;<br />
                    </td>
                </tr>
            </table>

            <br />
            <br />

            <p align="right"><font size=-1>
<!--   Bring this line in and add the powered by icon when available
            <img src="/console/images/ico_geronimo_16x16.gif"/>
-->
            </font><br />
            &nbsp;
            <font size=-1>Copyright &copy; 1999-2005 Apache Software Foundation</font><br />
            <font size=-1>All Rights Reserved</font></p>

        </td>

    </tr>
</table>


