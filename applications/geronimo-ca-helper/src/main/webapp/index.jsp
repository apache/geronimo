<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="org.apache.geronimo.ca.helper.util.CAHelperUtils"%>
<%
    Object certReqStore = CAHelperUtils.getCertificateRequestStore();
    Object certStore = CAHelperUtils.getCertificateStore();
%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>CA Helper Application</title>
</head>
<body>
<h2>CA Helper Application</h2>
<p>Welcome to CA Helper application. <p>

<%if(certReqStore == null) {%>
<p>A ceritificate request store is not available.  Application can not receive Certificate Signing Requests.</p>
<%}%>
<%if(certStore == null) {%>
<p>A ceritificate store is not available.  Application can not upload certificates.</p>
<%}%>
<%if(certReqStore == null || certStore == null) {%>
<p>Please contact the system administrator.</p>
<%} else {%>
<!-- The following is used to detect if the browser supports KEYGEN tag -->
<div style="display:none"><form name='keygentest'><keygen name="test"/></form></div>
<script>
if(document.keygentest.elements.length == 0) {
    document.write('Your browser does not support KEYGEN tag.  This application requires a browser that supports KEYGEN.');
} else {
    document.write('<p>This application allows you to submit certificate requests, download and install certificates issued by the CA.</p>');
    document.write(
    '<table border="0">'+
      '<tr>'+
        '<td>&nbsp;<a href="requestCertificate.jsp">Request Certificate</a>&nbsp;</td>'+
        '<td>&nbsp;<a href="downloadCertificate.jsp">Download your Certificate</a>&nbsp;</td>'+
        '<td>&nbsp;<a href="downloadCACertificate.jsp">Download CA Certificate</a>&nbsp;</td>'+
      '</tr>'+
    '</table>'
    )
}
</script>
<%}%>
</body>
</html>
