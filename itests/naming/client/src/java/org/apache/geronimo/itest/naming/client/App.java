package org.apache.geronimo.itest.naming.client;

import java.sql.Connection;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.geronimo.naming.java.javaURLContextFactory;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.kernel.Kernel;


public class App 
{
    public static void main( String[] args ) throws Exception
    {
        App app = new App();
        app.testResourceRefLookup();
        System.out.println( "Hello World!" );
    }

    public void testResourceRefLookup() throws Exception {
        InitialContext initialContext = new InitialContext();
        Object o = initialContext.lookup("java:comp/env/jdbc/DefaultDatasource");
        DataSource ds = (DataSource) o;
        Connection conn = ds.getConnection();
        conn.close();
    }
}
