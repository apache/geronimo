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
import org.apache.geronimo.corba.ClientInvocation;
import org.apache.geronimo.corba.LocationForwardException;
import org.apache.geronimo.corba.ORB;
import org.apache.geronimo.corba.Policies;
import org.apache.geronimo.corba.giop.GIOPHelper;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.apache.geronimo.corba.ior.InternalServiceContext;
import org.apache.geronimo.corba.ior.InternalServiceContextList;
import org.apache.geronimo.corba.ior.Profile;
import org.omg.CORBA.SystemException;
import org.omg.PortableInterceptor.ClientRequestInfo;
import org.omg.PortableInterceptor.ClientRequestInterceptor;
import org.omg.PortableInterceptor.ForwardRequest;
import org.omg.PortableInterceptor.LOCATION_FORWARD;
import org.omg.PortableInterceptor.SUCCESSFUL;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import org.omg.PortableInterceptor.USER_EXCEPTION;

public class ClientRequestInfoImpl extends RequestInfoImpl implements
		ClientRequestInfo {
	static final Log log = LogFactory.getLog(ClientRequestInfoImpl.class);

	InternalIOR effective_ior;
	InternalIOR orig_ior;

	Profile pi;

	ClientRequestInterceptor[] clinterceptors;

	ClientRequestInfoImpl(ClientInvocation inv) {
		super(inv);

		this.effective_ior = inv.getEffectiveIOR();
		this.orig_ior = inv.getOrigIOR();
		this.pi = inv.getProfile();

		status = -1;
	}

	public org.omg.CORBA.Object target() {
		return getORB().createObject(orig_ior);
	}

	public org.omg.CORBA.Object effective_target() {
		return getORB().createObject(effective_ior);
	}

	public org.omg.IOP.TaggedProfile effective_profile() {
		return pi.asTaggedProfile(getORB());
	}

	public org.omg.CORBA.Any received_exception() {
		if (received_ex instanceof org.omg.CORBA.UnknownUserException) {
			return ((org.omg.CORBA.UnknownUserException) received_ex).except;
		}

		org.omg.CORBA.Any any = getORB().create_any();
		GIOPHelper.insertException(any, received_ex);
		return any;
	}

	public java.lang.String received_exception_id() {
		if (status != SYSTEM_EXCEPTION.value && status != USER_EXCEPTION.value) {

			throw new org.omg.CORBA.BAD_INV_ORDER();
		}

		if (received_ex_id == null)
			received_ex_id = GIOPHelper.getExceptionID(received_ex);

		return received_ex_id;
	}

	public org.omg.IOP.TaggedComponent get_effective_component(int id) {
		for (int i = 0; i < pi.getComponentCount(); i++) {
			if (pi.getTag(i) == id)
				return pi.getTaggedComponent(i);
		}

		throw new org.omg.CORBA.BAD_PARAM("no comonent id: " + id);
	}

	public org.omg.IOP.TaggedComponent[] get_effective_components(int id) {
		throw new org.omg.CORBA.NO_IMPLEMENT();
	}

	public org.omg.CORBA.Policy get_request_policy(int type) {
		org.omg.CORBA.Policy p = inv.getPolicies().get(type);

		if (p == null)
			throw new org.omg.CORBA.INV_POLICY();
		else
			return p;
	}

	public void add_request_service_context(
			org.omg.IOP.ServiceContext service_context, boolean replace) {
		
		inv.getRequestServiceContextList(false).add(new InternalServiceContext(service_context), replace);
	}

	//
	// private members...
	//

	void do_send_request(List interceptors) throws LocationForwardException {
		clinterceptors = new ClientRequestInterceptor[interceptors.size()];

		for (int i = 0; i < interceptors.size(); i++) {
			ClientRequestInterceptor cli = (ClientRequestInterceptor) interceptors
					.get(i);

			try {
				if (log.isDebugEnabled()) {
					log.debug("send_request " + cli.getClass().getName());
				}

				cli.send_request(this);
				clinterceptors[i] = cli;

			} catch (org.omg.CORBA.SystemException ex) {
				__setStatus(SYSTEM_EXCEPTION.value);
				__setReceivedEx(ex, null);
				do_receive_reply();
			} catch (ForwardRequest ex) {
				ClientDelegate od = (ClientDelegate) ((org.omg.CORBA.portable.ObjectImpl) ex.forward)
						._get_delegate();

				__setForwardIOR(od.getIOR());
				do_receive_reply();
			}
		}
	}

	void do_receive_reply() throws LocationForwardException {
		for (int i = 0; i < clinterceptors.length; i++) {
			try {
				ClientRequestInterceptor cli = clinterceptors[i];

				if (cli == null) {
					log.debug("ClientRequestInterceptor [" + i + "] is null");
					if (received_ex != null) {
						log.debug("received_ex", received_ex);
					}
					continue;
				}

				if (log.isDebugEnabled()) {
					log.debug("receive_reply " + cli.getClass().getName());
				}

				if (status == SUCCESSFUL.value) {
					cli.receive_reply(this);

				} else if (status == SYSTEM_EXCEPTION.value
						|| status == USER_EXCEPTION.value) {
					cli.receive_exception(this);

				} else {
					cli.receive_other(this);
				}

			} catch (SystemException ex) {
				__setStatus(SYSTEM_EXCEPTION.value);
				__setReceivedEx(ex, null);

			} catch (ForwardRequest ex) {

				ClientDelegate od = (ClientDelegate) ((org.omg.CORBA.portable.ObjectImpl) ex.forward)
						._get_delegate();

				__setForwardIOR(od.getIOR());
			}
		}

		if (status == SYSTEM_EXCEPTION.value) {
			throw (SystemException) received_ex;
		}

		if (status == LOCATION_FORWARD.value) {
			throw new LocationForwardException(forward_ior, false);
		}

	}
}
