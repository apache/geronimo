<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<portlet:defineObjects/>

<c:set var="ksinfo" value="${requestScope['org.apache.geronimo.console.keystore.infobean']}"/>

<table width="75%" cellpadding="1%">
  <tr class="DarkBackground">
    <th>Keystore Type</th>
    <th>Provider</th>
    <th>Location</th>
    <th>Size</th>
  </tr>
  <tr>
    <td align="center"><c:out value="${requestScope['org.apache.geronimo.console.keystore.type']}"/></td>
    <td align="center"><c:out value="${requestScope['org.apache.geronimo.console.keystore.provider']}"/></td>
    <td align="center"><c:out value="${requestScope['org.apache.geronimo.console.keystore.location']}"/></td>
    <td align="center"><c:out value="${requestScope['org.apache.geronimo.console.keystore.size']}"/></td>
  </tr>
</table>

<br />

<table cellspacing="5">
  <tr>
    <td><b>Tools:</b></td>
  </tr>
  <tr>
    <td>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="<portlet:renderURL><portlet:param name='action' value='tools-import-trusted-certificate'/>
    import trusted certificate
    </portlet:renderURL>">import trusted certificate</a>
    </td>
  </tr>
  <tr>
    <td>
    &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    <a href="<portlet:renderURL><portlet:param name='action' value='tools-generate-key-pair'/>
    generate key pair
    </portlet:renderURL>">generate key pair</a>
    </td>
  </tr>
<%--
    <td>
    <a href="<portlet:renderURL><portlet:param name='action' value='tools-change-keystore-password'/>
    change keystore password
    </portlet:renderURL>">change keystore password</a>&nbsp;
    </td>
--%>
</table>

<br/>

<%--
<c:set var="ksitems" value="${ksinfo.keystoreEntries}"/>
--%>

<c:set var="ksitems" value="${requestScope['org.apache.geronimo.console.keystore.list']}"/>

<c:if test="${!empty ksitems}">  <%-- Do not show table if ksitems is empty (esp. per above comment removing setting of ksitmes --%>
  <table width="75%">
    <tr class="DarkBackground">
      <th>Details</th>
      <th>Alias Name</th>
      <th>Alias Type</th>
      <th>Last Modified</th>
    </tr>

      <c:set var="backgroundClass" value='MediumBackground'/>
      <c:forEach items="${ksitems}" var="ksi">
      <c:choose>
          <c:when test="${backgroundClass == 'MediumBackground'}" >
              <c:set var="backgroundClass" value='LightBackground'/>
          </c:when>
          <c:otherwise>
              <c:set var="backgroundClass" value='MediumBackground'/>
          </c:otherwise>
      </c:choose>
      <tr>
        <td class="${backgroundClass}" align="center" valign="top"><a href="<portlet:renderURL>
        <portlet:param name="action" value="view-keystore-entry-details"/>
        <portlet:param name="alias" value="${ksi.alias}"/>
        </portlet:renderURL>">view</a></td>
        <td class="${backgroundClass}" align="center" valign="top"><c:out value="${ksi.alias}"/></td>
        <td class="${backgroundClass}" align="center" valign="top"><c:out value="${ksi.type}"/></td>
        <td class="${backgroundClass}" align="center" valign="top"><c:out value="${ksi.created}"/></td>
      </tr>
      </c:forEach>
  </table>
</c:if>
