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

import org.omg.CORBA.portable.InputStream;
import org.omg.CORBA.portable.OutputStream;
import org.omg.IOP.TaggedComponent;
import org.omg.IOP.TaggedComponentHelper;

public class TaggedComponentSeqHelper {

	public static TaggedComponent[] read(InputStream in) {
		int len = in.read_long();
		TaggedComponent[] result = new TaggedComponent[len];
		for (int i = 0; i < len; i++) {
			result[i] = TaggedComponentHelper.read(in);
		}
		return result;
	}

	public static void write(OutputStream out, TaggedComponent[] tagged_components) {
		out.write_long(tagged_components.length);
		for (int i = 0; i < tagged_components.length; i++) {
			TaggedComponentHelper.write(out, tagged_components[i]);
		}
	}

}
