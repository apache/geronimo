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

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.ClientDelegate;
import org.apache.geronimo.corba.LocationForwardException;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.Policies;
import org.apache.geronimo.corba.giop.GIOPHelper;
import org.apache.geronimo.corba.ior.InternalServiceContext;
import org.apache.geronimo.corba.ior.InternalServiceContextList;
import org.apache.geronimo.corba.server.POA;
import org.apache.geronimo.corba.server.ServerInvocation;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.ServerRequestInfo;
import org.omg.PortableInterceptor.ServerRequestInterceptor;
import org.omg.PortableInterceptor.TRANSPORT_RETRY;
import org.omg.PortableInterceptor.USER_EXCEPTION;
import org.omg.PortableServer.Servant;

public class ServerRequestInfoImpl extends RequestInfoImpl implements
		ServerRequestInfo {
	static final Log log = LogFactory.getLog(ServerRequestInfoImpl.class);

	byte[] object_id;

	byte[] poa_id;

	POA poa;

	Servant servant;

	ServerRequestInterceptor[] svinterceptors;

	ServerRequestInfoImpl(ServerInvocation inv,
			// extra ...
			byte[] poa_id, byte[] object_id) {
		super(inv);

		this.poa_id = poa_id;
		this.object_id = object_id;

		status = -1;
	}

	//
	// Public API methods
	//

	public org.omg.CORBA.Any sending_exception() {
		if (status != SYSTEM_EXCEPTION.value && status != USER_EXCEPTION.value) {
			throw new org.omg.CORBA.BAD_INV_ORDER();
		}

		if (received_ex == null)
			throw new org.omg.CORBA.NO_RESOURCES("exception unavailable");

		if (received_ex instanceof org.omg.CORBA.UnknownUserException) {
			return ((org.omg.CORBA.UnknownUserException) received_ex).except;
		}

		org.omg.CORBA.Any any = getORB().create_any();
		GIOPHelper.insertException(any, received_ex);
		return any;
	}

	public byte[] object_id() {
		byte[] id = new byte[object_id.length];
		System.arraycopy(object_id, 0, id, 0, object_id.length);
		return id;
	}

	public byte[] adapter_id() {
		byte[] id = new byte[poa_id.length];
		System.arraycopy(poa_id, 0, id, 0, poa_id.length);
		return id;
	}

	public java.lang.String target_most_derived_interface() {
		if (servant == null)
			throw new org.omg.CORBA.BAD_INV_ORDER("no servant");

		if (poa == null)
			getORB().fatal("no POA");

		return servant._all_interfaces(poa, object_id)[0];
	}

	public org.omg.CORBA.Policy get_server_policy(int type) {
		org.omg.CORBA.Policy p = inv.getPolicies().get(type);

		if (p == null)
			throw new org.omg.CORBA.INV_POLICY();
		else
			return p;
	}

	public void set_slot(int id, org.omg.CORBA.Any data)
			throws org.omg.PortableInterceptor.InvalidSlot {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	public boolean target_is_a(java.lang.String id) {
		if (servant == null)
			throw new org.omg.CORBA.BAD_INV_ORDER();

		return servant._is_a(id);
	}

	public void add_reply_service_context(
			org.omg.IOP.ServiceContext service_context, boolean replace) {
		
		inv.getResponseServiceContextList(false).add(new InternalServiceContext(service_context), replace);
	}

	void do_receive_request_service_contexts(List interceptors)
			throws LocationForwardException {
		svinterceptors = new ServerRequestInterceptor[interceptors.size()];

		try {
			for (int i = 0; i < interceptors.size(); i++) {
				ServerRequestInterceptor svi = (ServerRequestInterceptor) interceptors
						.get(i);

				svi.receive_request_service_contexts(this);
				svinterceptors[i] = svi;
			}
		} catch (ForwardRequest ex) {
			ClientDelegate od = (ClientDelegate) ((org.omg.CORBA.portable.ObjectImpl) ex.forward)
					._get_delegate();

			throw new LocationForwardException(od.getIOR(), false);
		}
	}

	void do_receive_request() throws LocationForwardException {
		status = -1;

		try {
			for (int i = 0; i < svinterceptors.length; i++) {
				ServerRequestInterceptor svi = svinterceptors[i];

				if (svi != null) {
					if (log.isDebugEnabled()) {
						log
								.debug("receive_request "
										+ svi.getClass().getName());
					}

					svi.receive_request(this);
				}
			}
		} catch (ForwardRequest ex) {

			if (log.isDebugEnabled()) {
				log.debug("sending_forward");
			}

			ClientDelegate od = (ClientDelegate) ((org.omg.CORBA.portable.ObjectImpl) ex.forward)
					._get_delegate();

			throw new LocationForwardException(od.getIOR(), false);
		}
	}

	void do_send_reply() {
		if (status != SUCCESSFUL.value)
			getORB().fatal("status is not successful");

		servant = null;

		for (int i = 0; i < svinterceptors.length; i++) {
			ServerRequestInterceptor svi = svinterceptors[i];
			if (svi != null) {

				if (log.isDebugEnabled()) {
					log.debug("send_reply " + svi.getClass().getName());
				}

				svi.send_reply(this);
			}
		}
	}

	void do_send_exception() throws LocationForwardException {
		if (!(status == SYSTEM_EXCEPTION.value || status == USER_EXCEPTION.value)) {
			getORB().fatal("not exception state");
		}

		servant = null;

		try {
			for (int i = 0; i < svinterceptors.length; i++) {
				ServerRequestInterceptor svi = svinterceptors[i];
				if (svi != null) {
					svi.send_exception(this);
				}
			}
		} catch (ForwardRequest ex) {
			ClientDelegate od = (ClientDelegate) ((org.omg.CORBA.portable.ObjectImpl) ex.forward)
					._get_delegate();

			throw new LocationForwardException(od.getIOR(), false);
		}

	}

	void do_send_other() throws LocationForwardException {
		if (!(status == LOCATION_FORWARD.value || status == TRANSPORT_RETRY.value)) {
			getORB().fatal("not \"other\" state");
		}

		servant = null;

		try {
			for (int i = 0; i < svinterceptors.length; i++) {
				ServerRequestInterceptor svi = svinterceptors[i];
				if (svi != null) {
					svi.send_other(this);
				}
			}
		} catch (ForwardRequest ex) {
			ClientDelegate od = (ClientDelegate) ((org.omg.CORBA.portable.ObjectImpl) ex.forward)
					._get_delegate();

			throw new LocationForwardException(od.getIOR(), false);
		}

	}

	void setServantAndPOA(Servant servant, POA poa) {
		this.servant = servant;
		this.poa = poa;
	}
}
