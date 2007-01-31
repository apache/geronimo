package org.apache.geronimo.test.ws;

import java.rmi.RemoteException;
import javax.xml.rpc.ServiceException;
import javax.xml.rpc.server.ServiceLifecycle;


public class HelloWorldWS
    implements HelloWorld, ServiceLifecycle
{

    public HelloWorldWS()
    {
    }

    public String getHelloWorld(String name)
        throws RemoteException
    {
        return "Hello world, " + name;
    }

    public void init(Object obj)
        throws ServiceException
    {
    }

    public void destroy()
    {
    }
}