<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects/>

<p>This page edits a new or existing database pool.</p>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="test" />
            </portlet:actionURL>">Test Connection</a></p>
<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="save" />
            </portlet:actionURL>">Skip Test & Save</a></p>


<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="list" />
            </portlet:actionURL>">Cancel</a></p>
