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
    	<title>Test Interceptor</title>
  </head>
  <body>
    <h2>An Interceptor Sample</h2>
    <h3>Test Section One</h3>
    <p>
      <%
          String intcpt1 = String.valueOf(request.getAttribute("Intcpt1IsValid"));
          intcpt1 = (intcpt1.equals("null")||intcpt1.length()<=0) ? "uncheck" : intcpt1;
          request.setAttribute("Intcpt1IsValid", null);
          request.removeAttribute("Intcpt1IsValid");

          String intcpt2 = String.valueOf(request.getAttribute("Intcpt2IsValid"));
          intcpt2 = (intcpt2.equals("null")||intcpt1.length()<=0) ? "uncheck" : intcpt2;
          request.setAttribute("Intcpt2IsValid", null);
          request.removeAttribute("Intcpt2IsValid");

          String date1 = String.valueOf(request.getAttribute("date1"));
          date1 = (date1.equals("null")||date1.length()<=0) ? "uncheck" : date1;
          request.setAttribute("date1", null);
          request.removeAttribute("date1");

          String date2 = String.valueOf(request.getAttribute("date2"));
          date2 = (date2.equals("null")||date2.length()<=0) ? "uncheck" : date2;
          request.setAttribute("date2", null);
          request.removeAttribute("date2");

          String sysmi1 = String.valueOf(request.getAttribute("sysmi1"));
          sysmi1 = (sysmi1.equals("null")||sysmi1.length()<=0) ? "uncheck" : sysmi1;
          request.setAttribute("sysmi1", null);
          request.removeAttribute("sysmi1");

          String sysmi2 = String.valueOf(request.getAttribute("sysmi2"));
          sysmi2 = (sysmi2.equals("null")||sysmi2.length()<=0) ? "uncheck" : sysmi2;
          request.setAttribute("sysmi2", null);
          request.removeAttribute("sysmi2");
      %>
    </p>
    <form action="intcptServlet" method="get">
      <table border="3" cellpadding="1">
        <tr>
          <th>Input</th>
          <th>Operation</th>
        </tr>
        <tr>
          <td class="cell">
            <input type="text" name="NumberValue" value="0" />
          </td>
          <td class="cell">
            <input type="submit" name="operation" value="check"/>
          </td>
        </tr>      
      </table>
      <table border="3" cellpadding="1">
          <tr>
              <th>Interceptor 1</th>
              <th>Time 1</th>
              <th>SystemTimeMillis 1</th>
          <tr/>
          <tr>
              <td class="cell" id="intcpt1"><%=intcpt1 %></td>
              <td class="cell" id="time1"><%=date1%></td>
              <td class="cell" id="sys1"><%=sysmi1%></td>
          </tr>
      </table>
      <table border="3" cellpadding="1">
          <tr>
          <th>Interceptor 2</th>
          <th>Time 2</th>
          <th>SystemTimeMillis 2</th>
          <tr/>
          <tr>
              <td class="cell" id="intcpt2"><%=intcpt2 %></td>
              <td class="cell" id="time2"><%=date2%></td>
              <td class="cell" id="sys2"><%=sysmi2%></td>
          </tr>
      </table>
    </form>

    <br/>
    <h3>Test Section Two</h3>
    <p>Click here to check @AroundTimeout</p>
    <form action="timeoutIntcptServlet" method="get">
        <table border="3" cellpadding="1">
            <tr>
                <td>
                <input type="submit" name="checkAroundTimeout" value="check"/>
                </td>
            </tr>
        </table>
    </form>

    <br/>
    <h3>Test Section Three</h3>
    <p>Click here to check @Interceptor, @InterceptorBinding</p>
    <form action="intcptBindingServlet" method="get">
                <table border="3" cellpadding="1">
            <tr>
                <td>
                <input type="submit" name="checkIntcptBind" value="check"/>
                </td>
            </tr>
        </table>
        </form>

    <p>
        <br />
        <b>1.This example checks the input value in Section One.</b>
        <br/>
        &nbsp&nbsp Interceptor 1 says valid & Interceptor 2 says invalid if the value is greater than or equal zero.
        <br/>
        &nbsp&nbsp Interceptor 1 says invalid & Interceptor 2 says valid if the value is less than zero.
        <br/>
        <b>2.This example also present the invoking sequence of the interceptors.</b>
        <br/>
        &nbsp&nbsp It should firstly invoke Interceptor 1 and Interceptor 2 happens afterwards.
        <br/>
         &nbsp&nbsp You can check the invoking sequence through Time and SystemTimeMillis.
        <br/>
        <b>3.This example presents the @AroundTimeout annotation in interceptor-1.1 in Section Two.</b>
        <br/>
        <b>4.This example presents the @Interceptor and @InterceptorBinding annotation in interceptor-1.1 in Section Three.</b>
        <br/>
    </p>
  </body>
</html> 
