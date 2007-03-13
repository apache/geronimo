<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h" %>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<html>
    <head>
        <title>Hello World</title>
    </head>
    <body>
        <f:view>        
            <f:verbatim><h2></f:verbatim>
            <h:outputText value="Hello #{helloWorldBacking.name}. We hope you enjoy Apache MyFaces"/>
            <f:verbatim></h2></f:verbatim>

            <h:form id="form2">
              <h:commandLink id="link1" action="back">
                <h:outputText id="linkText" value="GO HOME"/>
              </h:commandLink>
            </h:form>
        </f:view>
    </body>
</html>