package org.apache.geronimo.itest.naming.client;

import java.sql.Connection;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.jms.Topic;

import org.apache.geronimo.naming.java.javaURLContextFactory;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.kernel.Kernel;
import org.apache.notgeronimo.itests.naming.common.Test;


public class App {
    public static void main(String[] args) throws Exception {

        String methodName = args[0];
        Method m = App.class.getMethod(methodName, new Class[] {});

        App app = new App();
        try {
            m.invoke(app, new Object[] {});
            System.out.println(methodName + " OK");
        } catch (Throwable e) {
            e.printStackTrace(System.out);
        }
    }

    public void testWebService() throws Exception {
        Test test = new Test();
        test.testWebServiceLookup();

    }

    public void testResourceRefLookup() throws Exception {
        Test test = new Test();
        test.testResourceRefLookup();
    }

}
