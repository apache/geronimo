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

import java.applet.Applet;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.dii.NVListImpl;
import org.apache.geronimo.corba.io.DefaultConnectionManager;
import org.apache.geronimo.corba.io.EncapsulationInputStream;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.apache.geronimo.corba.ior.URLManager;
import org.apache.geronimo.corba.server.DefaultServerManager;
import org.apache.geronimo.corba.server.ServantObject;
import org.omg.CORBA.Context;
import org.omg.CORBA.ContextList;
import org.omg.CORBA.NO_IMPLEMENT;
import org.omg.CORBA.NVList;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.Request;
import org.omg.CORBA.TRANSIENT;
import org.omg.CORBA.WrongTransaction;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.CORBA.portable.OutputStream;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;

public class ORB extends AbstractORB {

	private static final Log log = LogFactory.getLog(ORB.class);

	private DefaultConnectionManager cm;

	private URLManager urlManager = new URLManager(this);

	private Executor executor;

	private ServerManager sm;

	protected void set_parameters(String[] args, Properties props) {
		sm = new DefaultServerManager(this);

		if (cm == null) {
			try {
				cm = new DefaultConnectionManager(this);
			} catch (IOException e) {
				e.printStackTrace();
				TRANSIENT t = new TRANSIENT();
				t.initCause(e);
				throw t;
			}
		}
	}

	protected void set_parameters(Applet app, Properties props) {
		// TODO Auto-generated method stub

	}

	public String[] list_initial_services() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object resolve_initial_references(String object_name)
			throws InvalidName {
		// TODO Auto-generated method stub
		return null;
	}

	public String object_to_string(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object string_to_object(String str) {
		return urlManager.createObject(str);
	}

	public NVList create_list(int count) {
		return new NVListImpl(this, count);
	}

	public ContextList create_context_list() {
		throw new NO_IMPLEMENT();
	}

	public Context get_default_context() {
		throw new NO_IMPLEMENT();
	}

	public OutputStream create_output_stream() {
		// TODO Auto-generated method stub
		return null;
	}

	public void send_multiple_requests_oneway(Request[] req) {
		// TODO Auto-generated method stub

	}

	public void send_multiple_requests_deferred(Request[] req) {
		// TODO Auto-generated method stub

	}

	public boolean poll_next_response() {
		// TODO Auto-generated method stub
		return false;
	}

	public Request get_next_response() throws WrongTransaction {
		// TODO Auto-generated method stub
		return null;
	}

	public synchronized ConnectionManager getConnectionManager() {
		return cm;
	}

	public InvocationProfileSelector createInvocationProfileSelector(
			ClientDelegate delegate) {
		return new InvocationProfileSelector(this, delegate);
	}

	public InvocationProfile[] getInvocationProfiles(InternalIOR ior) {
		return getConnectionManager().getInvocationProfiles(ior);
	}

	public InputStreamBase getEncapsulationInputStream(byte[] component_data) {
		return new EncapsulationInputStream(this, component_data);
	}

	public void __checkDestroy() {
		// TODO Auto-generated method stub

	}

	/** return a string that is the host name to use as "localhost". */
	public String getLocalHost() {
		// TODO Auto-generated method stub
		return "localhost";
	}

	public Policy[] getPolicies() {
		return new Policy[0];
	}

	public Executor getExecutor() {
		if (executor == null) {
			executor = new ThreadedExecutor();
		}
		return executor;
	}

	public ServerManager __getServerManager() {
		return sm;
	}

}
