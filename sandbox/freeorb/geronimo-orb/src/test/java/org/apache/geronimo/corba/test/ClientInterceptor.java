package org.apache.geronimo.corba.test;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;

public class ClientInterceptor extends LocalObject implements
		ClientRequestInterceptor {

	public void send_request(ClientRequestInfo ri) throws ForwardRequest {
		
		System.out.println("send_request: "+ri.operation());

	}

	public void send_poll(ClientRequestInfo ri) {
		System.out.println("send_poll: "+ri.operation());

	}

	public void receive_reply(ClientRequestInfo ri) {
		System.out.println("receive_reply: "+ri.operation());

	}

	public void receive_exception(ClientRequestInfo ri) throws ForwardRequest {
		System.out.println("receive_exception: "+ri.operation());

	}

	public void receive_other(ClientRequestInfo ri) throws ForwardRequest {
		System.out.println("receive_other: "+ri.operation());

	}

	public String name() {
		return this.getClass().getName();
	}

	public void destroy() {
		// TODO Auto-generated method stub

	}

}
