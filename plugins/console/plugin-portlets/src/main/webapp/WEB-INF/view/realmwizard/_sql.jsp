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
<fmt:setBundle basename="pluginportlets"/>

      <script language="JavaScript">
        var <portlet:namespace/>requiredFieldsCommon = new Array('option-userSelect', 'option-groupSelect');
        var <portlet:namespace/>requiredFieldsJDBC = new Array('option-jdbcDriver', 'jar', 'option-jdbcURL', 'option-jdbcUser', 'option-jdbcPassword');
        var <portlet:namespace/>passwordFieldsJDBC = new Array('option-jdbcPassword');
        function <portlet:namespace/>changeRequiredFields(par) {
          <portlet:namespace/>passwordFields = <portlet:namespace/>passwordFieldsJDBC;
          if(par.value != '') // Database pool is selected
            <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFieldsCommon;
          else
            <portlet:namespace/>requiredFields = <portlet:namespace/>requiredFieldsCommon.concat(<portlet:namespace/>requiredFieldsJDBC);
        }
      </script>
      <tr>
        <th style="min-width: 140px"><div align="right"><label for="<portlet:namespace/>option-userSelect"><fmt:message key="realmwizard.common.userSelectSQL" /></label>:</div></th>
        <td><input name="option-userSelect" id="<portlet:namespace/>option-userSelect" type="text"
                   size="60" value="${realm.options['userSelect']}"></td>
      </tr>
      <tr>
        <td></td>
         <td><fmt:message key="realmwizard._sql.userSelectSQLExp" /></td>
      </tr>

      <tr>
        <th><div align="right"><label for="<portlet:namespace/>option-groupSelect"><fmt:message key="realmwizard.common.groupSelectSQL" /></label>:</div></th>
        <td><input name="option-groupSelect" id="<portlet:namespace/>option-groupSelect" type="text"
                   size="60" value="${realm.options['groupSelect']}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.groupSelectSQLExp" /></td>
      </tr>

      <tr>
        <th><div align="right"><label for="<portlet:namespace/>option-digest"><fmt:message key="realmwizard.common.digestAlgorithm" /></label>:</div></th>
        <td><input name="option-digest" id="<portlet:namespace/>option-digest" type="text"
                   size="10" value="${realm.options['digest']}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.digestAlgorithmExp" /></td>
      </tr>

      <tr>
        <th><div align="right"><label for="<portlet:namespace/>option-encoding"><fmt:message key="realmwizard.common.digestEncoding" /></label>:</div></th>
        <td><input name="option-encoding" id="<portlet:namespace/>option-encoding" type="text"
                   size="10" value="${realm.options['encoding']}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.digestEncodingExp" />
        </td>
      </tr>

      <tr>
        <td></td>
        <td><i><fmt:message key="realmwizard._sql.eitherPoolOrJDBC" />
        </i></td>
      </tr>


      <tr>
        <th><div align="right"><label for="<portlet:namespace/>option-databasePoolAbstractName"><fmt:message key="realmwizard.common.databasePool" /></label></div></th>
        <td>
          <select name="option-databasePoolAbstractName" id="<portlet:namespace/>option-databasePoolAbstractName" onChange="<portlet:namespace/>changeRequiredFields(this)">
            <option />
        <c:forEach var="pool" items="${pools}">
            <option value="${pool.abstractName}"<c:if test="${realm.options['dataSourceName'] eq pool.name && realm.options['dataSourceApplication'] eq pool.applicationName}"> selected</c:if>>${pool.displayName}</option>
        </c:forEach>
          </select>
          <script language="JavaScript">
            <portlet:namespace/>changeRequiredFields(document.forms[<portlet:namespace/>formName].elements['option-databasePoolAbstractName']);
          </script>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.databasePoolExp" /></td>
      </tr>

      <tr>
        <th><div align="right"><label for="<portlet:namespace/>option-jdbcDriver"><fmt:message key="realmwizard.common.JDBCDriverClass" /></label></div></th>
        <td><input name="option-jdbcDriver" id="<portlet:namespace/>option-jdbcDriver" type="text"
                   size="60" value="${realm.options['jdbcDriver']}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.JDBCDriverClassExp" /></td>
      </tr>

      <tr>
        <th><div align="right"><label for="<portlet:namespace/>jar"><fmt:message key="realmwizard.common.driverJAR" /></label>:</div></th>
        <td>
          <select name="jar" id="<portlet:namespace/>jar">
            <option />
        <c:forEach var="jar" items="${jars}">
            <option value="${jar}" <c:if test="${jar == realm.jar}">selected</c:if>>${jar}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.driverJARExp" /></td>
      </tr>

      <tr>
        <th><div align="right"><label for="<portlet:namespace/>option-jdbcURL"><fmt:message key="realmwizard.common.JDBCUrl" /></label></div></th>
        <td><input name="option-jdbcURL" id="<portlet:namespace/>option-jdbcURL" type="text"
                   size="60" value="${realm.options['jdbcURL']}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.JDBCUrlExp" /></td>
      </tr>

      <tr>
        <th><div align="right"><label for="<portlet:namespace/>option-jdbcUser"><fmt:message key="realmwizard.common.JDBCUsername" /></label></div></th>
        <td><input name="option-jdbcUser" id="<portlet:namespace/>option-jdbcUser" type="text"
                   size="20" value="${realm.options['jdbcUser']}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.JDBCUsernameExp" /></td>
      </tr>

      <tr>
        <th><div align="right"><label for="<portlet:namespace/>option-jdbcPassword"><fmt:message key="realmwizard.common.JDBCPassword" /></label></div></th>
        <td><input name="option-jdbcPassword" id="<portlet:namespace/>option-jdbcPassword" type="password"
                   size="20" value="${realm.options['jdbcPassword']}"></td>
      </tr>
      <tr>
        <td></td>
        <td><fmt:message key="realmwizard._sql.JDBCPasswordExp" /></td>
      </tr>
      <tr>
        <th><div align="right"><label for="<portlet:namespace/>confirm-option-jdbcPassword"><fmt:message key="consolebase.common.confirmPassword"/></label></div></th>
        <td><input name="confirm-option-jdbcPassword" id="<portlet:namespace/>confirm-option-jdbcPassword" type="password"
                   size="20" value="${realm.options['jdbcPassword']}"></td>
      </tr>

