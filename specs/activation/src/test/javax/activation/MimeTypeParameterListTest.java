/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package javax.activation;

import junit.framework.TestCase;


/**
 *
 * @version $Rev$ $Date$
 */
public class MimeTypeParameterListTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
	}

	public void testEmptyParameterList() {
		MimeTypeParameterList parameterList = new MimeTypeParameterList();
		assertEquals(0, parameterList.size());
	}

	public void testSimpleParameterList() throws MimeTypeParseException {
		MimeTypeParameterList parameterList = new MimeTypeParameterList(";name=value");
        assertEquals(1, parameterList.size());
        assertEquals("name", parameterList.getNames().nextElement());
		assertEquals("value", parameterList.get("name"));
	}

	public void testWhiteSpacesParameterList() throws MimeTypeParseException {
		MimeTypeParameterList parameterList = new MimeTypeParameterList("; name= value ;  ");
        assertEquals(1, parameterList.size());
        assertEquals("name", parameterList.getNames().nextElement());
		assertEquals("value", parameterList.get("name"));
	}

	public void testLongParameterList() throws MimeTypeParseException {
		MimeTypeParameterList parameterList = new MimeTypeParameterList(";name1=value1; name2 = value2; name3=value3;name4  = value4");
		assertEquals(4, parameterList.size());
		assertEquals("value1", parameterList.get("name1"));
		assertEquals("value2", parameterList.get("name2"));
		assertEquals("value3", parameterList.get("name3"));
		assertEquals("value4", parameterList.get("name4"));
	}

	public void testNoValueParameterList() {
		try {
			new MimeTypeParameterList("; name=");
            fail("Expected MimeTypeParseException");
		} catch (MimeTypeParseException mtpEx) {
            // ok
		}
	}

	public void testNoNameParameterList() {
		try {
			new MimeTypeParameterList("; = value");
            fail("Expected MimeTypeParseException");
		} catch (MimeTypeParseException mtpEx) {
            // ok
		}
	}
}

