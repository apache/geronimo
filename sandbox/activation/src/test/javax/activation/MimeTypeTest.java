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
public class MimeTypeTest extends TestCase {

	private final static String DEFAULT_PRIMARY_TYPE = "text";
	private final static String DEFAULT_SUB_TYPE = "plain";

	private String defaultRawdata;
	private String primary;
	private String sub;
	private String withParamsRawdata;

	public MimeTypeTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
		defaultRawdata = "text/plain;";
		primary = "primary";
		sub = "sub";
		withParamsRawdata = primary + "/" + sub + "; name1 =value1; name2 = value2;";
	}

	public void test1DefaultConstructor() {
		MimeType mimeType = new MimeType();
		assertEquals(DEFAULT_PRIMARY_TYPE, mimeType.getPrimaryType());
		assertEquals(DEFAULT_SUB_TYPE, mimeType.getSubType());
		assertEquals(0, mimeType.getParameters().size());
		assertTrue(mimeType.match(new MimeType()));
	}

	public void test2OthersConstructor() throws MimeTypeParseException {
		MimeType mimeType = new MimeType(defaultRawdata);
		MimeType defaultMimeType = new MimeType();
		assertEquals(defaultMimeType.getBaseType(), mimeType.getBaseType());
		assertTrue(mimeType.match(defaultMimeType));

		mimeType = new MimeType(withParamsRawdata);
		assertEquals(primary, mimeType.getPrimaryType());
		assertEquals(sub, mimeType.getSubType());
		assertEquals(2, mimeType.getParameters().size());
		assertEquals("value1", mimeType.getParameter("name1"));

		MimeType mimeType2 = new MimeType(primary, sub);
		assertEquals(primary, mimeType2.getPrimaryType());
		assertEquals(sub, mimeType2.getSubType());
		assertTrue(mimeType2.match(mimeType));
	}

	public void test3MatchMethods() throws MimeTypeParseException {
		assertTrue(new MimeType().match(new MimeType()));
		assertTrue(new MimeType().match(defaultRawdata));
	}

	public void test4ExternalMethods() {
		// TODO
	}
}

