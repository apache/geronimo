<?xml version="1.0"?>
<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at
    
     http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->
<jsp:root xmlns:jsp="http://java.sun.com/JSP/Page" version="1.2">
<jsp:directive.page contentType="text/html"/>

<jsp:directive.page import="java.io.IOException"/>
<jsp:directive.page import="javax.naming.InitialContext"/>
<jsp:directive.page import="javax.xml.rpc.Service"/>
<jsp:directive.page import="org.apache.hello_world_soap_http.Greeter"/>

<html><head><title>JAX-RPC Client JSP</title></head>
<body>

<jsp:scriptlet>
        try {
            String name = request.getParameter("name");
            if (name == null) {
                name = "Unknown";
            }
            System.out.println(name);
            InitialContext ctx = new InitialContext();
            Service service = (Service)ctx.lookup("java:comp/env/services/Greeter");
            Greeter greeter = (Greeter)service.getPort(Greeter.class);
            out.println("WebService returned: " + greeter.greetMe(name));
        } catch (Exception e) {
            e.printStackTrace();
            IOException exception = new IOException("Error");
            exception.initCause(e);
            throw exception;
        }
</jsp:scriptlet>

</body></html>
</jsp:root>
