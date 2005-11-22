<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

      <tr>
        <th style="min-width: 140px"><div align="right">User SELECT SQL:</div></th>
        <td><input name="option-userSelect" type="text"
                   size="60" value="${realm.options['userSelect']}"></td>
      </tr>
      <tr>
        <td></td>
        <td>A SQL statement to load user/password information.  It should return 2 columns, the first
          holding a username and the second holding a password.  The statement may use the PreparedStatement
          syntax of ? for a parameter, in which case the username will be set for every parameter.  A
          typical setting would be <tt>SELECT username, password FROM app_users WHERE username=?</tt></td>
      </tr>

      <tr>
        <th><div align="right">Group SELECT SQL:</div></th>
        <td><input name="option-groupSelect" type="text"
                   size="60" value="${realm.options['groupSelect']}"></td>
      </tr>
      <tr>
        <td></td>
        <td>A SQL statement to load group information for a user.  It should return 2 columns, the first
          holding a username and the second holding a group name.  The statement may use the PreparedStatement
          syntax of ? for a parameter, in which case the username will be set for every parameter.  A
          typical setting would be <tt>SELECT username, group_name FROM user_groups WHERE username=?</tt> or for
          a more normalized schema, <tt>SELECT u.username, g.name FROM app_users u, groups g, user_groups ug
          WHERE ug.user_id=users.id AND ug.group_id=g.id AND u.username=?</tt></td>
      </tr>

      <tr>
        <td></td>
        <td><i>A SQL security realm must either have a database pool or JDBC connectivity settings to
          connect to the database.  Please select EITHER the database pool, OR the rest of the JDBC
          settings.</i></td>
      </tr>


      <tr>
        <th><div align="right">Database Pool</div></th>
        <td>
          <select name="option-databasePoolObjectName">
            <option />
        <c:forEach var="pool" items="${pools}">
            <option value="${pool.objectName}"<c:if test="${realm.options['dataSourceName'] eq pool.name && realm.options['dataSourceApplication'] eq pool.applicationName}"> selected</c:if>>${pool.displayName}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>A database pool that the login module will use to connect to the database.  If this is specified, none of
          the rest of the settings after this are necessary.</td>
      </tr>

      <tr>
        <th><div align="right">JDBC Driver Class</div></th>
        <td><input name="option-jdbcDriver" type="text"
                   size="60" value="${realm.options['jdbcDriver']}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The fully-qualified JDBC driver class name.  This driver must be located in the JAR specified in the next
          field.</td>
      </tr>

      <tr>
        <th><div align="right">Driver JAR:</div></th>
        <td>
          <select name="jar">
            <option />
        <c:forEach var="jar" items="${jars}">
            <option <c:if test="${jar == realm.jar}">selected</c:if>>${jar}</option>
        </c:forEach>
          </select>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>The JAR holding the selected JDBC driver.  Should be installed under GERONIMO/repository/ to appear in this list.</td>
      </tr>

      <tr>
        <th><div align="right">JDBC URL</div></th>
        <td><input name="option-jdbcURL" type="text"
                   size="60" value="${realm.options['jdbcURL']}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The JDBC URL that specifies the details of the database to connect to.  This has a different form for
          each JDBC driver.</td>
      </tr>

      <tr>
        <th><div align="right">JDBC Username</div></th>
        <td><input name="option-jdbcUser" type="text"
                   size="20" value="${realm.options['jdbcUser']}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The username used to connect to the database</td>
      </tr>

      <tr>
        <th><div align="right">JDBC Password</div></th>
        <td><input name="option-jdbcPassword" type="password"
                   size="20" value="${realm.options['jdbcPassword']}"></td>
      </tr>
      <tr>
        <td></td>
        <td>The password used to connect to the database</td>
      </tr>

