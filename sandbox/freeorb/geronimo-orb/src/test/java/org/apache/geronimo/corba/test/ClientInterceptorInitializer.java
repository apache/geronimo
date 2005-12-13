package org.apache.geronimo.corba.test;

import org.omg.CORBA.LocalObject;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;

public class ClientInterceptorInitializer extends LocalObject implements
		ORBInitializer {

	public void pre_init(ORBInitInfo info) {
		
		ClientRequestInterceptor ci = new ClientInterceptor();

		try {
			info.add_client_request_interceptor(ci);
		} catch (DuplicateName e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void post_init(ORBInitInfo info) {
		// TODO Auto-generated method stub

	}

}
