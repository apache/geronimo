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

import org.apache.geronimo.core.service.Invocation;
import org.apache.geronimo.core.service.SimpleInvocation;
import org.apache.geronimo.core.service.StringInvocationKey;

import junit.framework.TestCase;

/**
 * @version $Revision: 1.5 $ $Date: 2004/03/10 09:58:44 $
 */
public class SimpleInvocationTest 
    extends TestCase
{
    private Invocation invocation;
    private StringInvocationKey  key;
    private Object     value;

    public SimpleInvocationTest( String name ) 
    {
        super( name );
    }

    protected void setUp()
        throws Exception
    {
        invocation = new SimpleInvocation();
        key        = new StringInvocationKey("Test", false);
        value      = new Object();
    }

    /**
     * Test case for {@link org.apache.geronimo.core.service.SimpleInvocation#getMarshal()}
     * and {@link org.apache.geronimo.core.service.SimpleInvocation#putMarshal( Object, Object )}
     *
     * @throws Exception
     */
    public void testGetPut()
        throws Exception
    {
        invocation.put( key, value );
        assertEquals( "Objects should match", value, invocation.get( key ) );
    }

    protected void tearDown()
        throws Exception
    {
        invocation = null;
        key        = null;
        value      = null;
    }
}
