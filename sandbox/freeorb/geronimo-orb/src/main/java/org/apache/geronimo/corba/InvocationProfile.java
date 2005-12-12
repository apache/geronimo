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

import org.apache.geronimo.corba.giop.GIOPOutputStream;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;
import org.apache.geronimo.corba.ior.Profile;
import org.omg.CORBA.portable.InputStream;

public interface InvocationProfile {

	GIOPOutputStream startRequest(ClientInvocation invocation);

	InputStreamBase invoke(ClientInvocation invocation, ClientDelegate delegate,
			OutputStreamBase out);

	void releaseReply(InputStreamBase in);

	Profile getProfile();

}
