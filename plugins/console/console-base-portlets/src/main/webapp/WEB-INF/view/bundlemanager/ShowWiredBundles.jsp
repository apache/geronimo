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
<%@ taglib uri="/WEB-INF/CommonMsg.tld" prefix="CommonMsg"%>
<fmt:setBundle basename="consolebase"/>
<portlet:defineObjects/>

<form id="perspectiveForm" method="POST" action="<portlet:actionURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:actionURL>">
    <a href="<portlet:actionURL/>" >OSGi Manager</a> > Show Wired Bundles &nbsp;
    (
    <input type="radio" name="perspectiveType" value="package" onclick="submitPerspectiveForm(this)"  />Package Perspective
    /
    <input type="radio" name="perspectiveType" value="bundle" onclick="submitPerspectiveForm(this)"  />Bundle Perspective
    )
</form>
<script language="javascript">    
     var radiogroup = document.getElementsByName("perspectiveType");  
     for(var i=0;i<radiogroup.length;i++){    
         if(radiogroup[i].value == "${perspectiveTypeValue}"){    
               radiogroup[i].checked = true;  
         }    
     }
</script>
<script language="javascript">
    function submitPerspectiveForm(radiobutton){
        if (radiobutton.value != "${perspectiveTypeValue}"){
            document.getElementById("perspectiveForm").submit();
        }
    }
</script>
<br/>
<table width="100%" class="BlankTableLine" summary="OSGi install">
    <tr>
        <td>
            The Wired Bundles of Bundle:
            &nbsp;
            <b>
            ${bundleInfo.symbolicName}
            </b>
            (id=${bundleInfo.bundleId})
            (version=${bundleInfo.bundleVersion})
            [${bundleInfo.state}]
        </td>
        <td align="right">
            View:&nbsp;
            <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>">Manifest</a>
            &nbsp;|&nbsp;
            <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${bundleInfo.bundleId}'/></portlet:renderURL>">Services</a>
        </td>
    </tr>
</table>
<br/>
<!-- ################## Package Perspective ################### -->
<c:if test="${perspectiveTypeValue == 'package'}" > 
    <!--  Importing  -->
    Import packages:<br/>
    <table width="100%" class="TableLine" summary="Wired Bundles">
        <tr class="DarkBackground">
            <th scope="col" width="40%">Importing Packages</th>   
            <th scope="col" width="60%">From Bundles</th> 
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="ipp" items="${importingPackagePerspectives}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">
                ${ipp.packageInfo.packageName} (version=${ipp.packageInfo.packageVersion})
            </td>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${ipp.bundleInfos}">
                    ${info.symbolicName} (id=${info.bundleId}) (version=${info.bundleVersion})
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
                    <br/>
                </c:forEach>
            </td>
          </tr>
        </c:forEach>
    </table>
    <br/>
    <!-- Dynamic Importing -->
    Dynamic import packages:<br/>
    <table width="100%" class="TableLine" summary="Wired Bundles">
        <tr class="DarkBackground">
            <th scope="col" width="40%">Dynamic Importing Packages</th>   
            <th scope="col" width="60%">From Bundles</th> 
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="dipp" items="${dynamicImportingPackagePerspectives}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">
                ${dipp.packageInfo.packageName} (version=${dipp.packageInfo.packageVersion})
            </td>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${dipp.bundleInfos}">
                    ${info.symbolicName} (id=${info.bundleId}) (version=${info.bundleVersion})
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
                    <br/>
                </c:forEach>
            </td>
          </tr>
        </c:forEach>
    </table>
    <br/>
    <!-- Require Bundles -->
    Import packages from the require bundles:<br/>
    <table width="100%" class="TableLine" summary="Wired Bundles">
        <tr class="DarkBackground">
            <th scope="col" width="40%">Importing Packages</th>   
            <th scope="col" width="60%">From the Require Bundles</th> 
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="rbipp" items="${requireBundlesImportingPackagePerspectives}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">
                ${rbipp.packageInfo.packageName} (version=${rbipp.packageInfo.packageVersion})
            </td>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${rbipp.bundleInfos}">
                    ${info.symbolicName} (id=${info.bundleId}) (version=${info.bundleVersion})
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
                    <br/>
                </c:forEach>
            </td>
          </tr>
        </c:forEach>
    </table>
    <br/>
    <!-- Exporting -->
    Export packages:<br/>
    <table width="100%" class="TableLine" summary="Wired Bundles">
        <tr class="DarkBackground">
            <th scope="col" width="40%">Exporting Packages</th>   
            <th scope="col" width="60%">To Bundles</th> 
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="epp" items="${exportingPackagePerspectives}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">
                ${epp.packageInfo.packageName} (version=${epp.packageInfo.packageVersion})
            </td>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${epp.bundleInfos}">
                    ${info.symbolicName} (id=${info.bundleId}) (version=${info.bundleVersion})
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                    <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${info.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
                    <br/>
                </c:forEach>
            </td>
          </tr>
        </c:forEach>
    </table>
</c:if>
<!-- ################## Bundle Perspective ################### -->
<c:if test="${perspectiveTypeValue == 'bundle'}" > 
    <!--  Importing  -->
    Import packages:<br/>
    <table width="100%" class="TableLine" summary="Wired Bundles">
        <tr class="DarkBackground">
            <th scope="col" width="40%">Importing Packages</th>   
            <th scope="col" width="60%">From Bundles</th> 
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="ibp" items="${importingBundlePerspectives}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${ibp.packageInfos}">
                    ${info.packageName} (version=${info.packageVersion}) <br/>
                </c:forEach>
            </td>
            <td class="${backgroundClass}">
                ${ibp.bundleInfo.symbolicName} (id=${ibp.bundleInfo.bundleId}) (version=${ibp.bundleInfo.bundleVersion})
                <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${ibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${ibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${ibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
            </td>
          </tr>
        </c:forEach>
    </table>
    <br/>
    <!-- Dynamic Importing -->
    Dynamic import packages:<br/>
    <table width="100%" class="TableLine" summary="Wired Bundles">
        <tr class="DarkBackground">
            <th scope="col" width="40%">Dynamic Importing Packages</th>   
            <th scope="col" width="60%">From Bundles</th> 
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="dibp" items="${dynamicImportingBundlePerspectives}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${dibp.packageInfos}">
                    ${info.packageName} (version=${info.packageVersion}) <br/>
                </c:forEach>
            </td>
            <td class="${backgroundClass}">
                ${dibp.bundleInfo.symbolicName} (id=${dibp.bundleInfo.bundleId}) (version=${dibp.bundleInfo.bundleVersion})
                <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${dibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${dibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${dibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
            </td>
          </tr>
        </c:forEach>
    </table>
    <br/>
    <!-- Require Bundles -->
    Import packages from the require bundles:<br/>
    <table width="100%" class="TableLine" summary="Wired Bundles">
        <tr class="DarkBackground">
            <th scope="col" width="40%">Importing Packages</th>   
            <th scope="col" width="60%">From the Require Bundles</th> 
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="rbibp" items="${requireBundlesImportingBundlePerspectives}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${rbibp.packageInfos}">
                    ${info.packageName} (version=${info.packageVersion}) <br/>
                </c:forEach>
            </td>
            <td class="${backgroundClass}">
                ${rbibp.bundleInfo.symbolicName} (id=${rbibp.bundleInfo.bundleId}) (version=${rbibp.bundleInfo.bundleVersion})
                <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${rbibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${rbibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${rbibp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
            </td>
          </tr>
        </c:forEach>
    </table>
    <br/>
    <!-- Exporting -->
    Export packages:<br/>
    <table width="100%" class="TableLine" summary="Wired Bundles">
        <tr class="DarkBackground">
            <th scope="col" width="40%">Exporting Packages</th>   
            <th scope="col" width="60%">To Bundles</th> 
        </tr>
        <c:set var="backgroundClass" value='MediumBackground'/>
        <c:forEach var="ebp" items="${exportingBundlePerspectives}">
          <c:choose>
              <c:when test="${backgroundClass == 'MediumBackground'}" >
                  <c:set var="backgroundClass" value='LightBackground'/>
              </c:when>
              <c:otherwise>
                  <c:set var="backgroundClass" value='MediumBackground'/>
              </c:otherwise>
          </c:choose>
          <tr>
            <td class="${backgroundClass}">
                <c:forEach var="info" items="${ebp.packageInfos}">
                    ${info.packageName} (version=${info.packageVersion}) <br/>
                </c:forEach>
            </td>
            <td class="${backgroundClass}">
                ${ebp.bundleInfo.symbolicName} (id=${ebp.bundleInfo.bundleId}) (version=${ebp.bundleInfo.bundleVersion})
                <a href="<portlet:renderURL><portlet:param name='page' value='view_manifest'/><portlet:param name='bundleId' value='${ebp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_mf.png" alt="icon_mf.png" title="View Manifest" style="vertical-align:middle"/></a>
                <a href="<portlet:renderURL><portlet:param name='page' value='view_wired_bundles'/><portlet:param name='bundleId' value='${ebp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_wb.png" alt="icon_wb.png" title="View Wired Bundles" style="vertical-align:middle"/></a>
                <a href="<portlet:renderURL><portlet:param name='page' value='view_services'/><portlet:param name='bundleId' value='${ebp.bundleInfo.bundleId}'/></portlet:renderURL>"><img border="0" src="<%=request.getContextPath()%>/images/icon_serv.png" alt="icon_serv.png" title="View Services" style="vertical-align:middle"/></a>
            </td>
          </tr>
        </c:forEach>
    </table>
</c:if>