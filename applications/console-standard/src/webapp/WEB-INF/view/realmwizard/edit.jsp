<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page edits a new or existing security realm.</p>

<p>A security realm may have one or more login modules.  Many simple realms have
only one login module.  Additional login modules may be used to access more
underlying security information stores, or to add functionality such as auditing
to a realm without affecting the authentication process for the realm.</p>

<c:if test="${empty realm.objectName}">
<p>If you don't need to use as many login modules as there are entries below,
just leave the extra ones blank.</p>
</c:if>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>RealmForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="save" />
    <input type="hidden" name="jar" value="${realm.jar}" />
    <input type="hidden" name="name" value="${realm.name}" />
    <input type="hidden" name="realmType" value="${realm.realmType}" />
  <c:forEach var="option" items="${realm.options}">
    <input type="hidden" name="option-${option.key}" value="${option.value}" />
  </c:forEach>
    <input type="hidden" name="auditPath" value="${realm.auditPath}" />
    <input type="hidden" name="lockoutCount" value="${realm.lockoutCount}" />
    <input type="hidden" name="lockoutWindow" value="${realm.lockoutWindow}" />
    <input type="hidden" name="lockoutDuration" value="${realm.lockoutDuration}" />
    <input type="hidden" name="storePassword" value="${realm.storePassword}" />
    <input type="hidden" name="objectName" value="${realm.objectName}" />

    <table border="0">
    <!-- ENTRY FIELD: NAME -->
      <tr>
        <th style="min-width: 140px"><div align="right">Realm Name:</div></th>
        <td>
      <c:choose> <%-- Can't change the realm name after deployment because it's wired into all the ObjectNames --%>
        <c:when test="${empty realm.objectName}">
          <input name="name" type="text" size="30" value="${realm.name}">
        </c:when>
        <c:otherwise>
          <input name="name" type="hidden" value="${realm.name}" />
          <b><c:out value="${realm.name}" /></b>
        </c:otherwise>
      </c:choose>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>A name that is different than the name for any other security realms in the server (no spaces in the name please).
          Other components will use this name to refer to the security realm.</td>
      </tr>
    <!-- HEADER -->
    <c:forEach var="module" items="${realm.modules}" varStatus="status" >
      <tr>
        <th colspan="2">Login Module ${status.index+1}</th>
      </tr>
      <tr>
        <th><div align="right">Login Domain Name:</div></th>
        <td>
      <c:choose> <%-- Can't change the login domain name after deployment because it's how we know which GBean is which --%>
        <c:when test="${empty realm.objectName}">
          <input name="module-domain-${status.index}" type="text" size="20" value="${module.loginDomainName}" />
        </c:when>
        <c:otherwise>
          <input name="module-domain-${status.index}" type="hidden" value="${module.loginDomainName}" />
          <b><c:out value="${module.loginDomainName}" /></b>
        </c:otherwise>
      </c:choose>
        </td>

        <td></td>
      </tr>
      <tr>
        <td></td>
        <td>The login domain for this login module, which must be unique among all modules in the security realm.
          This can be used to distinguish principals from two otherwise identical login modules (for example,
          from two LDAP login modules pointing to two different LDAP servers)</td>
      </tr>
      <tr>
        <th><div align="right">Login Module Class:</div></th>
        <td><input name="module-class-${status.index}" type="text" size="60" value="${module.className}" /></td>
      </tr>
      <tr>
        <td></td>
        <td>The fully-qualified class name for the login module.</td>
      </tr>
      <tr>
        <th><div align="right">Control Flag:</div></th>
        <td>
          <select name="module-control-${status.index}">
            <option value="OPTIONAL"<c:if test="${module.controlFlag eq 'OPTIONAL'}"> selected</c:if>>Optional</option>
            <option value="REQUIRED"<c:if test="${module.controlFlag eq 'REQUIRED'}"> selected</c:if>>Required</option>
            <option value="REQUISITE"<c:if test="${module.controlFlag eq 'REQUISITE'}"> selected</c:if>>Requisite</option>
            <option value="SUFFICIENT"<c:if test="${module.controlFlag eq 'SUFFICIENT'}"> selected</c:if>>Sufficient</option>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>The control flag for the login module, which controls what happens to the overall login processing if this
          login module succeeds or fails.  For more information see
          <a href="http://java.sun.com/j2se/1.4.2/docs/api/javax/security/auth/login/Configuration.html">javax.security.auth.login.Configuration</a>.</td>
      </tr>
      <tr>
        <th><div align="right">Server-Side:</div></th>
        <td>
          <select name="module-server-${status.index}">
            <option value="true"<c:if test="${module.serverSide}"> selected</c:if>>Server Side</option>
            <option value="false"<c:if test="${!module.serverSide}"> selected</c:if>>Client Side</option>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>Server-side login modules are executed within the application server (this is normally correct).  Client-side
          login modules are executed in the client's environment, for example, in order to use single sign-on features
          of the client OS.</td>
      </tr>
      <tr>
        <th><div align="right">Support Advanced Mapping:</div></th>
        <td>
          <select name="module-wrap-${status.index}">
            <option value="true"<c:if test="${module.wrapPrincipals}"> selected</c:if>>Yes</option>
            <option value="false"<c:if test="${!module.wrapPrincipals}"> selected</c:if>>No</option>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>Normally Geronimo can't distinguish between two different principals that have the same name
          and same principal class but were produced by two different login modules.  If this option is
          enabled, Geronimo will "wrap" principals to track which login module and realm each
          principal came from.  This lets you use the "realm-principal" and "login-domain-principal"
          elements in your security mapping in Geronimo deployment plans.</td>
      </tr>
      <tr>
        <th><div align="right">Configuration Options:</div></th>
        <td><textarea name="module-options-${status.index}" rows="5" cols="60">${module.optionString}</textarea></td>
      </tr>
      <tr>
        <td></td>
        <td>Any configuration options necessary for the login module, in the standard Java properties format (one
          per line, <tt>name=value</tt>)</td>
      </tr>
    </c:forEach>

    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
          <input type="button" value="<c:choose><c:when test="${empty realm.objectName}">Deploy</c:when><c:otherwise>Save</c:otherwise></c:choose>"
                 onclick="document.<portlet:namespace/>RealmForm.mode.value='save';document.<portlet:namespace/>RealmForm.submit();return false;" />
          <input type="button" value="Show Plan" onclick="document.<portlet:namespace/>RealmForm.mode.value='plan';document.<portlet:namespace/>RealmForm.submit();return false;" />
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->



<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
