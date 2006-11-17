<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<c:if test="${!empty(infoMsg)}">
<p><font color="blue"><b>${infoMsg}</b></font></p>
</c:if>

<c:if test="${!empty(errorMsg)}">
<p><font color="red"><b>Error: ${errorMsg}</b></font></p>
</c:if>
