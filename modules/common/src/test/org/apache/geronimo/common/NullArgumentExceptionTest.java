/**
 *
 * Copyright 2004 The Apache Software Foundation
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

import junit.framework.TestCase;

/**
 * Unit test for {@link NullArgumentException} class.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:04 $
 */
public class NullArgumentExceptionTest extends TestCase {

    public void testCheckForNull() {
        try {
            NullArgumentException.checkForNull("notNull", "notNull");
        } catch (NullArgumentException nae) {
            fail("Should not throw NullArgumentException for non null object");
        }
        
        try {
            NullArgumentException.checkForNull("notNull", null);
            fail("Expected NullArgumentException for null object");
        } catch (NullArgumentException nae) {
            //ignore it
        }
    }

}
