<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
    pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>Download Certificate</title>
<script language="JavaScript">
    function validateForm() {
        obj = document.forms[0].csrId;
        if(obj.value == '') {
            alert('csrId can not be empty');
            obj.focus();
            return false;
        }
        return true;
    }
</script>
</head>
<body>
<h2>Download Certificate</h2>
<p>This page enables you to download and install certificate issued to you by the CA.  Before installing your certificate,
install the CA's certificate in your web browser by clicking on the <a href="DownloadCertificateServlet?type=ca"> this link</a>.</p>

<form action="DownloadCertificateServlet" method="post">
    <table border="0">
        <tr>
            <th align="right">CSR Id:</th>
            <td>
                <input type="text" name="csrId" size="20" maxlength="200"/>
            </td>
        </tr>
    </table>
    <input type="submit" value="Download Certificate" onClick="return validateForm();"/>
    <input type="reset" name="reset" value="Reset"/>
</form>
<a href="<%=request.getContextPath()%>">Cancel</a>
</body>
</html>
