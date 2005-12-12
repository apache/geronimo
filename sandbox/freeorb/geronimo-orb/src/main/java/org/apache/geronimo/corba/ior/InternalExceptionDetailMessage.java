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

import org.apache.geronimo.corba.io.EncapsulationInputStream;
import org.apache.geronimo.corba.io.GIOPVersion;
import org.apache.geronimo.corba.io.InputStreamBase;
import org.omg.CORBA.ORB;
import org.omg.IOP.ExceptionDetailMessage;
import org.omg.IOP.ServiceContext;

public class InternalExceptionDetailMessage extends InternalServiceContext {

	public static int VALUE = ExceptionDetailMessage.value;
	
	private String msg;

	private InputStreamBase in;

	public InternalExceptionDetailMessage(InputStreamBase in,
			ServiceContext ctx) {
		super(ctx);
		this.in = in;
	}

	public static InternalExceptionDetailMessage get(InternalServiceContextList lst)
	{
		if (lst == null) return null;
		InternalServiceContext context = lst.getContextWithID(VALUE);
		return (InternalExceptionDetailMessage) context;
	}
	
	public String getMessage() {
		if (msg == null) {
			msg = decodeMessage();
			if (msg == null) {
				msg = "Unknown Exception";
			}
		}
		return msg;
	}

	private String decodeMessage() {
		EncapsulationInputStream ein = new EncapsulationInputStream(in.__orb(),
				in.getGIOPVersion(), in.__get_char_converter(), in
						.__get_wchar_converter(), ctx.context_data, 0,
				ctx.context_data.length);

		return ein.read_wstring();
	}

}
