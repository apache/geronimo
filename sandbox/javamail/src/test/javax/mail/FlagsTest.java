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
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import junit.framework.TestCase;
/**
 * @version $Revision: 1.1 $ $Date: 2004/01/29 04:20:05 $
 */
public class FlagsTest extends TestCase {
    private List flagtypes;
    private Flags flags;
    /**
     * Constructor for FlagsTest.
     * @param arg0
     */
    public FlagsTest(String name) {
        super(name);
    }
    /*
     * @see TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        flags = new Flags();
        flagtypes = new LinkedList();
        flagtypes.add(Flags.Flag.ANSWERED);
        flagtypes.add(Flags.Flag.DELETED);
        flagtypes.add(Flags.Flag.DRAFT);
        flagtypes.add(Flags.Flag.FLAGGED);
        flagtypes.add(Flags.Flag.RECENT);
        flagtypes.add(Flags.Flag.SEEN);
        Collections.shuffle(flagtypes);
    }
    public void testHashCode() {
        int before = flags.hashCode();
        flags.add("Test");
        assertTrue(
            "Before: " + before + ", now " + flags.hashCode(),
            flags.hashCode() != before);
        assertTrue(flags.hashCode() != 0);
    }
    /*
     * Test for void add(Flag)
     */
    public void testAddAndRemoveFlag() {
        Iterator it = flagtypes.iterator();
        while (it.hasNext()) {
            Flags.Flag flag = (Flags.Flag) it.next();
            assertFalse(flags.contains(flag));
            flags.add(flag);
            assertTrue(flags.contains(flag));
        }
        it = flagtypes.iterator();
        while (it.hasNext()) {
            Flags.Flag flag = (Flags.Flag) it.next();
            flags.remove(flag);
            assertFalse(flags.contains(flag));
        }
    }
    /*
     * Test for void add(String)
     */
    public void testAddString() {
        assertFalse(flags.contains("Frog"));
        flags.add("Frog");
        assertTrue(flags.contains("Frog"));
        flags.remove("Frog");
        assertFalse(flags.contains("Frog"));
    }
    /*
     * Test for void add(Flags)
     */
    public void testAddFlags() {
        Flags other = new Flags();
        other.add("Stuff");
        other.add(Flags.Flag.RECENT);
        flags.add(other);
        assertTrue(flags.contains("Stuff"));
        assertTrue(flags.contains(Flags.Flag.RECENT));
        assertTrue(flags.contains(other));
        assertTrue(flags.contains(flags));
        flags.add("Thing");
        assertTrue(flags.contains("Thing"));
        flags.remove(other);
        assertFalse(flags.contains("Stuff"));
        assertFalse(flags.contains(Flags.Flag.RECENT));
        assertFalse(flags.contains(other));
        assertTrue(flags.contains("Thing"));
    }
    /*
     * Test for boolean equals(Object)
     */
    public void testEqualsObject() {
        Flags other = new Flags();
        other.add("Stuff");
        other.add(Flags.Flag.RECENT);
        flags.add(other);
        assertEquals(flags, other);
    }
    public void testGetSystemFlags() {
        flags.add("Stuff");
        flags.add("Another");
        flags.add(Flags.Flag.FLAGGED);
        flags.add(Flags.Flag.RECENT);
        Flags.Flag[] array = flags.getSystemFlags();
        assertEquals(2, array.length);
        assertTrue(
            (array[0] == Flags.Flag.FLAGGED && array[1] == Flags.Flag.RECENT)
                || (array[0] == Flags.Flag.RECENT
                    && array[1] == Flags.Flag.FLAGGED));
    }
    public void testGetUserFlags() {
        final String stuff = "Stuff";
        final String another = "Another";
        flags.add(stuff);
        flags.add(another);
        flags.add(Flags.Flag.FLAGGED);
        flags.add(Flags.Flag.RECENT);
        String[] array = flags.getUserFlags();
        assertEquals(2, array.length);
        assertTrue(
            (array[0] == stuff && array[1] == another)
                || (array[0] == another && array[1] == stuff));
    }
    public void testClone() throws CloneNotSupportedException {
        flags.add("Thing");
        flags.add(Flags.Flag.RECENT);
        Flags other = (Flags) flags.clone();
        assertTrue(other != flags);
        assertEquals(other, flags);
    }
}
