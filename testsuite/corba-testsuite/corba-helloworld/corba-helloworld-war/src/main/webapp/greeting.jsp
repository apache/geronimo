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
