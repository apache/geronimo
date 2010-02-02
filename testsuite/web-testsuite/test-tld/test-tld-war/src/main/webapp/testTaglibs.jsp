<!--
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
-->
<%@ page language="java" contentType="text/html; charset=ISO-8859-1" trimDirectiveWhitespaces="true" %>
<%@ taglib uri="http://geronimo.apache.org/local" prefix="localns" %>
<%@ taglib uri="http://geronimo.apache.org/web" prefix="webns" %>
<%@ taglib uri="http://geronimo.apache.org/jar" prefix="jarns" %>
<html>
<head><title>test</title></head>
<body>
<localns:local/>
<br/>
<webns:web/>
<br/>
<jarns:jar/>
<br/>
Listener initialized: <%= getServletContext().getAttribute("listener") %>
</body>
</html>
