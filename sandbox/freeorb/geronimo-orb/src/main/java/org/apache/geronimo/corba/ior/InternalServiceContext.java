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

import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;
import org.omg.CORBA.portable.OutputStream;
import org.omg.IOP.ExceptionDetailMessage;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.ServiceContextHelper;

public class InternalServiceContext extends TaggedValue {

	protected ServiceContext ctx;

	public InternalServiceContext(ServiceContext ctx) {
		this.ctx = ctx;
	}

	public int tag() {
		return ctx.context_id;
	}


	public byte[] get_cached_byte_encoding() {
		return ctx.context_data;
	}
	
	protected void write_content(OutputStream eo) {
		ServiceContextHelper.write(eo, ctx);
	}

	public static InternalServiceContext read(InputStreamBase in) {
		ServiceContext ctx = ServiceContextHelper.read(in);
		
		switch(ctx.context_id) {

		case ExceptionDetailMessage.value:
			return new InternalExceptionDetailMessage(in, ctx);
				
		default:
			return new InternalServiceContext(ctx);		
		}
		
	}

}
