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
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="systemdatabase"/>
<portlet:defineObjects/>

<p><fmt:message key="dbwizard.usage.title">
<fmt:param  value="${pool.name}"/>
</fmt:message></p>

<p><b>WEB-INF/web.xml</b></p>

<p><fmt:message key="dbwizard.usage.resource_refSection"/></p>

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

<fmt:message key="dbwizard.usage.geronimo_webExp">
<fmt:param   value="${pool.name}" />
</fmt:message>


<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;web-app
    xmlns="http://geronimo.apache.org/xml/ns/j2ee/web-1.1"&gt;
    &lt;environment&gt;
        &lt;moduleId&gt;
            &lt;artifactId&gt;MyWebApp&lt;/artifactId&gt;
        &lt;/moduleId&gt;
        &lt;dependencies&gt;
            <b>&lt;dependency&gt;
                &lt;groupId&gt;${pool.abstractNameMap['groupId']}&lt;/groupId&gt;
                &lt;artifactId&gt;${pool.abstractNameMap['artifactId']}&lt;/artifactId&gt;
            &lt;/dependency&gt;</b>
        &lt;/dependencies&gt;
    &lt;/environment&gt;

    &lt;context-root&gt;/MyWebApp&lt;/context-root&gt;

    &lt;!-- security settings, if any, go here --&gt;

    &lt;resource-ref&gt;
        &lt;ref-name&gt;<b>jdbc/MyDataSource</b>&lt;/ref-name&gt;
        <b>&lt;resource-link&gt;${pool.name}&lt;/resource-link&gt;</b>
    &lt;/resource-ref&gt;
&lt;/web-app&gt;
</pre>
<fmt:message key="dbwizard.usage.searchOnlyOne">
<fmt:param   value="${pool.name}" />
</fmt:message>
</p>

<p><i>
<fmt:message key="dbwizard.usage.moreThanOnePool">
<fmt:param   value="${pool.name}" />
</fmt:message>
</i></p>


<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;web-app
    xmlns="http://geronimo.apache.org/xml/ns/j2ee/web-1.1"&gt;
    &lt;environment&gt;
        &lt;moduleId&gt;
            &lt;artifactId&gt;MyWebApp&lt;/artifactId&gt;
        &lt;/moduleId&gt;
        &lt;dependencies&gt;
            <b>&lt;dependency&gt;
                &lt;groupId&gt;${pool.abstractNameMap['groupId']}&lt;/groupId&gt;
                &lt;artifactId&gt;${pool.abstractNameMap['artifactId']}&lt;/artifactId&gt;
            &lt;/dependency&gt;</b>
        &lt;/dependencies&gt;
    &lt;/environment&gt;

    &lt;context-root&gt;/MyWebApp&lt;/context-root&gt;

    &lt;!-- security settings, if any, go here --&gt;

    &lt;resource-ref&gt;
        &lt;ref-name&gt;<b>jdbc/MyDataSource</b>&lt;/ref-name&gt;
        <b>&lt;pattern&gt;
          &lt;groupId&gt;${pool.abstractNameMap['groupId']}&lt;/groupId&gt;
          &lt;artifactId&gt;${pool.abstractNameMap['artifactId']}&lt;/artifactId&gt;
          &lt;name&gt;${pool.abstractNameMap['name']}&lt;/name&gt;
        &lt;/pattern&gt;</b>
    &lt;/resource-ref&gt;
&lt;/web-app&gt;
</pre>

<p><b><fmt:message key="dbwizard.usage.applicationCode" /></b></p>

<p><fmt:message key="dbwizard.usage.usingInCode" /></p>

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
            </portlet:actionURL>"><fmt:message key="dbwizard.common.returnToList" /></a></p>
