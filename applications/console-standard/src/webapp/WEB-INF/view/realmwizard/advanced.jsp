<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>Create Security Realm</b> -- Step 3: Advanced Configuration</p>

<c:if test="${!(empty AdvancedError)}"><p><font color="red"><b>Error: ${AdvancedError}</b></font></p></c:if>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>RealmForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="process-advanced" />
    <input type="hidden" name="test" value="true" />
    <input type="hidden" name="name" value="${realm.name}" />
    <input type="hidden" name="realmType" value="${realm.realmType}" />
    <input type="hidden" name="jar" value="${realm.jar}" />
  <c:forEach var="option" items="${realm.options}">
    <input type="hidden" name="option-${option.key}" value="${option.value}" />
  </c:forEach>
    <input type="hidden" name="objectName" value="${realm.objectName}" />
    <input type="hidden" name="module-domain-0" value="${realm.modules[0].loginDomainName}" />
    <input type="hidden" name="module-class-0" value="${realm.modules[0].className}" />
    <input type="hidden" name="module-control-0" value="${realm.modules[0].controlFlag}" />
    <input type="hidden" name="module-server-0" value="${realm.modules[0].serverSide}" />
    <input type="hidden" name="module-options-0" value="${realm.modules[0].optionString}" />
    <input type="hidden" name="module-domain-1" value="${realm.modules[1].loginDomainName}" />
    <input type="hidden" name="module-class-1" value="${realm.modules[1].className}" />
    <input type="hidden" name="module-control-1" value="${realm.modules[1].controlFlag}" />
    <input type="hidden" name="module-server-1" value="${realm.modules[1].serverSide}" />
    <input type="hidden" name="module-options-1" value="${realm.modules[1].optionString}" />
    <input type="hidden" name="module-domain-2" value="${realm.modules[2].loginDomainName}" />
    <input type="hidden" name="module-class-2" value="${realm.modules[2].className}" />
    <input type="hidden" name="module-control-2" value="${realm.modules[2].controlFlag}" />
    <input type="hidden" name="module-server-2" value="${realm.modules[2].serverSide}" />
    <input type="hidden" name="module-options-2" value="${realm.modules[2].optionString}" />
    <input type="hidden" name="module-domain-3" value="${realm.modules[3].loginDomainName}" />
    <input type="hidden" name="module-class-3" value="${realm.modules[3].className}" />
    <input type="hidden" name="module-control-3" value="${realm.modules[3].controlFlag}" />
    <input type="hidden" name="module-server-3" value="${realm.modules[3].serverSide}" />
    <input type="hidden" name="module-options-3" value="${realm.modules[3].optionString}" />
    <input type="hidden" name="module-domain-4" value="${realm.modules[4].loginDomainName}" />
    <input type="hidden" name="module-class-4" value="${realm.modules[4].className}" />
    <input type="hidden" name="module-control-4" value="${realm.modules[4].controlFlag}" />
    <input type="hidden" name="module-server-4" value="${realm.modules[4].serverSide}" />
    <input type="hidden" name="module-options-4" value="${realm.modules[4].optionString}" />
    <table border="0">
    <!-- ENTRY FIELD: Audit Log -->
      <tr>
        <th valign="top"><div align="right">Enable Auditing:</div></th>
        <td valign="top">
          <input type="checkbox" id="<portlet:namespace/>auditCheckbox" name="enableAuditing"<c:if test="${!(empty realm.auditPath)}"> checked="checked"</c:if>
          onclick="document.getElementById('<portlet:namespace/>auditDiv').style.display=this.checked ? 'block' : 'none';document.getElementById('<portlet:namespace/>auditPath').value='';"/>
          <div id="<portlet:namespace/>auditDiv" style="display: <c:choose><c:when test="${empty realm.auditPath}">none</c:when><c:otherwise>block</c:otherwise></c:choose>;">
          Log File: <input type="text" id="<portlet:namespace/>auditPath" name="auditPath" size="30" value="${realm.auditPath}" />
          </div>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>If enabled, every login attempt will be recorded to the specified file.  The path should
          be relative to the Geronimo home directory (a typical value would be
          <tt>var/log/login-attempts.log</tt>).</td>
      </tr>
    <!-- ENTRY FIELDS: Lockout -->
      <tr>
        <th valign="top"><div align="right">Enable Lockout:</div></th>
        <td valign="top">
          <input type="checkbox" id="<portlet:namespace/>lockoutCheckbox" name="enableAuditing"<c:if test="${realm.lockoutEnabled}"> checked="checked"</c:if>
                 onclick="document.getElementById('<portlet:namespace/>lockoutDiv').style.display=this.checked ? 'block' : 'none';document.getElementById('<portlet:namespace/>lockoutCount').value='';document.getElementById('<portlet:namespace/>lockoutWindow').value='';document.getElementById('<portlet:namespace/>lockoutDuration').value='';"/>
          <div id="<portlet:namespace/>lockoutDiv" style="display: <c:choose><c:when test="${realm.lockoutEnabled}">block</c:when><c:otherwise>none</c:otherwise></c:choose>;">
          Lock a user after <input type="text" id="<portlet:namespace/>lockoutCount" name="lockoutCount" size="2" maxlength="3" value="${realm.lockoutCount}" />
          failures within <input type="text" id="<portlet:namespace/>lockoutWindow" name="lockoutWindow" size="4" maxlength="5" value="${realm.lockoutWindow}" /> seconds<br />
          and keep the account locked for <input type="text" id="<portlet:namespace/>lockoutDuration" name="lockoutDuration" size="5" maxlength="5" value="${realm.lockoutDuration}" /> seconds.
          </div>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>If enabled, a certain number of failed logins in a particular time frame will cause a
          user's account to be locked for a certain period of time.  This is a defense against
          brute force account cracking attacks.</td>
      </tr>
    <!-- ENTRY FIELD: Store Password -->
      <tr>
        <th valign="top"><div align="right">Store Password:</div></th>
        <td valign="top">
          <input type="checkbox" name="storePassword"<c:if test="${realm.storePassword}"> checked="checked"</c:if>/>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>If enabled, the realm will store each user's password in a private credential in the
          Subject.  This will allow access to the password later after the login process has
          completed.  This is not normally required.</td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
      <c:choose>
        <c:when test="${realm.testable}">
          <input type="submit" value="Test a Login" />
          <input type="button" value="Skip Test and Deploy" onclick="document.<portlet:namespace/>RealmForm.test.value='false';document.<portlet:namespace/>RealmForm.submit();return false;" />
          <input type="button" value="Skip Test and Show Plan" onclick="document.<portlet:namespace/>RealmForm.mode.value='plan';document.<portlet:namespace/>RealmForm.submit();return false;" />
        </c:when>
        <c:otherwise>
          <input type="button" value="Deploy Realm" onclick="document.<portlet:namespace/>RealmForm.test.value='false';document.<portlet:namespace/>RealmForm.submit();return false;" />
          <input type="button" value="Show Plan" onclick="document.<portlet:namespace/>RealmForm.mode.value='plan';document.<portlet:namespace/>RealmForm.submit();return false;" />
        </c:otherwise>
      </c:choose>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
