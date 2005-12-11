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

import org.apache.geronimo.corba.ior.InternalIOR;

/** LocationForwardException is used internally in the Geronimo ORB to handle redirects. */

public class LocationForwardException extends Exception {

	private final InternalIOR ior;
	private final boolean isPermanent;
	
	public LocationForwardException(InternalIOR ior, boolean isPermanent) {
		this.ior = ior;
		this.isPermanent = isPermanent;
	}
	
	public InternalIOR getIor() {
		return ior;
	}

	public boolean isPermanent() {
		return isPermanent;
	}

}
