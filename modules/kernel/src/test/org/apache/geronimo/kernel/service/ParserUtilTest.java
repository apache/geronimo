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
 *        Apache Software Foundation (http://www.apache.org/)."
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
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.apache.geronimo.kernel.service;

import junit.framework.TestCase;

/**
 * Unit test for {@link ParserUtil} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/27 17:58:50 $
 */

public class ParserUtilTest extends TestCase {

    public void testParse() {

        try {
            ParserUtil.parse(null);
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            ParserUtil.parse("${noclosingbrackets");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        try {
            ParserUtil.parse("${something}${notclosing");
            fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ignore) {
        }

        //A blank string
        assertEquals(0,ParserUtil.parse("").length());

        assertEquals(System.getProperty("os.name"),ParserUtil.parse("${os.name}"));

        StringBuffer result = new StringBuffer(System.getProperty("java.home"))
                                       .append(System.getProperty("os.name"))
                                       .append(System.getProperty("file.separator"));
        assertEquals(result.toString(),ParserUtil.parse("${java.home}${os.name}${file.separator}"));


        //TODO IllegalArgumentException from System.getProperties would occur in this case.
        //Add this validation in ParserUtil -- Siva
        //ParserUtil.parse("${}");
    }

    public void testPrimitiveGetters() {

        assertEquals("boolean",ParserUtil.getPrimitiveType("boolean").getName());
        assertEquals("byte",ParserUtil.getPrimitiveType("byte").getName());
        assertEquals("char",ParserUtil.getPrimitiveType("char").getName());
        assertEquals("short",ParserUtil.getPrimitiveType("short").getName());
        assertEquals("int",ParserUtil.getPrimitiveType("int").getName());
        assertEquals("long",ParserUtil.getPrimitiveType("long").getName());
        assertEquals("float",ParserUtil.getPrimitiveType("float").getName());
        assertEquals("double",ParserUtil.getPrimitiveType("double").getName());
        assertEquals("void",ParserUtil.getPrimitiveType("void").getName());

        assertEquals("byte",ParserUtil.getVMPrimitiveType("B").getName());
        assertEquals("char",ParserUtil.getVMPrimitiveType("C").getName());
        assertEquals("double",ParserUtil.getVMPrimitiveType("D").getName());
        assertEquals("float",ParserUtil.getVMPrimitiveType("F").getName());
        assertEquals("int",ParserUtil.getVMPrimitiveType("I").getName());
        assertEquals("long",ParserUtil.getVMPrimitiveType("J").getName());
        assertEquals("short",ParserUtil.getVMPrimitiveType("S").getName());
        assertEquals("boolean",ParserUtil.getVMPrimitiveType("Z").getName());
        assertEquals("void",ParserUtil.getVMPrimitiveType("V").getName());

    }

    public void testLoadClass() {

        try {
            ParserUtil.loadClass(null);
            fail("Expected IllegalArgumentException");
        }
        catch(IllegalArgumentException ignore) {
        }
        catch (ClassNotFoundException e) {
            fail("Unexpected classNotFoundException"+e.getMessage());
        }

        try {
            ParserUtil.loadClass("someclass",null);
            fail("Expected IllegalArgumentException");
        }
        catch(IllegalArgumentException ignore) {
        }
        catch (ClassNotFoundException e) {
            fail("Unexpected classNotFoundException"+e.getMessage());
        }

        try {
            Class type = ParserUtil.loadClass("boolean");
            assertEquals(new Boolean(true),ParserUtil.getValue(type,"true",null));

            type = ParserUtil.loadClass("I");
            assertEquals(new Integer(9),ParserUtil.getValue(type,"9",null));

            assertEquals(String.class,ParserUtil.loadClass("Ljava.lang.String;"));
            assertEquals((new int[1][1][]).getClass(),ParserUtil.loadClass("[[[I"));
            assertEquals((new int[1][1][]).getClass(),ParserUtil.loadClass("I[][][]"));

        } catch (ClassNotFoundException e) {
            fail("Unexpected classNotFoundException"+e.getMessage());
        }
    }
}