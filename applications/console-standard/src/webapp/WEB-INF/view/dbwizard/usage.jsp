<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page talks about how to use the database pool ${pool.name} from a J2EE application.
  The example here is a web application, but other application modules would work in
  the same way.</p>


<p><b>WEB-INF/web.xml</b></p>

<p>The <tt>web.xml</tt> should have a <tt>resource-ref</tt> section declaring the database pool,
like this.  Note the <tt>res-ref-name</tt>, which is what we'll need to map the reference
to a pool, and also what the application will need in order to access the pool.</p>

<pre>
&lt;web-app xmlns="http://java.sun.com/xml/ns/j2ee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://java.sun.com/xml/ns/j2ee
         http://java.sun.com/xml/ns/j2ee/web-app_2_4.xsd"
         version="2.4"&gt;

  &lt;!--  servlets and mappings and normal web.xml stuff here --&gt;

  &lt;resource-ref&gt;
    &lt;res-ref-name&gt;<b>jdbc/MyDataSource</b>&lt;/res-ref-name&gt;
    &lt;res-type&gt;javax.sql.DataSource&lt;/res-type&gt;
    &lt;res-auth&gt;Container&lt;/res-auth&gt;
    &lt;res-sharing-scope&gt;Shareable&lt;/res-sharing-scope&gt;
  &lt;/resource-ref&gt;
&lt;/web-app&gt;
</pre>

<p><b>WEB-INF/geronimo-web.xml</b></p>

<p>To point the resource reference to a specific database pool in Gernimo, the web application
needs to have a <tt>geronimo-web.xml</tt> deployment plan.  That may be packaged in the WAR
in the <tt>WEB-INF</tt> directory, or it may be provided separately on the command line to
the deploy tool.  The <tt>geronimo-web.xml</tt> plan should have a <tt>resource-ref</tt>
block corresponding to the one above, which maps the resource reference to a specific database
pool.  In that block, the <tt>ref-name</tt> must match the <tt>res-ref-name</tt> from
the <tt>web.xml</tt> (above) and the <tt>resource-link</tt> must point to the database
pool.</p>

<p><i>If you have only one pool named ${pool.name} deployed in Geronimo, you can point to it
like this.</i></p>

<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;web-app
    xmlns="http://geronimo.apache.org/xml/ns/web"
    xmlns:naming="http://geronimo.apache.org/xml/ns/naming"
    configId="MyConfigName"
    parentId="org/apache/geronimo/Server"&gt;

    &lt;context-root&gt;/MyWebApp&lt;/context-root&gt;
    &lt;context-priority-classloader&gt;true&lt;/context-priority-classloader&gt;

    &lt;!-- security settings, if any, go here --&gt;

    &lt;naming:resource-ref&gt;
        &lt;naming:ref-name&gt;<b>jdbc/MyDataSource</b>&lt;/naming:ref-name&gt;
        <b>&lt;naming:resource-link&gt;${pool.name}&lt;/naming:resource-link&gt;</b>
    &lt;/naming:resource-ref&gt;
&lt;/web-app&gt;
</pre>

<p>That will search for a pool named ${pool.name} in both the current application and
as a server-wide pool.</p>

<p><i>If you have more than one pool named ${pool.name} (for example, one as a server-wide
pool and one deployed within an application EAR), then you can specify the pool to use
more explicitly like this:</i></p>

<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;web-app
    xmlns="http://geronimo.apache.org/xml/ns/web"
    xmlns:naming="http://geronimo.apache.org/xml/ns/naming"
    configId="MyConfigName"
    parentId="org/apache/geronimo/Server"&gt;

    &lt;context-root&gt;/MyWebApp&lt;/context-root&gt;
    &lt;context-priority-classloader&gt;true&lt;/context-priority-classloader&gt;

    &lt;!-- security settings, if any, go here --&gt;

    &lt;naming:resource-ref&gt;
        &lt;naming:ref-name&gt;<b>jdbc/MyDataSource</b>&lt;/naming:ref-name&gt;
        <b>&lt;naming:domain&gt;${pool.objectNameMap['domain']}&lt;/naming:domain&gt;
        &lt;naming:server&gt;${pool.objectNameMap['J2EEServer']}&lt;/naming:server&gt;
        &lt;naming:application&gt;${pool.objectNameMap['J2EEApplication']}&lt;/naming:application&gt;
        &lt;naming:module&gt;${pool.objectNameMap['JCAResource']}&lt;/naming:module&gt;
        &lt;naming:type&gt;${pool.objectNameMap['j2eeType']}&lt;/naming:type&gt;
        &lt;naming:name&gt;${pool.objectNameMap['name']}&lt;/naming:name&gt;</b>
    &lt;/naming:resource-ref&gt;
&lt;/web-app&gt;
</pre>

<p><b>Application Code</b></p>

<p>To get a reference to the database pool, your application can use code like this.  Note that
the JNDI lookup string is <tt>java:comp/env/</tt> plus the <tt>res-ref-name</tt> used in
<tt>web.xml</tt> (above).</p>

<pre>
protected void doGet(HttpServletRequest request, HttpServletResponse response) {
    try {
        InitialContext ctx = new InitialContext();
        DataSource ds = ctx.lookup("java:comp/env/<b>jdbc/MyDataSource</b>");
        Connection con = ds.getConnection();
    } catch(NamingException e) {
        ...
    } catch(SQLException e) {
        ...
    }
}
</pre>

<hr />

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Return to list</a></p>
