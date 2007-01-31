package org.apache.geronimo.test.ws;

import javax.xml.rpc.Service;

public interface HelloWorldService extends Service {

	 public java.lang.String getHelloWorldAddress();

	 public HelloWorld getHelloWorld() throws javax.xml.rpc.ServiceException;

	 public HelloWorld getHelloWorld(java.net.URL portAddress) throws javax.xml.rpc.ServiceException;
}
