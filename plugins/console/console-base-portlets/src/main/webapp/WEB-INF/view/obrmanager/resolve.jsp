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
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<fmt:setBundle basename="consolebase"/>

<c:choose>
    <c:when test="${resolved == true}">
        <h2>All resources resolved successfully</h2>

        <b>Resolved resources:</b>
        <table width="100%" class="TableLine" summary="Resolved Resources">
            <tr class="DarkBackground">
                <th scope="col" width="80%">Symbolic Name</th>
                <th scope="col" width="20%">Version</th>
            </tr>

            <c:set var="backgroundClass" value='MediumBackground' />
            <c:forEach var="resource" items="${resolver.addedResources}">
                <c:choose>
                    <c:when test="${backgroundClass == 'MediumBackground'}">
                        <c:set var="backgroundClass" value='LightBackground' />
                    </c:when>
                    <c:otherwise>
                        <c:set var="backgroundClass" value='MediumBackground' />
                    </c:otherwise>
                </c:choose>

                <tr class="${backgroundClass}" onmouseover="highlightBgColor(this)" onmouseout="recoverBgColor(this)">
                    <!-- resource name -->
                    <td>${resource.symbolicName}</td>

                    <!-- resource version -->
                    <td>${resource.version}</td>
                </tr>

            </c:forEach>
        </table>

        <c:choose>
            <c:when test="${empty(resolver.requiredResources) && empty(resolver.optionalResources)}">
                <!-- no additional dependencies -->
            </c:when>
            <c:otherwise>
                <br />
                <b>Additional dependent resources:</b>
                <table width="100%" class="TableLine" summary="Required Resources">
                    <tr class="DarkBackground">
                        <th scope="col" width="70%">Symbolic Name</th>
                        <th scope="col" width="20%">Version</th>
                        <th scope="col" width="10%">Required</th>
                    </tr>

                    <c:set var="backgroundClass" value='MediumBackground' />

                    <c:if test="${!empty(resolver.requiredResources)}">
                        <c:forEach var="resource" items="${resolver.requiredResources}">
                            <c:choose>
                                <c:when test="${backgroundClass == 'MediumBackground'}">
                                    <c:set var="backgroundClass" value='LightBackground' />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="backgroundClass" value='MediumBackground' />
                                </c:otherwise>
                            </c:choose>

                            <tr class="${backgroundClass}" onmouseover="highlightBgColor(this)"
                                onmouseout="recoverBgColor(this)">
                                <!-- resource name -->
                                <td>${resource.symbolicName}</td>

                                <!-- resource version -->
                                <td>${resource.version}</td>

                                <td>Yes</td>
                            </tr>
                        </c:forEach>
                    </c:if>

                    <c:if test="${!empty(resolver.optionalResources)}">
                        <c:forEach var="resource" items="${resolver.optionalResources}">
                            <c:choose>
                                <c:when test="${backgroundClass == 'MediumBackground'}">
                                    <c:set var="backgroundClass" value='LightBackground' />
                                </c:when>
                                <c:otherwise>
                                    <c:set var="backgroundClass" value='MediumBackground' />
                                </c:otherwise>
                            </c:choose>

                            <tr class="${backgroundClass}" onmouseover="highlightBgColor(this)"
                                onmouseout="recoverBgColor(this)">
                                <!-- resource name -->
                                <td>${resource.symbolicName}</td>

                                <!-- resource version -->
                                <td>${resource.version}</td>

                                <td>No</td>
                            </tr>
                        </c:forEach>
                    </c:if>

                </table>
            </c:otherwise>
        </c:choose>
        
        <p/>
        
        <form name="resolve-form" action="<portlet:actionURL/>">
            <input type="submit" value='<fmt:message key="consolebase.common.return"/>' onclick="history.go(-1); return false;" />
        </form>

    </c:when>

    <c:otherwise>
       <h2>One or more resources cannot be resolved</h2>
       
       <b>Unsatisfied requirements:</b>
       <table width="100%" class="TableLine" summary="Unsatisfied Requirements">
            <tr class="DarkBackground">
                <th scope="col" width="20%">Symbolic Name</th>
                <th scope="col" width="80%">Unsatisfied Requirement</th>
            </tr>

            <c:set var="backgroundClass" value='MediumBackground' />
            <c:forEach var="reason" items="${resolver.unsatisfiedRequirements}">
                <c:choose>
                    <c:when test="${backgroundClass == 'MediumBackground'}">
                        <c:set var="backgroundClass" value='LightBackground' />
                    </c:when>
                    <c:otherwise>
                        <c:set var="backgroundClass" value='MediumBackground' />
                    </c:otherwise>
                </c:choose>

                <tr class="${backgroundClass}" onmouseover="highlightBgColor(this)" onmouseout="recoverBgColor(this)">
                    <!-- resource name -->
                    <td>${reason.resource.symbolicName}</td>

                    <!-- requirement -->
                    <td>${reason.requirement.filter}</td>
                </tr>

            </c:forEach>
        </table>
        
        <p/>
        
        <form name="resolve-form" action="<portlet:actionURL/>">
            <input type="submit" value='<fmt:message key="consolebase.common.return"/>' onclick="history.go(-1); return false;" />
        </form>
    </c:otherwise>
</c:choose>
