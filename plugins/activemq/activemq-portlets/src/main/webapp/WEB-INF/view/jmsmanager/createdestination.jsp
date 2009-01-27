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
        var <portlet:namespace/>formName = "<portlet:namespace/>createDestinationForm";
        var <portlet:namespace/>requiredFields = new Array("destinationMessageDestinationName","destinationPhysicalName");
        function <portlet:namespace/>validateForm(){
            return (textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields));
        }
</script>
<script>
       function <portlet:namespace/>backToDestinationList(){

           document.<portlet:namespace/>createDestinationForm.action = "<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewDestinations"/></portlet:renderURL>";
           document.<portlet:namespace/>createDestinationForm.submit();

           return true;
       }
</script>

       <br>

        <form name="<portlet:namespace/>createDestinationForm" action="<portlet:actionURL portletMode="view"/>" method="POST">
        <input type=hidden name="processaction" value="createDestination"  >
       <table>

       <tr>
         <th colspan="2" align=LEFT>
            <fmt:message key="jmsmanager.common.addQueue_Topic" />
         </th>

       </tr>
         <tr>
             <td  align=LEFT>
                  <label for="<portlet:namespace/>destinationMessageDestinationName"><fmt:message key="jmsmanager.common.messageDestinationName" /></label>
             </td>
             <td  align=LEFT>
                   <input type="text" name="destinationMessageDestinationName" id="<portlet:namespace/>destinationMessageDestinationName" value=""/>
             </td>
         </tr>
         <tr>
             <td  align=LEFT>
                   <label for="<portlet:namespace/>destinationPhysicalName"><fmt:message key="jmsmanager.common.destinationPhysicalName" /></label>
             </td>
             <td  align=LEFT>
                   <input type="text" name="destinationPhysicalName" id="<portlet:namespace/>destinationPhysicalName" value=""/>
             </td>
         </tr>
         <tr>
             <td  align=LEFT>
                   <label for="<portlet:namespace/>destinationType"><fmt:message key="jmsmanager.common.type"/></label>
             </td>
             <td  align=LEFT>
               <select name="destinationType" id="<portlet:namespace/>destinationType">
                  <option value="javax.jms.Queue" selected="true" >QUEUE</option>
                  <option value="javax.jms.Topic" >TOPIC</option>
               </select>
             </td>
         </tr>
         <tr>
             <td  align=LEFT>
                   <label for="<portlet:namespace/>destinationApplicationName"><fmt:message key="jmsmanager.common.applicationName" /></label>
             </td>
             <td  align=LEFT>
                   <input type="text" name="destinationApplicationName" id="<portlet:namespace/>destinationApplicationName" value="null"/>
               </select>
             </td>
         </tr>
         <tr>
             <td  align=LEFT>
                    <label for="<portlet:namespace/>destinationModuleName"><fmt:message key="jmsmanager.common.moduleName" /></label>
             </td>
             <td  align=LEFT>
                   <input type="text" name="destinationModuleName" id="<portlet:namespace/>destinationModuleName" value="defaultJMS"/>
               </select>
             </td>
         </tr>
         <tr>
                <td colspan="2" align="center" class="formElement">
                <input type="submit" value='<fmt:message key="jmsmanager.common.submit"/>' 
                    onclick="return <portlet:namespace/>validateForm()">
                 <input type="reset" value='<fmt:message key="jmsmanager.common.clear"/>'> 
                <input type="submit" value='<fmt:message key="jmsmanager.common.back"/>'  onClick="<portlet:namespace/>backToDestinationList();">
                </td>
         </tr>
        </table>

        </form>

