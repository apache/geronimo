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
<%@ page contentType="application/xhtml+xml" %>
<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f" %>
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en">
    <head>
        <title>Hello World</title>
    </head>

    <body>
        <f:view>
            <h:form id="form">
              <h:outputText id="output1" value="#{helloWorldBacking.testDouble} Please enter your name"/>
              <h:inputText id="input1" value="#{helloWorldBacking.name}" required="true"/>
              <h:outputText value="#{helloWorldBacking.greeting}"/>
              <h:commandButton id="button1" value="press me" action="#{helloWorldBacking.send}"/>
              <h:commandLink value="Update greeting" actionListener="#{helloWorldBacking.updateGreeting}"/>
              <h:message id="message1" for="input1"/>
            </h:form>
        </f:view>
    </body>
</html>
