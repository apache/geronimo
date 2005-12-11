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

import org.omg.CORBA.SystemException;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.GIOP.ReplyStatusType_1_2;
import org.omg.PortableInterceptor.ForwardRequest;

import org.apache.geronimo.corba.giop.GIOPMessageTransport;
import org.apache.geronimo.corba.giop.GIOPOutputStream;
import org.apache.geronimo.corba.giop.RequestID;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;
import org.apache.geronimo.corba.ior.InternalServiceContextList;


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

    public ClientInvocation(InvocationProfileSelector manager,
                            String operation,
                            boolean responseExpected,
                            InvocationProfile profile
    )
    {
        this.manager = manager;
        this.operation = operation;
        this.responseExpected = responseExpected;
        this.profile = profile;
        this.iscl = new InternalServiceContextList();
    }

    ClientDelegate getDelegate() {
        return manager.getDelegate();
    }


    public GIOPOutputStream startRequest()
            throws ForwardRequest
    {
        return profile.startRequest(this);
    }

	public String getOperation() {
		return operation;
	}

	public byte getResponseFlags() {
		if (responseExpected) {
			return GIOPMessageTransport.SYNC_WITH_TARGET;
		} else {
			return GIOPMessageTransport.SYNC_NONE;
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
		// TODO Auto-generated method stub
		return true;
	}

	public void setRequestID(RequestID requestID) {
		this.requestID = requestID;		
	}

	public InternalServiceContextList getRequestServiceContextList() {
		return iscl;
	}

	public RequestID getRequestID() {
		return requestID;
	}

	public void releaseReply(InputStreamBase in) {
		profile.releaseReply(in);
	}

	public void setResposeServiceContextList (InternalServiceContextList scl) {
		this.respose_scl = scl;
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

	public void checkException() throws ApplicationException {
		if (this.systemException != null) {
			throw systemException;
		}
		
		if (userException != null) {
			throw userException;
		}
		
	}

	public InternalServiceContextList getReplyServiceContextList() {
		return respose_scl;
	}

}
