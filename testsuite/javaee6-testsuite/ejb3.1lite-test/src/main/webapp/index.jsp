

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

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>A Singleton Session Sample - Calculator</title>
    </head>
    <body>
      <h2>A Singleton Session Sample</h2>
      <h3>Calculator</h3>
      <font style="font-size:12px">
      <p>
      <%
          String result = String.valueOf(request.getAttribute("result"));
          result = ("null".equals(result)) ? "0" : result;
          request.setAttribute("result", null);
          request.removeAttribute("result");
      %>
      </p>
      
      <form action="CalculatorServlet" method="get">
      <table border="3" id="mainTable" cellpadding="0">
        <thead id="mainTableHead">
        <tr>
          <td class="header">Number</td>
          <td class="header">Operation</td>
          <td class="header">Result</td>
        </tr>
        </thead>
        <tr>
          <td class="cell">
            <input type="text" name="NumberValue" value="0" />
          </td>
          <td class="cell">
            <input type="submit" name="operation" value="add" />
            <br />
            <input type="submit" name="operation" value="sub" />
          </td>
          <td class="cell">
            <div id="result" ><%=result%></div></td>
        </tr>
      </table>
    </form>
    <p>
        This sample shows an EJB 3.1 Lite Singleton session bean.
    </p>
     <p>
          It also presents "Local/No Interface","no ejb-jar.xml","one war package" features.
     </p>

      </font>
    </body>
</html>
