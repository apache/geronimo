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

package org.apache.geronimo.common;

import org.apache.geronimo.core.service.StackThreadLocal;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class StackThreadLocalTest 
    extends TestCase
{
    private StackThreadLocal stack;
    private Object           value;

    public StackThreadLocalTest( String name ) 
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        stack = new StackThreadLocal();
        value = new Object();
    }

    /**
     * Test case for {@link org.apache.geronimo.core.service.StackThreadLocal#push()},
     * {@link org.apache.geronimo.core.service.StackThreadLocal#peek()} and {@link org.apache.geronimo.core.service.StackThreadLocal#pop()}
     *
     * @throws Exception
     */
    public void testPushPeekPop()
        throws Exception
    {
        stack.push( value );
        assertEquals( "The objects should be the same", value, stack.peek() );
        assertEquals( "The objects should be the same", value, stack.pop() );
    }

    protected void tearDown()
        throws Exception
    {
        value = null;
    }
}

