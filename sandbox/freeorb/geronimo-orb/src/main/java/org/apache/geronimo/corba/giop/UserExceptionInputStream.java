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
package org.apache.geronimo.corba.giop;

import org.apache.geronimo.corba.io.FilterInputStream;
import org.apache.geronimo.corba.io.InputStreamBase;


/** This is an inputstream specifically for reading the result of a user exception
 * 
 * The function is to return a predefined rep-id as the first result of a call
 * to read_string.
 *  */
public class UserExceptionInputStream extends FilterInputStream {

	private final String id;
	private boolean did_read_string = false;
	
	/** the input stream */
	UserExceptionInputStream(InputStreamBase base, String id) {
		super(base);
		this.id = id;
	}
	
	public String read_string()
	{
		if (did_read_string) {
			super.read_string();
		}
		
		did_read_string = true;
		return id;
	}
}
