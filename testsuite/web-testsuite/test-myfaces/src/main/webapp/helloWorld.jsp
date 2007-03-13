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
              <h:panelGrid id="grid" columns="2">
                <h:outputText id="output1" value="Please enter your name"/>
                <h:inputText id="input1" value="#{helloWorldBacking.name}" required="true"/>
                <h:outputText value="#{helloWorldBacking.greeting}"/>
                <h:commandButton id="button1" value="press me" action="#{helloWorldBacking.send}"/>
                <h:commandLink value="Update greeting" actionListener="#{helloWorldBacking.updateGreeting}"/>
                <h:message id="message1" for="input1"/>
              </h:panelGrid>
            </h:form>
        </f:view>
    </body>
</html>
