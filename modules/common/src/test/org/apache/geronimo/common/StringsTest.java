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

import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for {@link Strings} class.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/16 15:08:53 $
 */
public class StringsTest extends TestCase {
    
    public void testCapitalize() {
        assertEquals("Apache", Strings.capitalize("apache"));
        assertEquals("ALLCAPS", Strings.capitalize("ALLCAPS"));
        
        try {
            Strings.capitalize(null);
            fail("Expected IllegalArgumnetException to be thrown");
        } catch (IllegalArgumentException ignore) {}
        
        try {
            Strings.capitalize("");
            fail("Expected IllegalArgumnetException to be thrown");
        } catch (IllegalArgumentException ignore) {}
    }
    
    public void testIsEmpty() {
        assertTrue(Strings.isEmpty(""));
        assertFalse(Strings.isEmpty("  "));
        
        try {
            Strings.isEmpty(null);
            fail("Expected NullArgumentException to be thrown");
        } catch (NullArgumentException ignore) {}
    }
    
    public void testCompare() {
        assertTrue(Strings.compare(null, null));
        assertFalse(Strings.compare("Hello", "hello"));
        assertTrue(Strings.compare("Hello", new String("Hello")));
        assertTrue(Strings.compare("Hello", "Hello"));
    }
    
    public void testPadWithBuffer() {
        StringBuffer buffer = new StringBuffer("Hello");
        assertEquals("Hello*****", Strings.pad(buffer, "*", 5));
    }
    
    public void testPad() {
        assertEquals("*****", Strings.pad("*", 5));
    }
    
    public void testPadWithObject() {
        Integer integer = new Integer(1);
        assertEquals("11111", Strings.pad(integer, 5));
    }
    
    public void testCount() {
        assertEquals(3, Strings.count("Merry go round merry go round go round", "go"));
    }
    
    public void testCountWithChar() {
        assertEquals(5, Strings.count("abracadabra", 'a'));
    }
    
    public void testNthIndexOf() {
        String toSearch = "1234-1234-1234";
        assertEquals(7, Strings.nthIndexOf(toSearch, "3", 2));
        assertEquals(-1, Strings.nthIndexOf(toSearch, "5", 2));
    }
    
    public void testSubst() {
        StringBuffer buffer = new StringBuffer();
        
        assertEquals("The world is not enough", Strings.subst(buffer, "basta", "enough", "The world is not basta"));
        assertEquals("The world is not enough", Strings.subst("basta", "enough", "The world is not basta"));
    }
    
    public void testSubstWithMap() {
        StringBuffer buffer = new StringBuffer();
        
        Map map = new HashMap();
        map.put("The", "il");
        map.put("world", "mondo");
        map.put("not", "non");
        map.put("enough", "basta");
        
        assertEquals("ilmondononbasta", Strings.subst(buffer, "<The><world><not><enough>", map, "<", ">"));
        assertEquals("ilmondononbasta", Strings.subst("<The><world><not><enough>", map, "<", ">"));
    }
    
    public void testTrim() {
        String[] toTrim = {"   Hello ", "foo bar", "ciao"};
        Strings.trim(toTrim);
        
        assertEquals("Hello", toTrim[0]);
        assertEquals("foo bar", toTrim[1]);
        assertEquals("ciao", toTrim[2]);
    }
    
    public void testJoin() {
        StringBuffer buffer = new StringBuffer();
        Object[] objArray = {"Java", new Integer(2), "rocks"};
        
        assertEquals("Java 2 rocks", Strings.join(buffer, objArray, " "));
        
        buffer = new StringBuffer();
        assertEquals("<<Java 2 rocks>>", Strings.join(buffer, objArray, "<<", " ", ">>"));
        
        assertEquals("Java 2 rocks", Strings.join(objArray, " "));
        assertEquals("Java2rocks", Strings.join(objArray));
        
    }
    
    public void testSplit() {
        String toSplit = "one,two,three,four,five";
        
        String[] strArray = Strings.split(toSplit, ",", 3);
        
        assertEquals(3, strArray.length);
        assertEquals("one", strArray[0]);
        assertEquals("two", strArray[1]);
        assertEquals("three,four,five", strArray[2]);
        
        strArray = Strings.split(toSplit, ",");
        
        assertEquals(5, strArray.length);
        assertEquals("one", strArray[0]);
        assertEquals("two", strArray[1]);
        assertEquals("three", strArray[2]);
        assertEquals("four", strArray[3]);
        assertEquals("five", strArray[4]);
    }
}
