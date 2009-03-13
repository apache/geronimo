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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg" %>
<fmt:setBundle basename="systemdatabase"/>

<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>DatabaseForm";
var <portlet:namespace/>requiredFields = new Array();
var <portlet:namespace/>passwordFields = new Array();
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.common.emptyText"/>');
        return false;    
    }
    if(!passwordElementsConfirm(<portlet:namespace/>formName, <portlet:namespace/>passwordFields)) {
        addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.common.passwordMismatch"/>');
        return false;
    }
    return <portlet:namespace/>validate();
}
</script>

<CommonMsg:commonMsg/><div id="<portlet:namespace/>CommonMsgContainer"></div>

<p>
  <c:choose> <%-- Can't change the pool name after deployment because it's wired into all the ObjectNames --%>
    <c:when test="${empty pool.rarPath}">
      <fmt:message key="dbwizard.edit.summary"/>
    </c:when>
    <c:otherwise>
      <fmt:message key="dbwizard.basicParams.title"/>
    </c:otherwise>
  </c:choose>
</p>

<script language="JavaScript">
function <portlet:namespace/>validate() {

   if (document.<portlet:namespace/>DatabaseForm.minSize.value == "") {
       document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
   }
   if (document.<portlet:namespace/>DatabaseForm.maxSize.value == "") {
       document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
   }

   var min = parseInt(document.<portlet:namespace/>DatabaseForm.minSize.value); 
   var max = parseInt(document.<portlet:namespace/>DatabaseForm.maxSize.value); 

   if (isNaN(min)) {
       addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.edit.errorMsg01"/>');
       min = document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
       return false;
   }
   if (min < 0){
       addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.edit.errorMsg02"/>');
       min = document.<portlet:namespace/>DatabaseForm.minSize.value = 0;
       return false;
   }
   if (isNaN(max)) {
       addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.edit.errorMsg03"/>');
       max = document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
       return false;
   }
   if (max <= 0){
       addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.edit.errorMsg04"/>');
       max = document.<portlet:namespace/>DatabaseForm.maxSize.value = 10;
       return false;
   } 
   if (min > max) {
       addErrorMessage("<portlet:namespace/>", '<fmt:message key="dbwizard.edit.errorMsg05"/>');
       return false;
   }
   return true;
}
</script>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>DatabaseForm" action="<portlet:actionURL/>" method="POST">
    <input type="hidden" name="mode" value="process-url" />
    <input type="hidden" name="test" value="true" />

    <input type="hidden" name="dbtype" value="${pool.dbtype}" />
    <input type="hidden" name="urlPrototype" value="${pool.urlPrototype}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
  <c:forEach var="prop" items="${pool.urlProperties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
    <input type="hidden" name="abstractName" value="${pool.abstractName}" />
    <input type="hidden" name="adapterDisplayName" value="${pool.adapterDisplayName}" />
    <input type="hidden" name="adapterDescription" value="${pool.adapterDescription}" />
    <input type="hidden" name="rarPath" value="${pool.rarPath}" />

    <table border="0">
    <!-- ENTRY FIELD: NAME -->
      <tr>
        <th style="min-width: 140px"><div align="right"><fmt:message key="dbwizard.edit.poolName"/>:</div></th>
        <td>
      <c:choose> <%-- Can't change the pool name after deployment because it's wired into all the ObjectNames --%>
        <c:when test="${empty pool.abstractName}">
          <input name="name" type="text" size="30" value="${pool.name}" title='<fmt:message key="dbwizard.edit.poolName"/>'>
          <script language="JavaScript">
            <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFields.concat(new Array("name"));
          </script>
        </c:when>
        <c:otherwise>
          <input name="name" type="hidden" value="${pool.name}" />
          <b><c:out value="${pool.name}" /></b>
        </c:otherwise>
      </c:choose>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.edit.poolNameExp"/></td>
      </tr>
    <!-- STATUS FIELD: Display Name -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.edit.poolType"/>:</div></th>
        <td><i><c:out value="${pool.adapterDisplayName}" /></i></td>
      </tr>
      <tr>
        <td />
        <td><c:out value="${pool.adapterDescription}" /></td>
      </tr>
    <!-- HEADER -->
      <tr>
        <th colspan="2"><fmt:message key="dbwizard.edit.basicConProperties"/></th>
      </tr>
<c:choose>
  <c:when test="${pool.generic}"> <%-- This is a standard TranQL JDBC pool -- we know what parameters it wants --%>
    <!-- ENTRY FIELD: Driver Class -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.JDBCDriverClass" />:</div></th>
        <td>
      <c:choose>
        <c:when test="${empty pool.abstractName}">
          <input name="driverClass" title='<fmt:message key="dbwizard.common.JDBCDriverClass" />' type="text" size="30" value="${pool.driverClass}">
          <script language="JavaScript">
            <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFields.concat(new Array("driverClass"));
          </script>
        </c:when>
        <c:otherwise>
          <input type="hidden" name="driverClass" value="${pool.driverClass}" />
          <i><c:out value="${pool.driverClass}" /></i>
        </c:otherwise>
      </c:choose>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>
          <c:if test="${!(empty driverError)}"><font color="red"><b><fmt:message key="dbwizard.edit.unableToLoadDriver"/></b></font></c:if>
          <fmt:message key="dbwizard.edit.seeDocumentation"/> 
        </td>
      </tr>
    <!-- ENTRY FIELD: Driver JAR -->
  <c:choose> <%-- Can't set JAR after deployment because we don't know how to dig through dependencies yet --%>
    <c:when test="${empty pool.abstractName}">
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>jars"><fmt:message key="dbwizard.common.driverJAR"/></label>:</div></th>
        <td>
          <select multiple="true" name="jars" id="<portlet:namespace/>jars" size="10">
              <c:forEach var="availableJar" items="${availableJars}">
                  <option value="${availableJar}" <c:forEach var="jar" items="${pool.jars}"><c:if test="${availableJar == jar}">selected</c:if></c:forEach>>
                      ${availableJar}
                  </option>
              </c:forEach>
          </select>
          <script language="JavaScript">
            <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFields.concat(new Array("jars"));
          </script>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.common.driverJARExplanation"/>
          <input type="button" value='<fmt:message key="dbwizard.common.downloadDriver"/>' onclick="document.<portlet:namespace/>DatabaseForm.mode.value='download';document.<portlet:namespace/>DatabaseForm.submit();return false;" />)
        </td>
      </tr>
    </c:when>
    <c:otherwise>
      <c:forEach var="jar" items="${pool.jars}">
        <input type="hidden" name="jars" value="${jar}" />
      </c:forEach>
    </c:otherwise>
  </c:choose>
    <!-- ENTRY FIELD: URL -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>url"><fmt:message key="dbwizard.common.JDBCConnectURL"/></label>:</div></th>
        <td><input name="url" id="<portlet:namespace/>url" type="text" size="50" value="${pool.url}"></td>
      </tr>
      <script language="JavaScript">
        <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFields.concat(new Array("url"));
      </script>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.common.URLFits"/></td>
      </tr>
    <!-- ENTRY FIELD: Username -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>user"><fmt:message key="dbwizard.common.DBUserName"/></label>:</div></th>
        <td><input name="user" id="<portlet:namespace/>user" type="text" size="20" value="${pool.user}" autocomplete="off"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.common.DBUserNameExp"/></td>
      </tr>
    <!-- ENTRY FIELD: Password -->
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>password"><fmt:message key="dbwizard.common.DBPassword"/></label>:</div></th>
        <td><input name="password" id="<portlet:namespace/>password" type="password" size="20" value="${pool.password}" autocomplete="off"></td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>confirm-password"><fmt:message key="dbwizard.common.confirmPassword"/></label>:</div></th>
        <td><input name="confirm-password" id="<portlet:namespace/>confirm-password" type="password" size="20" value="${pool.password}" autocomplete="off"></td>
      </tr>
      <script language="JavaScript">
        <portlet:namespace/>passwordFields = <portlet:namespace/>passwordFields.concat(new Array("password"));
      </script>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.common.DBPasswordExp"/>

        <%-- Just to be safe, save all the non-Generic properties since we're not going to edit them here --%>
  <c:forEach var="prop" items="${pool.properties}">
    <input type="hidden" name="${prop.key}" value="${prop.value}" />
  </c:forEach>
        </td>
      </tr>
  </c:when>
  <c:otherwise> <%-- This is an XA or other connection factory that we don't have special parameter handling for --%>
    <!-- ENTRY FIELD: Driver JAR -->
  <c:choose> <%-- Can't set JAR after deployment because we don't know how to dig through dependencies yet --%>
    <c:when test="${empty pool.abstractName}">
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>jars"><fmt:message key="dbwizard.common.driverJAR"/></label>:</div></th>
        <td>
          <select multiple="true" name="jars" id="<portlet:namespace/>jars" size="10">
              <c:forEach var="availableJar" items="${availableJars}">
                  <option value="${availableJar}" <c:forEach var="jar" items="${pool.jars}"><c:if test="${availableJar == jar}">selected</c:if></c:forEach>>
                      ${availableJar}
                  </option>
              </c:forEach>
          </select>
          <script language="JavaScript">
            <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFields.concat(new Array("jars"));
          </script>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.common.driverJARExplanation"/>
          <input type="button" value='<fmt:message key="dbwizard.common.downloadDriver"/>'  onclick="document.<portlet:namespace/>DatabaseForm.mode.value='download';document.<portlet:namespace/>DatabaseForm.submit();return false;" />)
        </td>
      </tr>
    </c:when>
    <c:otherwise>
      <c:forEach var="jar" items="${pool.jars}">
        <input type="hidden" name="jars" value="${jar}" />
      </c:forEach>
    </c:otherwise>
  </c:choose>
    <c:forEach var="prop" items="${pool.properties}">
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>${prop.key}"><fmt:message key="dbwizard.${pool.adapterRarPath}.${fn:substring(prop.key, 9, -1)}"/></label>:</div></th>
        <td><input name="${prop.key}" id="<portlet:namespace/>${prop.key}" <c:choose><c:when test="${fn:containsIgnoreCase(prop.key, 'password')}">type="password" autocomplete="off"</c:when><c:otherwise>type="text"</c:otherwise></c:choose> size="20" value="${prop.value}"></td>
      </tr>
    <c:if test="${fn:containsIgnoreCase(prop.key, 'password')}">
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>confirm-${prop.key}"><fmt:message key="dbwizard.common.confirmPassword"/></label>:</div></th>
        <td><input name="confirm-${prop.key}" id="<portlet:namespace/>confirm-${prop.key}" type="password" size="20" value="${prop.value}" autocomplete="off"></td>
      </tr>
      <script language="JavaScript">
        <portlet:namespace/>passwordFields = <portlet:namespace/>passwordFields.concat(new Array("${prop.key}"));
      </script>
    </c:if>   
      <tr>
        <td></td>
        <td><fmt:message key="dbwizard.${pool.adapterRarPath}.${fn:substring(prop.key, 9, -1)}Exp"/></td>
      </tr>
    </c:forEach>
      <tr><td colspan="2">
        <%-- Just to be safe, save all the Generic properties since we're not going to edit them here --%>
        <input type="hidden" name="user" value="${pool.user}" />
        <input type="hidden" name="password" value="${pool.password}" />
        <input type="hidden" name="driverClass" value="${pool.driverClass}" />
        <input type="hidden" name="url" value="${pool.url}" />
        <!-- There is already a form element "jars" defined in this form either as a "select" or as "hidden" field
             depending upon whether the pool is being created or edited.  Commented out the following.  Need to
             investigate the other four hidden elements above too. -->
        <!-- <input type="hidden" name="jars" value="${pool.jars}" /> -->
      </td></tr>
  </c:otherwise>
</c:choose>
    <!-- HEADER -->
      <tr>
        <th colspan="2"><fmt:message key="dbwizard.common.connectionPoolParameters"/></th>
      </tr>
     <c:if test="${pool.transactionType != null}"> 
       <!-- ENTRY FIELD: TRANSACTION TYPE -->
        <tr>
          <th><div align="right"><label for="<portlet:namespace/>transactionType"><fmt:message key="dbwizard.common.transactionType"/></label>:</div></th>
          <td>
            <select name="transactionType" id="<portlet:namespace/>transactionType">
              <option <c:if test="${'LOCAL' == pool.transactionType}">selected</c:if>>LOCAL</option>       
              <option <c:if test="${'XA' == pool.transactionType}">selected</c:if>>XA</option>       
              <option <c:if test="${'NONE' == pool.transactionType}">selected</c:if>>NONE</option>       
            </select>
          </td>
        </tr>
        <tr>
          <td></td>
          <td><fmt:message key="dbwizard.common.driverTransactionTypes"/></td>
        </tr>
      </c:if>
    <!-- ENTRY FIELD: Min Size -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.poolMinSize"/>:</div></th>
        <td><input name="minSize" id="<portlet:namespace/>minSize" type="text" size="5" value="${pool.minSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td><label for="<portlet:namespace/>minSize"><fmt:message key="dbwizard.edit.minimumNoOfCon"/></label></td>
      </tr>
    <!-- ENTRY FIELD: Max Size -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.poolMaxSize"/>:</div></th>
        <td><input name="maxSize" id="<portlet:namespace/>maxSize" type="text" size="5" value="${pool.maxSize}"></td>
      </tr>
      <tr>
        <td></td>
        <td><label for="<portlet:namespace/>maxSize"><fmt:message key="dbwizard.common.maxNoOfCon"/></label></td>
      </tr>
    <!-- ENTRY FIELD: Blocking Timeout -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.blockingTimeout"/>:</div></th>
        <td><input name="blockingTimeout" id="<portlet:namespace/>blockingTimeout" type="text" size="7" value="${pool.blockingTimeout}"> (<fmt:message key="dbwizard.common.inMilliseconds"/>)</td>
      </tr>
      <tr>
        <td></td>
        <td><label for="<portlet:namespace/>blockingTimeout"><fmt:message key="dbwizard.edit.blockingTimeoutExp"/></label></td>
      </tr>
    <!-- ENTRY FIELD: Idle timeout -->
      <tr>
        <th><div align="right"><fmt:message key="dbwizard.common.idleTimeout"/>:</div></th>
        <td><input name="idleTimeout" id="<portlet:namespace/>idleTimeout" type="text" size="5" value="${pool.idleTimeout}"> (<fmt:message key="dbwizard.common.inMinutes"/>)</td>
      </tr>
      <tr>
        <td></td>
        <td><label for="<portlet:namespace/>idleTimeout"><fmt:message key="dbwizard.edit.idleTimeoutExp"/></label></td>
      </tr>

    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td>
<c:choose> <%-- Don't know how to test a non-generic pool, so you can only save it --%>
  <c:when test="${pool.generic}">
    <c:choose> <%-- Can't test after deployment because we don't know what JAR to put on the ClassPath, can't show plan becasue we can't update a plan --%>
      <c:when test="${empty pool.abstractName}">
          <input type="button" value='<fmt:message key="dbwizard.common.testConnection"/>' onclick="if (<portlet:namespace/>validateForm()){document.<portlet:namespace/>DatabaseForm.test.value='true';document.<portlet:namespace/>DatabaseForm.submit();}" />
          <input type="button" value='<fmt:message key="dbwizard.common.skipTestAndDeploy"/>' onclick="if (<portlet:namespace/>validateForm()){document.<portlet:namespace/>DatabaseForm.test.value='false';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
          <input type="button" value='<fmt:message key="dbwizard.common.skipTestAndShowPlan"/>' onclick="if (<portlet:namespace/>validateForm()){document.<portlet:namespace/>DatabaseForm.mode.value='plan';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
      </c:when>
      <c:otherwise>
          <input type="button" value='<fmt:message key="dbwizard.common.save"/>' onclick="if (<portlet:namespace/>validateForm()){document.<portlet:namespace/>DatabaseForm.mode.value='save';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
      </c:otherwise>
    </c:choose>
  </c:when>
  <c:otherwise> <%-- Not a generic JDBC pool --%>
    <c:choose>
      <c:when test="${empty pool.abstractName}"> <%-- If it's new we can preview the plan or save/deploy --%>
          <input type="button" value='<fmt:message key="dbwizard.common.deploy"/>' onclick="if (<portlet:namespace/>validateForm()){document.<portlet:namespace/>DatabaseForm.mode.value='save';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
          <input type="button" value='<fmt:message key="dbwizard.common.showPlan"/>' onclick="if (<portlet:namespace/>validateForm()){document.<portlet:namespace/>DatabaseForm.mode.value='plan';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
      </c:when>
      <c:otherwise> <%-- If it's existing we can only save --%>
          <input type="button" value='<fmt:message key="dbwizard.common.save"/>'  onclick="if (<portlet:namespace/>validateForm()){document.<portlet:namespace/>DatabaseForm.mode.value='save';document.<portlet:namespace/>DatabaseForm.submit();return false;}" />
      </c:otherwise>
    </c:choose>
  </c:otherwise>
</c:choose>
        </td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->



<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>"><fmt:message  key="dbwizard.common.cancel"/></a></p>
