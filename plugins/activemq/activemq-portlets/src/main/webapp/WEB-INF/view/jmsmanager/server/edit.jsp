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
<fmt:setBundle basename="activemq"/>
<portlet:defineObjects/>

<script language="JavaScript">
    var <portlet:namespace/>formName = "<portlet:namespace/>ActiveMQForm";
    var <portlet:namespace/>requiredFields = new Array("brokerName", "configXML");
    function <portlet:namespace/>validateForm() {
        if(!textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields)) {
            addErrorMessage("<portlet:namespace/>", '<fmt:message key="jmsmanager.broker.emptyText"/>');
            return false;    
        }
        return true;
    }
</script>

<div id="<portlet:namespace/>CommonMsgContainer"></div><br>

<form name="<portlet:namespace/>ActiveMQForm" action="<portlet:actionURL/>" onsubmit="return <portlet:namespace/>validateForm();" method="post">
<input type="hidden" name="mode" value="${mode}"/>
<table width="100%" align="center" border="0" cellspacing="4">    		
    <tr>
        <td><fmt:message key="jmsmanager.common.broker"/>&nbsp;<fmt:message key="jmsmanager.common.name"/>:
           <c:choose>
                <c:when test="${mode eq 'create'}">
                    <input type="text" value="" name="brokerName" id="<portlet:namespace/>brokerName"/>
                </c:when>
                <c:otherwise>
                    <span>
                        <c:out value="${brokerWrapper.brokerName}" default=""/>
                        <input type="hidden" name="brokerName" value="${brokerWrapper.brokerName}"/>
                        <input type="hidden" name="brokerURI" value="${brokerWrapper.brokerURI}"/>
                    </span>    
                </c:otherwise>        
            </c:choose> 
        </td>
    </tr>
    <tr>
        <td>
            <c:choose>
                <c:when test="${mode eq 'create'}">                    
                    <fmt:message key="jmsmanager.broker.creationtip"/>
                </c:when>
                <c:otherwise>
                    <fmt:message key="jmsmanager.broker.updatetip"/>
                </c:otherwise>                
            </c:choose>
            <br/>
         </td>
    </tr>
    <tr>
        <td>            
            <textarea id="<portlet:namespace/>configXML" name="configXML" cols="120" rows="40"><c:out value="${configXML}" default=""/></textarea>
        </td>
    </tr>
    <tr>
        <td colspan="2">        
            <input name="submit" type="submit" value='<fmt:message key="jmsmanager.common.save"/>'/>                   
            <input name="reset" type="reset" value='<fmt:message key="jmsmanager.common.reset"/>'/>
        </td>
    </tr>
</table>                
</table>
</form>
        
