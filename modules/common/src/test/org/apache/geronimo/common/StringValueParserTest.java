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

package org.apache.geronimo.common;

import junit.framework.TestCase;

/**
 * Tests for <code>Configuration</code>.
 *
 * @version <code>$Revision: 1.1 $ $Date: 2003/08/24 20:09:25 $</code>
 */
public class StringValueParserTest
    extends TestCase
{
    protected StringValueParser parser;
    
    /**
     * Set up instance variables required by this test case.
     */
    protected void setUp()
    {
        parser = new StringValueParser();
    }
    
    /**
     * Tear down instance variables required by this test case.
     */
    protected void tearDown()
    {
        parser = null;
    }
    
    
    /////////////////////////////////////////////////////////////////////////
    //                               Tests                                 //
    /////////////////////////////////////////////////////////////////////////
    
    public void testDefault() throws Exception
    {
        String value = "${java.home}";
        String result = parser.parse(value);
        assertEquals(result, System.getProperty("java.home"));
    }
    
    public void testSubst() throws Exception
    {
        String value = "BEFORE${java.home}AFTER";
        String result = parser.parse(value);
        assertEquals(result, "BEFORE" + System.getProperty("java.home") + "AFTER");
    }
    
    public void testVariable() throws Exception
    {
        String myvar = "this is my variable";
        parser.getVariables().put("my.var", myvar);
        
        String value = "${my.var}";
        String result = parser.parse(value);
        assertEquals(result, myvar);
    }
    
    public void testFlatVariable() throws Exception
    {
        String myvar = "this is my variable";
        parser.getVariables().put("my.var", myvar);
        parser.getVariables().put("my", "not used");
        
        String value = "${my.var}";
        String result = parser.parse(value);
        assertEquals(result, myvar);
    }
    
    public void testSyntax() throws Exception
    {
        String value = "${java.home";
            
        try {
            String result = parser.parse(value);
            assertTrue("Should have thrown an exception", false);
        }
        catch (Exception ignore) {}
    }
}
