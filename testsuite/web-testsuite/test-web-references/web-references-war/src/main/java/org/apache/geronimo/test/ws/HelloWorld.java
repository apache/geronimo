package org.apache.geronimo.test.ws;

import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.xml.rpc.Service;

public interface HelloWorld   extends Remote
{

    public abstract String getHelloWorld(String s)
        throws RemoteException;
}