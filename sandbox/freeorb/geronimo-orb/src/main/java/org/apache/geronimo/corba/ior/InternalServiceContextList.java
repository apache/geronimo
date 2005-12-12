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
package org.apache.geronimo.corba.ior;

import java.util.ArrayList;
import java.util.List;

import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;
import org.omg.IOP.ServiceContext;


public class InternalServiceContextList {

	List scl = new ArrayList();
	
    public void write(OutputStreamBase out) {
        out.write_long(0);
    }

	public void read(InputStreamBase in) {
		int count = in.read_long();
		for (int i = 0; i < count; i++) {
			InternalServiceContext sc = InternalServiceContext.read(in);
			scl.add(sc);
		}
	}

	public InternalServiceContext getContextWithID(int value) {
		for (int i = 0; i < scl.size(); i++) {
			InternalServiceContext ctx = (InternalServiceContext) scl.get(i);
			if (ctx.tag() == value) {
				return ctx;
			}
		}
		
		return null;
	}

	public void add(InternalServiceContext service_context, boolean replace) {
		
		if (replace) {
			for (int i = 0; i < scl.size(); i++) {
				InternalServiceContext sc = (InternalServiceContext) scl.get(i);
				if (sc.tag() == service_context.tag()) {
					scl.set(i, service_context);
				}
			}
		}

		scl.add(service_context);
	}


}
