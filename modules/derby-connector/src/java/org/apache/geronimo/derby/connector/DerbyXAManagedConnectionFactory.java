package org.apache.geronimo.derby.connector;

import java.sql.SQLException;

import org.apache.derby.jdbc.EmbeddedXADataSource;
import org.tranql.connector.jdbc.AbstractXADataSourceMCF;
import org.tranql.connector.NoExceptionsAreFatalSorter;

/**
 */
public class DerbyXAManagedConnectionFactory extends AbstractXADataSourceMCF{

    public DerbyXAManagedConnectionFactory() {
        super(new EmbeddedXADataSource(), new NoExceptionsAreFatalSorter());
    }

    EmbeddedXADataSource getDerbyXADS() {
        return (EmbeddedXADataSource) xaDataSource;
    }

    public String getUserName() {
        return getDerbyXADS().getUser();
    }

    public void setUserName(String userName) {
        getDerbyXADS().setUser(userName);
    }

    public String getPassword() {
        return getDerbyXADS().getPassword();
    }

    public void setPassword(String password) {
        getDerbyXADS().setPassword(password);
    }

    public String getDatabaseName() {
        return getDerbyXADS().getDatabaseName();
    }

    public void setDatabaseName(String databaseName) {
        getDerbyXADS().setDatabaseName(databaseName);
    }

    public Integer getLoginTimeout() throws SQLException {
        return new Integer(getDerbyXADS().getLoginTimeout());
    }

    public void setLoginTimeout(Integer loginTimeout) throws SQLException {
        getDerbyXADS().setLoginTimeout(loginTimeout == null? 0: loginTimeout.intValue());
    }

    public String getCreateDatabase() {
        return getDerbyXADS().getCreateDatabase();
    }

    public void setCreateDatabase(String createDatabase) {
        getDerbyXADS().setCreateDatabase(createDatabase);
    }

    public String getShutdownDatabase() {
        return getDerbyXADS().getShutdownDatabase();
    }

    public void setShutdownDatabase(String shutdownDatabase) {
        getDerbyXADS().setShutdownDatabase(shutdownDatabase);
    }

}
