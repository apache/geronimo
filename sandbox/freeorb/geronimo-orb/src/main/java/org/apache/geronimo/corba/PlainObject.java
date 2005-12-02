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

import org.omg.CORBA_2_3.portable.ObjectImpl;


public class PlainObject extends ObjectImpl {

	public PlainObject() {
		
	}
	
    public PlainObject(ClientDelegate delegate) {
    		_set_delegate(delegate);
	}

	ClientDelegate __get_delegate() {
        return (ClientDelegate) _get_delegate();
    }

    public String[] _ids() {
        return new String[]{__get_delegate().getIOR().getType()};
    }

}
