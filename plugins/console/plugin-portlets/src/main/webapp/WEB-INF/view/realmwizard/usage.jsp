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
<fmt:setBundle basename="pluginportlets"/>
<portlet:defineObjects/>

<p><fmt:message key="realmwizard.usage.title" >
<fmt:param  value="${realm.name}"/>
</fmt:message></p>


<p><b>WEB-INF/web.xml</b></p>

<fmt:message key="realmwizard.usage.webXmlShouldHave" />

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

<fmt:message key="realmwizard.usage.geronimoWebXmlPreface" />

<pre>
&lt;?xml version="1.0" encoding="UTF-8"?&gt;
&lt;web-app
    xmlns="http://geronimo.apache.org/xml/ns/j2ee/web-1.1"&gt;
    &lt;environment&gt;
        &lt;moduleId&gt;
            &lt;artifactId&gt;MyWebApp&lt;/artifactId&gt;
        &lt;/moduleId&gt;
    &lt;/environment&gt;

    &lt;context-root&gt;/MyWebApp&lt;/context-root&gt;

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
<fmt:message key="realmwizard.usage.geronimoWebXmlLater" >
<fmt:param  value="${realm.name}"/>
</fmt:message>

<p><b><fmt:message key="realmwizard.usage.applicationCode" /></b></p>
<fmt:message key="realmwizard.usage.applicationCodeExp" />


<hr />

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message key="consolebase.common.returnToList"/></a></p>
