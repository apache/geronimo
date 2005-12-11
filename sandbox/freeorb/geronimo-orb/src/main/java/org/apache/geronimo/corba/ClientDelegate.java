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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.corba.dii.ExceptionListImpl;
import org.apache.geronimo.corba.dii.NVListImpl;
import org.apache.geronimo.corba.dii.NamedValueImpl;
import org.apache.geronimo.corba.dii.RequestImpl;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;
import org.apache.geronimo.corba.ior.InternalIOR;
import org.apache.geronimo.corba.server.POA;
import org.apache.geronimo.corba.server.ServantObject;
import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.CORBA.portable.ApplicationException;
import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.CORBA.portable.RemarshalException;
import org.omg.CORBA_2_3.portable.Delegate;
import org.omg.IOP.IOR;

public class ClientDelegate extends Delegate {

	private static final Log log = LogFactory.getLog(ClientDelegate.class);

	private final ORB orb;

	private InternalIOR effective_ior;

	private InvocationProfileSelector profileManager;

	private final Policy[] policies;

	private POA poa;

	private byte[] oid;

	private boolean certainlyRemote;

	private ServantObject servantObject;

	private InternalIOR orig_ior;

	private ThreadLocal retryState;

	private Policies effective_policies;
	
	/* @deprecated */
	public ClientDelegate(InternalIOR ior) {
		this(ior, null);
	}

	public ClientDelegate(InternalIOR ior, Policy[] policies) {
		this.effective_ior = ior;
		this.policies = policies;
		this.orb = (ORB) ior.orb;
	}

	public ClientDelegate(ORB orb, IOR ior) {
		this(new InternalIOR(orb, ior), orb.getPolicies());
	}

	ClientDelegate(ORB orb, IOR ior, Policy[] policies) {
		this(new InternalIOR(orb, ior), policies);
	}

	//
	//
	//

	public ClientDelegate(ORB orb, POA poa, byte[] oid, String repository_id,
			Policy[] policies) {
		this.orb = orb;
		this.policies = policies;
		this.poa = poa;
		this.oid = oid;
	}

	public boolean is_local(org.omg.CORBA.Object self) {
		
        if (log.isDebugEnabled ()) {
            log.debug ("is_local "+self);
        }

        if(certainlyRemote) {
            if (log.isDebugEnabled ()) {
                log.debug ("is_local ==> false [certainlyRemote == true]");
            }

            return false;
        }

        if(servantObject != null && !servantObject.isDeactivated()) {
            if (log.isDebugEnabled ()) {
                log.debug ("is_local ==> true [servantObject != null]");
            }

            return true;
        }

        int count = 0;
        while(true)
            {
                try {
                    servantObject = orb.__getServerManager().getServantObject(getIOR(), policies);
                    break;

                } catch (LocationForwardException lex) {

                        effective_ior = lex.getIor ();
                        if (lex.isPermanent ()) {
                            orig_ior = effective_ior;
                        }
                        
                        profileManager.reset();
                        certainlyRemote = false;
                            
                }

                if (count++ == 100)
                    throw new org.omg.CORBA.COMM_FAILURE ("More than 100 successive forwards?");
            }
        
        if(servantObject != null) {
            setRetry (true);

            if (log.isDebugEnabled ()) {
                log.debug ("is_local ==> true [found local servant]");
            }

            return true;

        } else {

            if (log.isDebugEnabled ()) {
                log.debug ("is_local ==> false [no local servant]");
            }

            certainlyRemote = true;
        }

        return false;


	}

	public OutputStream request(org.omg.CORBA.Object self, String operation,
			boolean responseExpected) {

		while (true) {

			InvocationProfileSelector manager = getProfileSelector();

			try {

				// process client interceptor (pre-marshal) and write
				// RequestHeader to output stream.

				OutputStreamBase result = manager.setupRequest(operation,
						responseExpected, effective_policies);

				if (result.getClientInvocation() == null) {
					throw new INTERNAL();
				}

				return result;

			} catch (LocationForwardException ex) {

				if (ex.isPermanent()) {
					setOrigIOR(ex.getIor());
				}
				
				setIOR(ex.getIor());
				
				continue;

			}

		}
	}

	private void setOrigIOR(InternalIOR ior2) {
		this.orig_ior = ior2;
		this.effective_ior = ior2;
		this.profileManager = null;
	}

	private void setIOR(InternalIOR ior) {
		this.effective_ior = ior;
		this.profileManager = null;
	}

	private InvocationProfileSelector getProfileSelector() {
		if (this.profileManager == null) {
			this.profileManager = orb.createInvocationProfileSelector(this);
		}
		return profileManager;
	}

	/**
	 * Method invocation sequence, step 2.
	 * 
	 * this method is responsible for finishing the output stream and
	 * relinquishing the underlying channel to let other threads do invocations
	 * on the same GIOPMessageTransport.
	 * 
	 */
	public InputStream invoke(org.omg.CORBA.Object self, OutputStream output)
			throws ApplicationException, RemarshalException {
		OutputStreamBase out = (OutputStreamBase) output;

		ClientInvocation inv = out.getClientInvocation();

		if (inv == null) {
			throw new BAD_INV_ORDER("OutputStream from wrong context");
		}

		InputStream in = inv.invoke(this, out);

		try {
			inv.checkException();
		} catch (LocationForwardException e) {
			
			if (e.isPermanent()) {
				setOrigIOR(e.getIor());
			} else {
				setIOR(e.getIor());
			}

			throw new RemarshalException();
		}

		return in;
	}

	public void releaseReply(org.omg.CORBA.Object self, InputStream input) {
		InputStreamBase in = (InputStreamBase) input;

		if (in == null)
			return;

		ClientInvocation inv = in.getClientInvocation();

		if (inv == null)
			return;

		inv.releaseReply(in);
	}

	public Object get_interface_def(Object self) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object duplicate(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	public void release(Object obj) {
		// TODO Auto-generated method stub

	}

	public boolean is_a(Object self, String rid) {

		if (log.isDebugEnabled()) {
			log.debug("is_a " + rid);
		}

		if (rid.equals("IDL:omg.org/CORBA/Object:1.0")) {
			if (log.isDebugEnabled()) {
				log.debug("is_a org.omg.CORBA.Object => true");
			}

			return true;
		}

		org.omg.CORBA.portable.ObjectImpl obj = (org.omg.CORBA.portable.ObjectImpl) self;
		String[] ids = obj._ids();
		for (int i = 0; i < ids.length; i++) {
			if (log.isDebugEnabled()) {
				log.debug("is_a ids[" + i + "] =" + ids[i]);
			}

			if (ids[i].equals(rid))
				return true;
		}

		if (log.isDebugEnabled()) {
			log.debug("is_a ior.type_id =" + getIOR().getType());
		}

		if (rid.equals(getIOR().getType()))
			return true;

		while (true) {
			if (!is_local(self)) {
				OutputStream out = null;
				InputStream in = null;

				if (log.isDebugEnabled()) {
					log.debug("invoking remote _is_a");
				}

				try {
					out = (OutputStream) request(self, "_is_a", true);
					out.write_string(rid);
					in = (InputStream) invoke(self, out);
					boolean result = in.read_boolean();

					if (log.isDebugEnabled()) {
						log.debug("invoking remote _is_a => " + result);
					}

					return result;

				} catch (org.omg.CORBA.portable.ApplicationException ex) {
					orb.fatal("unexpected exception from invoking is_a", ex);

				} catch (org.omg.CORBA.portable.RemarshalException ex) {
					continue;

				} finally {
					releaseReply(self, in);
				}
			} else {
				if (log.isDebugEnabled()) {
					log.debug("invoking local _is_a");
				}

				ServantObject so = (ServantObject) servant_preinvoke(self,
						"_is_a", null);

				if (so == null)
					continue;

				try {
					org.omg.PortableServer.Servant servant = so.original_servant;

					boolean result = servant._is_a(rid);

					if (log.isDebugEnabled()) {
						log.debug("invoking local _is_a => " + result);
					}

					return result;
				} finally {
					servant_postinvoke(self, so);
				}
			}
		}
	}

	public boolean non_existent(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean is_equivalent(Object self, Object other) {
		if (other == null)
			return false;

		if (self == other)
			return true;

		if (self instanceof LocalObject || other instanceof LocalObject)
			return false;

		org.omg.CORBA.portable.ObjectImpl otherObject = (org.omg.CORBA.portable.ObjectImpl) other;

		ClientDelegate delegate = (ClientDelegate) otherObject._get_delegate();
		if (delegate == this)
			return true;

		synchronized (this) {
			return getIOR().equals(delegate.getIOR());
		}
	}

	public int hash(Object obj, int max) {
		// TODO Auto-generated method stub
		return 0;
	}

	public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self,
			org.omg.CORBA.Context ctx, String operation,
			org.omg.CORBA.NVList arg_list, org.omg.CORBA.NamedValue result) {
		return new RequestImpl(orb, (org.omg.CORBA.portable.ObjectImpl) self,
				operation, ctx, (NVListImpl) arg_list, (NamedValueImpl) result);
	}

	public org.omg.CORBA.Request create_request(org.omg.CORBA.Object self,
			org.omg.CORBA.Context ctx, String operation,
			org.omg.CORBA.NVList arg_list, org.omg.CORBA.NamedValue result,
			org.omg.CORBA.ExceptionList excepts,
			org.omg.CORBA.ContextList contexts) {
		return new RequestImpl(orb, (org.omg.CORBA.portable.ObjectImpl) self,
				operation, ctx, (NVListImpl) arg_list, (NamedValueImpl) result,
				(ExceptionListImpl) excepts, contexts);
	}

	public org.omg.CORBA.Request request(org.omg.CORBA.Object self,
			String operation) {
		return new RequestImpl(orb, (org.omg.CORBA.portable.ObjectImpl) self,
				operation);
	}

	public InternalIOR getInternalIOR() {
		return effective_ior;
	}

	public ORB getORB() {
		return orb;
	}

	public InternalIOR getIOR() {
		return effective_ior;
	}

    private synchronized void setRetry (boolean value)
    {
        if (retryState == null && value == true)
            return;

        if (retryState == null)
            retryState = new ThreadLocal();

        if (value == true)
            retryState.set (null);
        else
            retryState.set (this);
    }

    private synchronized boolean getRetry ()
    {
        if (retryState == null)
            return true;

        return retryState.get () == null;
    }

    private synchronized boolean getAndSetRetry (boolean value)
    {
        boolean result = getRetry ();
        if (result != value)
            setRetry (value);
        return result;
    }

	public InternalIOR getOrigIOR() {
		return orig_ior;
	}

	
}
