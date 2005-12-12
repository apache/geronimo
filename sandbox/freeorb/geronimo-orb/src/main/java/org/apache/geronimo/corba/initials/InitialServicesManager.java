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
package org.apache.geronimo.corba.initials;

import java.util.Properties;

import org.apache.geronimo.corba.ORB;
import org.omg.CORBA.Object;
import org.omg.CORBA.ORBPackage.InvalidName;

public class InitialServicesManager {

	private final ORB orb;

	public InitialServicesManager(ORB orb) {
		this.orb = orb;
	}

	public String[] list_initial_services() {
		// TODO Auto-generated method stub
		return null;
	}

	public Object resolve_initial_references(String object_name) throws InvalidName {
		// TODO Auto-generated method stub
		return null;
	}

	public void register_initial_reference(String id, Object obj) throws InvalidName {
		// TODO Auto-generated method stub
		
	}

	public void init(String[] args, Properties props) {
		// TODO Auto-generated method stub
		
	}

}
