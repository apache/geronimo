<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Confirm CSR</title>
</head>
<body>
<h2>CSR Confirmation</h2>
<p>Your Certificate Signing Request(CSR) is received by the CA. Your CSR Id is <b>${id}</b>. Please note down your
CSR Id. It will be required later on to download your certificate.</p>

<a href="<%=request.getContextPath()%>">Back to CA Helper home</a>
</body>
</html>
