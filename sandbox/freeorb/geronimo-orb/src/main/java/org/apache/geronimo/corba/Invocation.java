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

import org.apache.geronimo.corba.ior.InternalServiceContextList;

public interface Invocation {

	static public final byte SYNC_NONE = 0;
	static public final byte SYNC_WITH_TRANSPORT = 1;
	static public final byte SYNC_WITH_SERVER = 2;
	static public final byte SYNC_WITH_TARGET = 3;

	public abstract int getRequestID();

	public abstract String getOperation();

	public abstract short getSyncScope();

	public abstract boolean isResponseExpected();

	public abstract InternalServiceContextList getResponseServiceContextList(boolean create);

	public abstract InternalServiceContextList getRequestServiceContextList(boolean create);

	public abstract ORB getORB();

	public abstract Policies getPolicies();

}
