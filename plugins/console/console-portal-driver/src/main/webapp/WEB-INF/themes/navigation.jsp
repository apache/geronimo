<%--
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://portals.apache.org/pluto" prefix="pluto" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt_rt" prefix="fmt"%>
<fmt:setLocale value="<%=request.getLocale()%>"/>
<fmt:setBundle basename="org.apache.geronimo.console.i18n.ConsoleResource"/>

<%@ page import="java.util.ArrayList,java.util.HashMap,org.apache.pluto.driver.services.portal.PageConfig" %>


<%
    //Get everything into the map and ready.
    String delim = "/";
    int delim_len = delim.length();

    HashMap<String, ArrayList<PageConfig>> categories = new HashMap<String, ArrayList<PageConfig>>();
    ArrayList<String> catNames = new ArrayList<String>();
    HashMap<String, String> shortNames = new HashMap<String, String>();
    PageConfig welcomePageConfig = null;
    String welcomePageName = "Welcome";
%>        
    <c:forEach var="page" items="${driverConfig.pages}">
    <%
        PageConfig pageConfig = (PageConfig)(pageContext.getAttribute("page"));
        String str = pageConfig.getName();
        String cat;
        String pageName;
        
        if(!str.equals(welcomePageName)){
            int index = str.indexOf(delim);
            if(index != -1){
                cat = str.substring(0, index);
                pageName = str.substring(index+delim_len);
            }else{
                cat = "Other";
                pageName = str;
            }
            
            if(categories.get(cat)==null){
                categories.put(cat, new ArrayList<PageConfig>());
                catNames.add(cat);
            }
            categories.get(cat).add(pageConfig);
            shortNames.put(str,pageName);
        }else{
            welcomePageConfig = pageConfig;
        }
    %>
    </c:forEach>




<table width="200px" border="0" cellpadding="0" cellspacing="0"> 
    <tr><td CLASS="ReallyDarkBackground"><strong>&nbsp;<fmt:message key="Console Navigation"/></strong></td></tr>
    <tr><td><div class="Selection">
        <table width="100%" border="0" cellpadding="0" cellspacing="0">
            <tr>
                <td class="CollapsedLeft"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1" alt=""/></td>
                <td class="Indent"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1" alt=""/></td>
                <td class="TopMiddle"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1" alt=""/></td>
                <td class="CollapsedRight"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1" alt=""/></td> 
            </tr>
        </table>
    </div></td></tr>
    
    <%
    String pageName;
    if(welcomePageConfig!=null){
        pageContext.setAttribute("page",welcomePageConfig);
        pageName = welcomePageName;
    %>
        <!-- Add the Welcome Link -->
        <c:choose>
            <c:when test="${page == currentPage}">
                <tr><td><div class="SelectedSubselection Selection"></c:when>
            <c:otherwise>
                <tr><td><div class="Selection"></c:otherwise>
        </c:choose>
                <table width="100%" border="0" cellpadding="1" cellspacing="0"> 
                    <tr>
                        <td class="CollapsedLeft">&nbsp;</td>
                        <td class="Indent">&nbsp;</td>
                        <td class="TopMiddle">
                            <img border="0" src="<%=request.getContextPath()%>/images/ico_geronimo_16x16.gif" alt=""/>&nbsp;<a href='<c:out value="${pageContext.request.contextPath}"/>/portal/<c:out value="${page.name}"/>'><fmt:message key="<%=pageName%>"/></a>
                        </td>
                        <td class="CollapsedRight">&nbsp;</td> 
                    </tr> 
                </table>
            </div></td></tr>
        <%}
    %>
<%
    //generate the output tree
    int catNames_len = catNames.size();
    for(int i=0;i<catNames_len;i++){
        String catName = catNames.get(i);
        pageContext.setAttribute("catName",catName);
        %>
        <tr><td><div class="Selection">
            <table width="100%" border="0" cellpadding="1" cellspacing="0">
                <tr>
                    <td class="CollapsedLeft">&nbsp;</td>
                    <td class="Indent">&nbsp;</td>
                    <td class="TopMiddle">
                        <img border="0" src="<%=request.getContextPath()%>/images/ico_folder_16x16.gif" alt=""/>&nbsp;<fmt:message key="<%=catName%>"/>
                    </td>
                    <td class="CollapsedRight">&nbsp;</td> 
                </tr>
            </table>
        </div></td></tr>
        <%
        ArrayList<PageConfig> list = categories.get(catName);
        int list_len = list.size();
        for(int k=0;k<list_len;k++){
            //System.out.println("\t"+list.get(k));
            PageConfig pageConfig = list.get(k);
            pageContext.setAttribute("page",pageConfig);
            pageName = shortNames.get(pageConfig.getName());
            String icon = pageConfig.getIcon();
            if (icon == null || icon.trim().length() == 0) {
               icon = "/images/ico_doc_16x16.gif";
            }
    %>
    <c:choose>
        <c:when test="${page == currentPage}">
            <tr><td><div class="SelectedSubselection Subselection"></c:when>
        <c:otherwise>
            <tr><td><div class="Subselection"></c:otherwise>
    </c:choose>
                <table width="100%" border="0" cellpadding="1" cellspacing="0"> 
                    <tr>
                        <td class="Left">&nbsp;</td> 
                        <td class="Indent">&nbsp;</td> 
                        <td class="Middle">
                            &nbsp;&nbsp;&nbsp;
                            <img border="0" src="<%=request.getContextPath()%><%=icon%>" alt=""/>&nbsp;<a href='<c:out value="${pageContext.request.contextPath}"/>/portal/<c:out value="${page.name}"/>'><fmt:message key="<%=pageName%>"/></a>
                        </td> 
                        <td class="Right">&nbsp;</td> 
                    </tr> 
                </table>
            </div></td></tr>
            <%
        }
    }
%>
    <tr><td><div class="Selection"><table width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
            <td class="CollapsedLeft"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1" alt=""/></td>
            <td class="Indent"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1" alt=""/></td>
            <td class="TopMiddle"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1" alt=""/></td>
            <td class="CollapsedRight"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1" alt=""/></td> 
        </tr>
    </table></div></td></tr>
</table>

