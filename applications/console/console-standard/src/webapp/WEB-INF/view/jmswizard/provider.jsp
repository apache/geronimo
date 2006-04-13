<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p><b>JMS Resource Group</b> -- Select JMS Provider RAR</p>

<!--   FORM TO COLLECT DATA FOR THIS PAGE   -->
<form name="<portlet:namespace/>JMSForm" action="<portlet:actionURL/>">
    <input type="hidden" name="mode" value="provider-after" />
    <table border="0">
    <!-- ENTRY FIELD: JMS Connection RAR -->
      <tr>
        <th><div align="right">JMS Provider RAR:</div></th>
        <td>
          <select name="rar">
            <option></option>
        <c:forEach var="rar" items="${rars}">
            <option <c:if test="${rar == pool.rar}">selected</c:if>>${rar}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>The Resource Adapter RAR that can be used to connect to the JMS provider
            in question.  This RAR should be installed under GERONIMO/repository/ in
            order for it to appear in this list.
        </td>
      </tr>
    <!-- SUBMIT BUTTON -->
      <tr>
        <td></td>
        <td><input type="submit" value="Next" /></td>
      </tr>
    </table>
</form>
<!--   END OF FORM TO COLLECT DATA FOR THIS PAGE   -->


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list-before" />
            </portlet:actionURL>">Cancel</a></p>
