/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http:www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http:www.apache.org/>.
 *
 * ====================================================================
 */
package javax.activation;

import junit.framework.TestCase;


/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/29 04:28:53 $
 */
public class MimeTypeParameterListTest extends TestCase {

	private String simpleParameterListStr;
	private String withWhiteSpacesParameterListStr;
	private String longParameterListStr;
	private String noNameParameterListStr;
	private String noValueParameterListStr;

	public MimeTypeParameterListTest(String name) {
		super(name);
	}

	protected void setUp() throws Exception {
		super.setUp();
		simpleParameterListStr = ";name=value";
		withWhiteSpacesParameterListStr = "; name= value ;  ";
		longParameterListStr = "; name1 =value1;;   ; name2= value2;name3= value3;name4  =value4;";
		noNameParameterListStr = "; = value";
		noValueParameterListStr = "; name=";
	}

	public void testEmptyParameterList() {
		MimeTypeParameterList parameterList = new MimeTypeParameterList();
		assertEquals(0, parameterList.size());
	}

	public void testSimpleParameterList() throws MimeTypeParseException {
		MimeTypeParameterList parameterList = new MimeTypeParameterList(simpleParameterListStr);
		assertEquals(simpleParameterListStr, parameterList.toString());
	}

	public void testWhiteSpacesParameterList() throws MimeTypeParseException {
		MimeTypeParameterList parameterList = new MimeTypeParameterList(withWhiteSpacesParameterListStr);
		assertEquals(simpleParameterListStr, parameterList.toString());
	}

	public void testLongParameterList() throws MimeTypeParseException {
		MimeTypeParameterList parameterList = new MimeTypeParameterList(longParameterListStr);
		assertEquals(4, parameterList.size());
		assertEquals("value1", parameterList.get("name1"));
		assertEquals("value2", parameterList.get("name2"));
		assertEquals("value3", parameterList.get("name3"));
		assertEquals("value4", parameterList.get("name4"));
	}

	public void testNoNameParameterList() {
		boolean catched = false;
		try {
			MimeTypeParameterList parameterList = new MimeTypeParameterList(noNameParameterListStr);
		} catch (MimeTypeParseException mtpEx) {
			catched = true;
		}
		assertTrue(catched);
	}

	public void testNoValueParameterList() {
		boolean catched = false;
		try {
			MimeTypeParameterList parameterList = new MimeTypeParameterList(noValueParameterListStr);
		} catch (MimeTypeParseException mtpEx) {
			catched = true;
		}
		assertTrue(catched);
	}
}

