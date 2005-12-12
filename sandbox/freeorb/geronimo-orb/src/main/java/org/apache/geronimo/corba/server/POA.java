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
package org.apache.geronimo.corba.server;

import org.omg.CORBA.Object;
import org.omg.CORBA.Policy;
import org.omg.PortableServer.AdapterActivator;
import org.omg.PortableServer.IdAssignmentPolicy;
import org.omg.PortableServer.IdAssignmentPolicyValue;
import org.omg.PortableServer.IdUniquenessPolicy;
import org.omg.PortableServer.IdUniquenessPolicyValue;
import org.omg.PortableServer.ImplicitActivationPolicy;
import org.omg.PortableServer.ImplicitActivationPolicyValue;
import org.omg.PortableServer.LifespanPolicy;
import org.omg.PortableServer.LifespanPolicyValue;
import org.omg.PortableServer.POAManager;
import org.omg.PortableServer.RequestProcessingPolicy;
import org.omg.PortableServer.RequestProcessingPolicyValue;
import org.omg.PortableServer.Servant;
import org.omg.PortableServer.ServantManager;
import org.omg.PortableServer.ServantRetentionPolicy;
import org.omg.PortableServer.ServantRetentionPolicyValue;
import org.omg.PortableServer.ThreadPolicy;
import org.omg.PortableServer.ThreadPolicyValue;
import org.omg.PortableServer.POAPackage.AdapterAlreadyExists;
import org.omg.PortableServer.POAPackage.AdapterNonExistent;
import org.omg.PortableServer.POAPackage.InvalidPolicy;
import org.omg.PortableServer.POAPackage.NoServant;
import org.omg.PortableServer.POAPackage.ObjectAlreadyActive;
import org.omg.PortableServer.POAPackage.ObjectNotActive;
import org.omg.PortableServer.POAPackage.ServantAlreadyActive;
import org.omg.PortableServer.POAPackage.ServantNotActive;
import org.omg.PortableServer.POAPackage.WrongAdapter;
import org.omg.PortableServer.POAPackage.WrongPolicy;

public class POA extends org.omg.CORBA.LocalObject implements
		org.omg.PortableServer.POA {

	public org.omg.PortableServer.POA create_POA(String arg0, POAManager arg1,
			Policy[] arg2) throws AdapterAlreadyExists, InvalidPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public org.omg.PortableServer.POA find_POA(String arg0, boolean arg1)
			throws AdapterNonExistent {
		// TODO Auto-generated method stub
		return null;
	}

	public void destroy(boolean arg0, boolean arg1) {
		// TODO Auto-generated method stub

	}

	public ThreadPolicy create_thread_policy(ThreadPolicyValue arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public LifespanPolicy create_lifespan_policy(LifespanPolicyValue arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public IdUniquenessPolicy create_id_uniqueness_policy(
			IdUniquenessPolicyValue arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public IdAssignmentPolicy create_id_assignment_policy(
			IdAssignmentPolicyValue arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ImplicitActivationPolicy create_implicit_activation_policy(
			ImplicitActivationPolicyValue arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public ServantRetentionPolicy create_servant_retention_policy(
			ServantRetentionPolicyValue arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public RequestProcessingPolicy create_request_processing_policy(
			RequestProcessingPolicyValue arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public String the_name() {
		// TODO Auto-generated method stub
		return null;
	}

	public org.omg.PortableServer.POA the_parent() {
		// TODO Auto-generated method stub
		return null;
	}

	public org.omg.PortableServer.POA[] the_children() {
		// TODO Auto-generated method stub
		return null;
	}

	public POAManager the_POAManager() {
		// TODO Auto-generated method stub
		return null;
	}

	public AdapterActivator the_activator() {
		// TODO Auto-generated method stub
		return null;
	}

	public void the_activator(AdapterActivator arg0) {
		// TODO Auto-generated method stub

	}

	public ServantManager get_servant_manager() throws WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public void set_servant_manager(ServantManager arg0) throws WrongPolicy {
		// TODO Auto-generated method stub

	}

	public Servant get_servant() throws NoServant, WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public void set_servant(Servant arg0) throws WrongPolicy {
		// TODO Auto-generated method stub

	}

	public byte[] activate_object(Servant arg0) throws ServantAlreadyActive,
			WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public void activate_object_with_id(byte[] arg0, Servant arg1)
			throws ServantAlreadyActive, ObjectAlreadyActive, WrongPolicy {
		// TODO Auto-generated method stub

	}

	public void deactivate_object(byte[] arg0) throws ObjectNotActive,
			WrongPolicy {
		// TODO Auto-generated method stub

	}

	public Object create_reference(String arg0) throws WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public Object create_reference_with_id(byte[] arg0, String arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] servant_to_id(Servant arg0) throws ServantNotActive,
			WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public Object servant_to_reference(Servant arg0) throws ServantNotActive,
			WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public Servant reference_to_servant(Object arg0) throws ObjectNotActive,
			WrongPolicy, WrongAdapter {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] reference_to_id(Object arg0) throws WrongAdapter, WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public Servant id_to_servant(byte[] arg0) throws ObjectNotActive,
			WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public Object id_to_reference(byte[] arg0) throws ObjectNotActive,
			WrongPolicy {
		// TODO Auto-generated method stub
		return null;
	}

	public byte[] id() {
		// TODO Auto-generated method stub
		return null;
	}

	public void __preinvoke(String operation, byte[] oid,
			Servant original_servant, Object object) {
		// TODO Auto-generated method stub

	}

	public void __postinvoke() {
		// TODO Auto-generated method stub

	}

	public boolean __incrementRequestCount() {
		// TODO Auto-generated method stub
		return false;
	}

	public void __decrementRequestCount() {
		// TODO Auto-generated method stub

	}

}
