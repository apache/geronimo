<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>


<%@ page  session="true" %>

<html>
<head>
    <title>Geronimo Hello World Enterprise Application</title>
    <c:url value="/greeting.css"  var="stylesheet"/>
    <link rel="stylesheet" type="text/css" href="${stylesheet}">
</head>
    <body>
    <table width="600">
        <tr>
            <td class="mainHead" colspan="6">Geronimo Hello World Enterprise Application</td>
        </tr>
    </table>

    <ol>
        <c:forEach var="greeting" items="${greetings}">
            <li width="500" align="left">${greeting}</li>
        </c:forEach>
    </ol>

    </body>
</html>
