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
package org.apache.geronimo.corba;

import java.util.List;

import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.PortableInterceptor.ForwardRequest;

import org.apache.geronimo.corba.giop.GIOPOutputStream;
import org.apache.geronimo.corba.giop.RequestID;
import org.apache.geronimo.corba.interceptor.ClientRequestInfoImpl;
import org.apache.geronimo.corba.interceptor.InterceptorManager;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.apache.geronimo.corba.ior.InternalServiceContextList;
import org.apache.geronimo.corba.ior.Profile;


public class ClientInvocation implements Invocation {

    private final InvocationProfileSelector manager;
    private final String operation;
    private final boolean responseExpected;

    /** */
    private final InvocationProfile profile;

	private RequestID requestID;
	private InternalServiceContextList iscl;
	private InternalServiceContextList respose_scl;
	private ReplyStatusType_1_2 reply_status;
	private SystemException systemException;
	private ApplicationException userException;
	private final Policies policies;
	private InterceptorManager im;
	private ClientRequestInfoImpl ir;
	private ORB orb;

    public ClientInvocation(ORB orb,
    						  InvocationProfileSelector manager,
                            String operation,
                            boolean responseExpected,
                            InvocationProfile profile,
                            Policies policies
    )
    {
    		this.orb = orb;
    		this.im = orb.getInterceptorManager();
        this.manager = manager;
        this.operation = operation;
        this.responseExpected = responseExpected;
        this.profile = profile;
		this.policies = policies;
        this.iscl = new InternalServiceContextList();
    }

    ClientDelegate getDelegate() {
        return manager.getDelegate();
    }


    public OutputStreamBase startRequest()
            throws LocationForwardException
    {
    		if (im != null) {
    			ir = im.clientSendRequest(this);
    		}
    	 
		return profile.startRequest(this);
    }

	public String getOperation() {
		return operation;
	}

	public short getSyncScope() {
		// TODO: in the future we may provide a way to set this
		if (responseExpected) {
			return Invocation.SYNC_WITH_SERVER;
		} else {
			return Invocation.SYNC_NONE;
		}
	}

	public InputStream invoke(ClientDelegate delegate, OutputStreamBase out) {
		InputStreamBase in = profile.invoke(this, delegate, out);
		if (in != null) {
			in.setClientInvocation(this);
		}
		return in;
	}

	public boolean isResponseExpected() {
		return responseExpected;
	}

	public void setRequestID(RequestID requestID) {
		this.requestID = requestID;		
	}

	public InternalServiceContextList getRequestServiceContextList(boolean create) {
		if (create && iscl == null) {
			iscl = new InternalServiceContextList();
		}
		return iscl;
	}

	public int getRequestID() {
		return requestID.value();
	}

	public RequestID getRequestIDObject() {
		return requestID;
	}

	public void releaseReply(InputStreamBase in) {
		try {
			//
			// This is effectively what flushes the message
			// to the underlying transport
			//
			profile.releaseReply(in);
		} catch (SystemException ex) {
			
			setSystemException(ex);
			try {
				checkException();
			} catch (ApplicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (LocationForwardException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		
		/** this is where we handle the "normal" case for client interceptors */
		if (im != null && ir != null && userException == null && systemException == null) {
			im.clientReceiveReply(ir);
		}
	}

	public void setResponseServiceContextList (InternalServiceContextList scl) {
		this.respose_scl = scl;
	}

	public InternalServiceContextList getResponseServiceContextList(boolean create) {
		if ( create && this.respose_scl  == null) {
			respose_scl = new InternalServiceContextList();
		}
		
		return respose_scl;
	}
	
	public void setReplyStatus(ReplyStatusType_1_2 reply_status) {
		this.reply_status = reply_status;
	}

	public ReplyStatusType_1_2 getReplyStatus() {
		return reply_status;
	}

	public void setSystemException(SystemException sex) {
		this.systemException = sex;
		this.reply_status = ReplyStatusType_1_2.SYSTEM_EXCEPTION;
	}

	public void setUserException(ApplicationException aex) {
		this.userException = aex;
		this.reply_status = ReplyStatusType_1_2.USER_EXCEPTION;
	}

	public void checkException() throws ApplicationException, LocationForwardException {
		
		if (systemException != null) {
			
			if (im != null) {
				im.clientReceiveException(ir, true, systemException, systemException.getClass().getName());
			}
			
			throw systemException;
		}
		
		if (userException != null) {

			if (im != null) {
				im.clientReceiveException(ir, false, userException, userException.getClass().getName());
			}
			
			throw userException;
		}
		
	}

	
	public InternalIOR getEffectiveIOR() {
		return getDelegate().getIOR();
	}

	public InternalIOR getOrigIOR() {
		return getDelegate().getOrigIOR();
	}

	public Policies getPolicies() {
		return policies;
	}

	public Profile getProfile() {
		return profile.getProfile();
	}

	public ORB getORB() {
		return orb;
	}
}
