<%@ taglib uri="http://java.sun.com/jsf/html" prefix="h"%>
<%@ taglib uri="http://java.sun.com/jsf/core" prefix="f"%>
<html>
<head>
	<title>Label Test</title>
</head>

<body>
<f:view>
	<h:form id="labelTestForm">
		<h:messages />
		<br>
		<h:inputText id="txt_text" value="#{labelTest.text}" label="Text" required="true"></h:inputText>
		<h:message for="txt_text"></h:message>
		<br>
		<h:commandButton value="Test" />
	</h:form>
</f:view>
</body>
</html>
