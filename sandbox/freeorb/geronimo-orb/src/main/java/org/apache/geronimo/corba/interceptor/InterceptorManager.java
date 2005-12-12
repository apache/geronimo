/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.corba.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.ClientInvocation;
import org.apache.geronimo.corba.LocationForwardException;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.Policies;
import org.apache.geronimo.corba.giop.GIOPHelper;
import org.apache.geronimo.corba.initials.InitialServicesManager;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.apache.geronimo.corba.ior.InternalServiceContextList;
import org.apache.geronimo.corba.server.POA;
import org.apache.geronimo.corba.server.ServerInvocation;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.OBJECT_NOT_EXIST;
import org.omg.IOP.CodecFactory;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.IORInterceptor;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.PolicyFactory;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;
import org.omg.PortableServer.Servant;

public class InterceptorManager extends LocalObject implements ORBInitInfo {
	static Log log = LogFactory.getLog(InterceptorManager.class);

	ORB orb;

	List clientRequestInterceptors;

	List serverRequestInterceptors;

	List iorInterceptors;

	CodecFactory codec_factory;

	boolean initialized;

	String[] args;

	private final InitialServicesManager im;

	private boolean isDestroyed;

	public InterceptorManager(ORB orb, InitialServicesManager im) {
		this.orb = orb;
		this.im = im;
		clientRequestInterceptors = new ArrayList();
		serverRequestInterceptors = new ArrayList();
		iorInterceptors = new ArrayList();
	}

	public org.omg.CORBA.ORB getORB() {
		return orb;
	}

	private void checkDestroy()
	{
		if (isDestroyed) {
			throw new OBJECT_NOT_EXIST("ORBInitInfo has been destroyed");
		}
	}
	
	//
	// ORBInitInfo API
	//

	public void add_client_request_interceptor(ClientRequestInterceptor cri) throws DuplicateName {
		checkDestroy();
		
		if (cri == null) {
			throw new org.omg.CORBA.BAD_PARAM("null interceptor");
		}
		
		if (cri.name() == null) {
			throw new org.omg.CORBA.BAD_PARAM("null name for interceptor");
		}
		
		for (int i = 0; i < clientRequestInterceptors.size(); i++) {
			ClientRequestInterceptor ri = (ClientRequestInterceptor)clientRequestInterceptors.get(i);
			if (ri.name().equals(cri.name()))  {
				throw new DuplicateName();
			}
		}

		clientRequestInterceptors.add(cri);
	}

	public void add_server_request_interceptor(ServerRequestInterceptor sri) {
		checkDestroy();
		if (sri == null) {
			throw new org.omg.CORBA.BAD_PARAM("null interceptor");
		}

		serverRequestInterceptors.add(sri);
	}

	public java.lang.String[] arguments() {
		checkDestroy();
		return args;
	}

	public java.lang.String orb_id() {
		checkDestroy();
		// TODO: Grab this value from somewhere
		return "Apache Geronimo CORBA ORB 1.x";
	}

	public void register_initial_reference(java.lang.String id,
			org.omg.CORBA.Object obj) throws InvalidName {
		
		checkDestroy();
		try {
			im.register_initial_reference(id, obj);
		} catch (org.omg.CORBA.ORBPackage.InvalidName ex) {
			throw new InvalidName(ex.getMessage());
		}
	}

	public org.omg.CORBA.Object resolve_initial_references(java.lang.String id)
			throws InvalidName {
		checkDestroy();
		try {
			return orb.resolve_initial_references(id);
		} catch (org.omg.CORBA.ORBPackage.InvalidName ex) {
			throw new InvalidName(ex.getMessage());
		}
	}

	public void add_ior_interceptor(IORInterceptor interceptor)
			throws DuplicateName {
		checkDestroy();
		iorInterceptors.add(interceptor);
	}

	public int allocate_slot_id() {
		checkDestroy();
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	public void register_policy_factory(int type, PolicyFactory policy_factory) {
		checkDestroy();
		orb.getPolicyFactoryManager().register_policy_factory(type,
				policy_factory);
	}

	public org.omg.IOP.CodecFactory codec_factory() {
		checkDestroy();
		if (codec_factory == null) {
			codec_factory = new CodecFactoryImpl(orb);
		}

		return codec_factory;
	}

	//
	// Internal API
	//

	public ClientRequestInfoImpl clientSendRequest(ClientInvocation inv)
			throws LocationForwardException {
		ClientRequestInfoImpl ri = new ClientRequestInfoImpl(inv);

		ri.do_send_request(clientRequestInterceptors);
		return ri;
	}

	public void clientReceiveReply(ClientRequestInfoImpl ri) {
		ri.__setStatus(SUCCESSFUL.value);
		try {
			ri.do_receive_reply();
		} catch (LocationForwardException e) {
			throw new INTERNAL("cannot handle forwards in reveive_reply");
		}
	}

	public void clientReceiveLocationForward(ClientRequestInfoImpl ri,
			InternalIOR forwardIOR) throws LocationForwardException {
		ri.__setForwardIOR(forwardIOR);
		ri.do_receive_reply();
	}

	public void clientReceiveException(ClientRequestInfoImpl ri, boolean system,
			Exception ex, String exId) throws LocationForwardException {
		ri.__setStatus(system ? SYSTEM_EXCEPTION.value : USER_EXCEPTION.value);

		ri.__setReceivedEx(ex, exId);
		ri.do_receive_reply();
	}

	public ServerRequestInfoImpl serverCreateReceiveRequest(
			ServerInvocation inv, byte[] poa_id, byte[] object_id) {
		if (log.isDebugEnabled()) {
			log.debug("creating ServerRequestInfo for " + inv.getOperation());
		}

		ServerRequestInfoImpl si = new ServerRequestInfoImpl(inv, poa_id, object_id);

		return si;
	}

	public void serverSetupServant(ServerRequestInfoImpl si, Servant servant,
			POA poa) {
		si.setServantAndPOA(servant, poa);
	}

	public void serverReceiveRequestServiceContexts(ServerRequestInfoImpl si)
			throws LocationForwardException {
		if (log.isDebugEnabled()) {
			log.debug("[server] requestServiceContext " + si.operation());
		}

		si.do_receive_request_service_contexts(serverRequestInterceptors);
	}

	public void serverReceiveRequest(ServerRequestInfoImpl si)
			throws LocationForwardException {
		if (log.isDebugEnabled()) {
			log.debug("[server] request " + si.operation());
		}

		si.do_receive_request();
	}

	public void serverSendReply(ServerRequestInfoImpl si) {
		si.__setStatus(SUCCESSFUL.value);
		si.do_send_reply();
	}

	public void serverSendException(ServerRequestInfoImpl si, boolean system,
			Exception ex) throws LocationForwardException {
		si.__setStatus(system ? SYSTEM_EXCEPTION.value : USER_EXCEPTION.value);

		si.__setReceivedEx(ex, null);
		si.do_send_exception();
	}

	public void serverSendLocationForward(ServerRequestInfoImpl si,
			InternalIOR ior) throws LocationForwardException {
		si.__setForwardIOR(ior);
		si.do_send_other();
	}

	public boolean haveClientInterceptors() {
		return initialized && clientRequestInterceptors.size() > 0;
	}

	public boolean haveServerInterceptors() {
		return initialized && serverRequestInterceptors.size() > 0;
	}

	public boolean haveIORInterceptors() {
		return initialized && iorInterceptors.size() > 0;
	}

	public void init(String[] args, java.util.Properties props) {

		this.args = args;

		ORBInitializer[] inits = initializers(props);
		for (int i = 0; i < inits.length; i++) {
			inits[i].pre_init(this);
		}

		for (int i = 0; i < inits.length; i++) {
			inits[i].post_init(this);
		}

		initialized = true;
	}

	private ORBInitializer[] initializers(java.util.Map props) {
		ArrayList iters = new ArrayList();
		java.util.Iterator it = props.keySet().iterator();
		String prefix = "org.omg.PortableInterceptor.ORBInitializerClass.";

		while (it.hasNext()) {
			String key = (String) it.next();

			if (key.startsWith(prefix)) {
				String name = key.substring(prefix.length());

				try {
					Class cls = GIOPHelper.classForName(name);
					java.lang.Object obj = cls.newInstance();
					iters.add((ORBInitializer) obj);
					// orb.log.info ("loaded ORBInitializer "+name);

				} catch (ClassNotFoundException ex) {
					log.warn("can't find initializer class " + name);

				} catch (IllegalAccessException ex) {
					log.warn("can't instantiate initializer class " + name);

				} catch (InstantiationException ex) {
					log.warn("can't instantiate initializer class " + name);

				}
			}
		}

		ORBInitializer[] result = new ORBInitializer[iters.size()];
		iters.toArray(result);
		return result;
	}

	public void establishComponents(String name, java.util.Map componentMap,
			java.util.List components, Policies policies) {
		if (!haveIORInterceptors())
			return;

		IORInfoImpl ii = new IORInfoImpl(name, componentMap, components,
				policies);

		for (int i = 0; i < iorInterceptors.size(); i++) {
			IORInterceptor interceptor = (IORInterceptor) iorInterceptors
					.get(i);

			try {
				interceptor.establish_components(ii);
			} catch (org.omg.CORBA.SystemException ex) {
				// ignore //
			}
		}
	}

}
