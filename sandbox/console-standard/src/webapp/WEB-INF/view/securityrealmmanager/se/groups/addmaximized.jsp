<%@ page import="org.apache.geronimo.console.util.SERealmGroupHelper" %>
<%@ page import="java.util.Collection" %>
<%@ page import="java.util.Iterator" %>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>addgroup";
var <portlet:namespace/>requiredFields = new Array("group");
function <portlet:namespace/>validateForm(){
    return (textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields));
}
function <portlet:namespace/>move(formName, from, to){
    var objFrom = eval("document." + formName + "." + from);
    var objTo = eval("document." + formName + "." + to);
    for(var pos = objFrom.length-1; pos >=0; pos--){ 
        if(objFrom.options[pos].selected){
            objTo.options.length += 1;
            objTo.options[objTo.options.length-1].value = objFrom.options[pos].value;
            objTo.options[objTo.options.length-1].text = objFrom.options[pos].text;
            <portlet:namespace/>removeAt(objFrom,pos);
        }
    }    
}
function <portlet:namespace/>removeAt(obj,pos){
    for(var i = pos; i < obj.options.length-1; i++){
        obj.options[i].value = obj.options[i+1].value;
        obj.options[i].text = obj.options[i+1].text;
    }
    obj.options.length -= 1;    
}

function <portlet:namespace/>selectAll(formName, objName){
    var obj = eval("document." + formName + "." + objName);
    for(var i = 0; i < obj.options.length; i++){
        obj.options[i].selected = true;
    }
    return true;
}

</script>
<c:set var="add" value="${group == null}"/>
<form name="<portlet:namespace/>addgroup" action="<portlet:actionURL portletMode="view"/>" >
    <table cellspacing="5">
    <tr>
      <td colspan="2" align="left" class="formHeader">
       <c:choose>
	   <c:when test="${add}"> 
       		<b>ADD GROUP</b>
      		<c:set var="GroupName" value=""/>
      		<c:set var="Submit" value="Add"/>
       </c:when>
       <c:otherwise>
			<b>UPDATE GROUP</b>
      		<c:set var="GroupName" value="${group}"/>
      		<c:set var="Submit" value="Update"/>
       </c:otherwise>
       </c:choose>
        </td>
    </tr>
    <tr>
        <td width="200" class="formLabel">Group Name</td>
        <td class="formElement">
       <c:choose>
	   <c:when test="${add}"> 
	    <input type="hidden" name="action" value="add">
	    <input type="text" name="group" value="">
       </c:when>
       <c:otherwise>
	    <input type="hidden" name="action" value="update">
        <input type="hidden" name="group" value="${GroupName}">
        ${GroupName}
       </c:otherwise>
       </c:choose>       
        </td>
    </tr>   
    <tr>
        <td class="formLabel">Users</td>
        <td class="formElement">
        <c:choose>
        <c:when test="${users != null && fn:length(users) > 0}">
            <% 
            String[] users = ((String[])request.getAttribute("users"));
            %>
            <table>
            <tr>
                <td>
                <select name="availusers" size="4" multiple>
                <%
                for(int i = 0;i<users.length;i++){
                    String user = users[i];
                    Object grp = request.getAttribute("group");
                    String group = ((grp!=null)?grp.toString():"");
                %>    
                <%if(!SERealmGroupHelper.isGroupMember(group, user)){%>
                <option><%=user%></option>
                <%}%>
               <%}%> 
                </select>
                </td>
                <td align="center" valign="middle">
                <input type = "button" 
                    value="&nbsp;&nbsp;&nbsp;&nbsp;Add &gt;&gt;&nbsp;&nbsp;&nbsp;&nbsp;"
                    onclick="<portlet:namespace/>move('<portlet:namespace/>addgroup','availusers','users');"
                    />
                    
                    <br/>
                <input type = "button" value="&lt;&lt; Remove"
                    onclick="<portlet:namespace/>move('<portlet:namespace/>addgroup','users','availusers');"
                    />                
                </td> 
                <td>
                <select name="users" size="4" multiple>
                <%
                for(int i = 0;i<users.length;i++){
                    String user = users[i];
                    Object grp = request.getAttribute("group");
                    String group = ((grp!=null)?grp.toString():"");
                %>    
                <%if(SERealmGroupHelper.isGroupMember(group, user)){%>
                <option><%=user%></option>
                <%}%>
               <%}%> 
                </select>
                </td>
            </tr>   
            </table>
            </td>
             
        </c:when>
        <c:otherwise>
        No available users.
        </c:otherwise>
        </c:choose>       
        </td>
    </tr>
    <tr>   
       <td>&nbsp;</td><td align="left" class="formElement">
       <input type="submit" value="${Submit}" 
            onclick="return <portlet:namespace/>validateForm() && <portlet:namespace/>selectAll('<portlet:namespace/>addgroup', 'users');">
       <input type="submit" name="cancel"  value="Cancel">
       </td>
      </tr>
    </table>
</form>