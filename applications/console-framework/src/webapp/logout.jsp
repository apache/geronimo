<%@ page language="java" %>
<%
    request.getSession().invalidate();
	response.sendRedirect("./portal/info");
%>