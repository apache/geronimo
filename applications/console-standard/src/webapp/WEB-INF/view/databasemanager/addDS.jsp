<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>
<script language="javascript">
<!--
	function doCheck(){
		var poolMax = <portlet:namespace/>.poolMaxSize.value;
        var poolInit = <portlet:namespace/>.poolInitSize.value;
        
        if(isNaN(parseFloat(poolMax))){
            alert("Please enter a numeric value for Pool Max Capacity.");
            return false;
        }
        if(isNaN(parseFloat(poolInit))){ 
            alert("Please enter a numeric value for Pool Initial Capacity.");
            return false;
        }
        return true;
	}
//-->
</script>

<form name="<portlet:namespace/>" action="<portlet:actionURL/>" onSubmit="return doCheck();">
<input type="hidden" name="mode" value="addDS">
<table width="100%%"  border="0">
  <tr>
    <td width="16%"> <div align="right">Name: </div></td>
    <td width="84%"><input name="dsName" type="text" size="50"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> <p>Name of this JDBC data source.</td>
  </tr>
  <tr>
    <td> <div align="right">Dependency: </div></td>
    <td>
	<select name="dependency">
	<c:forEach var="item" items="${dependencies}">
	  <option value="${item}">${item}</option>
	</c:forEach>	
    </select>
	<!--
	<select name="dependency">
		<option value="Cloudscape">Cloudscape</option>
		<option value="DB2">DB2</option>
		<option value="Informix">Informix</option>
		<option value="mm-mysql/jars/mysqlconnector-3.0.14.jar">MS SQL Server</option>
		<option value="MySQL">MySQL</option>
		<option value="Oracle">Oracle</option>
		<option value="PointBase" selected>PointBase</option>
		<option value="PostgreSQL">PostgreSQL</option>
		<option value="Progress">Progress</option>\
		<option value="Sybase">Sybase</option>
    </select>
	-->
	</td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>Dependency jar file of this JDBC data source. To add new file go to <a href="/console/portal/repo">Repository</a>.</td>
  </tr>
  <tr>
    <td> <div align="right">Driver Classname: </div></td>
    <td><input name="driverClass" type="text" size="50"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> JDBC driver class (use full package name).</td>
  </tr>
  <tr>
    <td> <div align="right">DB URL: </div></td>
    <td><input name="jdbcUrl" type="text" size="50"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> URL of the database to connect to (check your JDBC driver documentation for proper format).</td>
  </tr>
  <tr>
    <td> <div align="right">DB User Name: </div></td>
    <td><input name="dbUser" type="text" size="50"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> Database account user name. </td>
  </tr>
  <tr>
    <td><div align="right"> DB Password: </div></td>
    <td><input name="dbPassword" type="password"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> Database account password. </td>
  </tr>
  <!--
  <tr>
    <td><div align="right"> DB Properties: </div></td>
    <td><textarea name="dbProperties" cols="40"></textarea></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> List of properties passed to the JDBC driver. List each property=value pair on a separate line. </td>
  </tr>
  -->
  <tr>
    <td> <div align="right">Pool Max Capacity: </div></td>
    <td><input name="poolMaxSize" type="text" size="20" value="0"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> Maximum database connection pool size. </td>
  </tr>
  <tr>
    <td> <div align="right">Pool Initial Capacity: </div></td>
    <td><input name="poolInitSize" type="text" size="20" value="0"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td> Initial database connection pool size. </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td><input name="submit" type="submit" value="Create"></td>
  </tr>
</table>
</form>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'>List Datasources</a>