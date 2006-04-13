<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<portlet:defineObjects/>

<c:set var="ksinfo" value="${requestScope['org.apache.geronimo.console.keystore.infobean']}"/>

<table cellpadding="1%">
<th>keystore-type</th>
<th>keystore-provider</th>
<th>keystore-location</th>
<th>keystore-size</th>
<tr>
<td align="center"><c:out value="${requestScope['org.apache.geronimo.console.keystore.type']}"/></td>
<td align="center"><c:out value="${requestScope['org.apache.geronimo.console.keystore.provider']}"/></td>
<td align="center"><c:out value="${requestScope['org.apache.geronimo.console.keystore.location']}"/></td>
<td align="center"><c:out value="${requestScope['org.apache.geronimo.console.keystore.size']}"/></td>
</tr>
</table>

<br/>

<table cellspacing="5">
<tr>
<td><b>Tools:</b></td>
<td>
<a href="<portlet:renderURL><portlet:param name='action' value='tools-import-trusted-certificate'/>
import trusted certificate
</portlet:renderURL>">import trusted certificate</a>
</td>
<td>
<a href="<portlet:renderURL><portlet:param name='action' value='tools-generate-key-pair'/>
generate key pair
</portlet:renderURL>">generate key pair</a>&nbsp;
</td>
<%--
<td>
<a href="<portlet:renderURL><portlet:param name='action' value='tools-change-keystore-password'/>
change keystore password
</portlet:renderURL>">change keystore password</a>&nbsp;
</td>
--%>
</tr>
</table>

<br/>

<%--
<c:set var="ksitems" value="${ksinfo.keystoreEntries}"/>
--%>

<c:set var="ksitems" value="${requestScope['org.apache.geronimo.console.keystore.list']}"/>

<table cellspacing="5">
<th>details</th>
<th>alias-name</th>
<th>alias-type</th>
<th>modified</th>
<c:forEach items="${ksitems}" var="ksi">
<tr>
<td align="center" valign="top"><a href="<portlet:renderURL>
<portlet:param name="action" value="view-keystore-entry-details"/>
<portlet:param name="alias" value="${ksi.alias}"/>
</portlet:renderURL>">view</a></td>
<td align="center" valign="top"><c:out value="${ksi.alias}"/></td>
<td align="center" valign="top"><c:out value="${ksi.type}"/></td>
<td align="center" valign="top"><c:out value="${ksi.created}"/></td>
</tr>
</c:forEach>
</table>