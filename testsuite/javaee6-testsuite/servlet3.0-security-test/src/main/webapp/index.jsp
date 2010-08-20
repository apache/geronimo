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
        <title>Programatic Security Sample</title>
    </head>
    <body>
        <h1>This example tests Programatic Security:</h1>
        <h2>Test Login, Logout Method:</h2>
        <form name="login" method="GET" action="LoginServlet">

            <br/><br/>
            Username:<input type="text" name="UserName" value="" /><br>
            Password:<input type="password" name="Password" value="" /><br>
            <br/>
            <input type="submit" value="Login" />
         </form>
        <br/>
    </body>
</html>