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

import java.sql.SQLWarning;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Unit test for {@link ThrowableHandler} class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/18 16:54:41 $
 */

public class ThrowableHandlerTest extends TestCase {

    MockThrowableListener listenerOne;
    MockThrowableListener listenerTwo;

    protected void setUp() throws Exception {

        super.setUp();

        listenerOne = new MockThrowableListener();
        listenerTwo = new MockThrowableListener();
    }

    public void testAddAndFire() {

        ThrowableHandler.addThrowableListener(listenerOne);
        ThrowableHandler.addThrowableListener(listenerTwo);

        //Duplicate listener.Should not be registered.else our expected size of lists will differ
        ThrowableHandler.addThrowableListener(listenerOne);

        ThrowableHandler.addError(new IllegalArgumentException());
        ThrowableHandler.addError(new NullPointerException());
        ThrowableHandler.addWarning(new SQLWarning());
        ThrowableHandler.add(new Exception());

        ArrayList errorListOne = listenerOne.getErrorList();
        ArrayList errorListTwo = listenerTwo.getErrorList();

        assertEquals(2, errorListOne.size());
        assertEquals(2, errorListTwo.size());

        assertEquals("java.lang.IllegalArgumentException", ((errorListOne.get(0)).getClass().getName()));
        assertEquals("java.lang.NullPointerException", ((errorListOne.get(1)).getClass().getName()));

        assertEquals("java.lang.IllegalArgumentException", ((errorListTwo.get(0)).getClass().getName()));
        assertEquals("java.lang.NullPointerException", ((errorListTwo.get(1)).getClass().getName()));


        assertEquals(1, listenerOne.getWarningList().size());
        assertEquals(1, listenerTwo.getWarningList().size());

        assertEquals("java.sql.SQLWarning", ((listenerOne.getWarningList().get(0)).getClass().getName()));
        assertEquals("java.sql.SQLWarning", ((listenerTwo.getWarningList().get(0)).getClass().getName()));

        assertEquals(1, listenerOne.getUnknownList().size());
        assertEquals(1, listenerTwo.getUnknownList().size());

        assertEquals("java.lang.Exception", ((listenerOne.getUnknownList().get(0)).getClass().getName()));
        assertEquals("java.lang.Exception", ((listenerTwo.getUnknownList().get(0)).getClass().getName()));

    }

    protected void tearDown() throws Exception {

        listenerOne = null;
        listenerTwo = null;

        super.tearDown();
    }
}