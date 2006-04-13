<%@ page import="java.io.PrintWriter"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<c:if test="${! outcome}"><pre>${outcome}</pre></c:if>
<form enctype="multipart/form-data" method="POST" action="<portlet:actionURL><portlet:param name="action" value="deploy"/></portlet:actionURL>">
<table>
  <tr><th align="right">Archive: </th><td><input type="file" name="module" /></td></tr>
  <tr><th align="right">Plan: </th><td><input type="file" name="plan" /></td></tr>
  <tr>
    <td></td>
    <td><input name="startApp" type="checkbox" value="yes" checked>Start app after install</td>
  </tr>
  <tr>
    <td></td>
    <td><input type="submit" value="Install" /></td>
  </tr>
</table>
</form>