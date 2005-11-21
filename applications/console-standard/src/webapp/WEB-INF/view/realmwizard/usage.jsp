<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page talks about how to use the security realm ${realm.name} from a J2EE application.
  The example here is a web application, but other application modules would work similarly.</p>


<p><b>WEB-INF/web.xml</b></p>

<p>The <tt>web.xml</tt> should have</p>
<ul>
  <li>One or more <tt>security-constraint</tt> blocks designating the protected pages or URLs</li>
  <li>A <tt>login-config</tt> section configuring the login style for the application</li>
  <li>One or more <tt>security-role</tt> blocks listing the security roles used by the
    application</li>
</ul>

<pre>
&lt;web-app xmlns="http://java.sun.com/xml/ns/j2ee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee
         http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
         version="2.4"&gt;

  &lt;!--  servlets and mappings and normal web.xml stuff here --&gt;

    &lt;security-constraint&gt;
        &lt;web-resource-collection&gt;
            &lt;web-resource-name&gt;Protected&lt;/web-resource-name&gt;
            &lt;url-pattern&gt;/protected/*&lt;/url-pattern&gt;
            &lt;http-method&gt;GET&lt;/http-method&gt;
            &lt;http-method&gt;POST&lt;/http-method&gt;
        &lt;/web-resource-collection&gt;
        &lt;auth-constraint&gt;
            &lt;role-name&gt;<b>admin</b>&lt;/role-name&gt;
        &lt;/auth-constraint&gt;
    &lt;/security-constraint&gt;
    &lt;login-config&gt;
        &lt;auth-method&gt;FORM&lt;/auth-method&gt;
        &lt;realm-name&gt;This is not used for FORM login&lt;/realm-name&gt;
        &lt;form-login-config&gt;
            &lt;form-login-page&gt;/login.jsp&lt;/form-login-page&gt;
            &lt;form-error-page&gt;/loginerror.jsp&lt;/form-error-page&gt;
      &lt;/form-login-config&gt;
    &lt;/login-config&gt;
    &lt;security-role&gt;
        &lt;role-name&gt;<b>admin</b>&lt;/role-name&gt;
    &lt;/security-role&gt;
&lt;/web-app&gt;
</pre>

<p><b>WEB-INF/geronimo-web.xml</b></p>

<p>To configure the security realm and the members of each role, the web application
needs to have a <tt>geronimo-web.xml</tt> deployment plan.  That may be packaged in the WAR
in the <tt>WEB-INF</tt> directory, or it may be provided separately on the command line to
the deploy tool.</p>

<p>The <tt>geronimo-web.xml</tt> plan should have a <tt>security-realm-name</tt>
element indicating which realm will be used to authenticate logins to the web application.
It also needs to have a <tt>security</tt> element listing the users or groups who
should be members of each <tt>security-role</tt> listed in <tt>web.xml</tt>.</p>

<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;web-app
    xmlns="http://geronimo.apache.org/xml/ns/web"
    xmlns:naming="http://geronimo.apache.org/xml/ns/naming"
    configId="MyConfigName"
    parentId="org/apache/geronimo/Server"&gt;

    &lt;context-root&gt;/MyWebApp&lt;/context-root&gt;
    &lt;context-priority-classloader&gt;true&lt;/context-priority-classloader&gt;

    &lt;security-realm-name&gt;<b>${realm.name}</b>&lt;/security-realm-name&gt;
    &lt;security&gt;
        &lt;default-principal&gt;
            &lt;principal name="anonymous"
class="org.apache.geronimo.security.realm.providers.GeronimoUserPrincipal"
            /&gt;
        &lt;/default-principal&gt;
        &lt;role-mappings&gt;
            &lt;role role-name="<b>admin</b>"&gt;
                &lt;principal name="administrators" designated-run-as="true"
class="org.apache.geronimo.security.realm.providers.<b>GeronimoGroupPrincipal</b>"
                /&gt;
                &lt;principal name="root"
class="org.apache.geronimo.security.realm.providers.<b>GeronimoUserPrincipal</b>"
                /&gt;
            &lt;/role&gt;
        &lt;/role-mappings&gt;
    &lt;/security&gt;
&lt;/web-app&gt;
</pre>

<p>This example indicated that ${realm.name} will be used to handle all logins to
the web application.  Then it maps the <tt>admin</tt> role to a combination of
one user (<tt>root</tt>) and one group (<tt>administrators</tt>), using a combination
of the principal classes and principal names.  (Note that if ${realm.name} uses a
custom login module, the principal classes may be different, but the ones listed
above are used for users and groups by all the standard Geronimo login modules.)</p>

<p>It's also possible to configure separate login modules to use separate login
domain names, and then use the login domain names in the role mapping (so a user
"root" from login domain "Foo" is different from a user "root" from login domain
"Bar"), but this is only important if you have multiple login modules assigning
principals to the users.</p>

<p>Finally, if the <tt>security</tt> section is declared in an EAR
<tt>application.xml</tt> deployment descriptor, there's no need to repeat it
in any of the modules inside the EAR -- they'll all share the same role mapping
information.</p>

<p><b>Application Code</b></p>

<p>No special application code is required to work with security roles.</p>

<p>If an application calls <tt>HttpServletRequest.getUserPrincipal()</tt>,
Geronimo will return a principal where the principal class implements
<tt>GeronimoCallerPrincipal</tt> -- normally a username (since <tt>GeronimoUserPrincipal</tt>
implements <tt>GeronimoCallerPrincipal</tt>).  If you're using a custom login
module and getting the wrong results for <tt>getUserPrincipal</tt>, try
making your user principal class implement <tt>GeronimoCallerPrincipal</tt>.</p>

<p>If an application calls <tt>HttpServletRequest.isUserInRole(role)</tt>,
Geronimo will return true or false depending on whether any of the principals
assigned to that user by the realm's login modules were listed in the role
mapping above.</p>

<hr />

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Return to list</a></p>
