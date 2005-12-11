<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>
<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>generateKeyPair";
var <portlet:namespace/>requiredFields = new Array("alias","validity","cn","ou","o","l","st","c");
function <portlet:namespace/>validateForm(){
    return textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields) && checkIntegral(<portlet:namespace/>formName,"validity");
}
</script>

<form method="post" name="<portlet:namespace/>generateKeyPair"
action="<portlet:actionURL><portlet:param name="action" value="generate-key-pair"/></portlet:actionURL>">
<table>
<th colspan="2">Generate Key Pair</th>
<tr>
<td>Alias:</td>
<td><input type="text" name="alias"/></td>
</tr>
<tr>
<td>Key Algorithm:</td>
<td><input type="radio" name="keyalg" value="RSA" checked>RSA</input></td>
</tr>
<tr>
<td>Key Size:</td>
<td>
<input type="radio" name="keysize" value="512">512</input>
<input type="radio" name="keysize" value="1024" checked>1024</input>
<input type="radio" name="keysize" value="2048">2048</input>
</td>
</tr>
<tr>
<td>Signature Algorithm:</td>
<td>
<input type="radio" name="sigalg" value="MD2withRSA">MD2withRSA</input>
<input type="radio" name="sigalg" value="MD5withRSA" checked>MD5withRSA</input>
<input type="radio" name="sigalg" value="SHA1withRSA">SHA1withRSA</input>
</td>
</tr>
<tr>
<td>Validity:</td><td><input type="text" name="validity"/></td>
</tr>
<tr>
<td>Common Name (CN):</td><td><input type="text" name="cn"/></td>
</tr>
<tr>
<td>Organizational Unit (OU):</td><td><input type="text" name="ou"/></td>
</tr>
<tr>
<td>Organizational Name (O):</td><td><input type="text" name="o"/></td>
</tr>
<tr>
<td>Locality (L):</td><td><input type="text" name="l"/></td>
</tr>
<tr>
<td>State (ST):</td><td><input type="text" name="st"/></td>
</tr>
<tr>
<td>Country (C):</td><td><input type="text" name="c"/></td>
</tr>
<%--
<tr>
<td>Email (E):</td><td><input type="text" name="e"/></td>
</tr>
--%>
</table>
<br/>
<input type="submit" name="submit" value="Submit" onclick="return <portlet:namespace/>validateForm()"/>
<input type="reset"/>
<input type="submit" name="submit" value="Cancel"/>
</form>
