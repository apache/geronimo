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

package org.apache.geronimo.twiddle.console;

import java.util.ArrayList;

import junit.framework.TestCase;

import org.apache.geronimo.common.NullArgumentException;

/**
 * Unit test for {@link TransientHistory} class.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:38 $
 */

public class TransientHistoryTest extends TestCase {

    TransientHistory transientHistoryOne;
    TransientHistory transientHistoryTwo;

    public void testConstructor()
    {
        try {
            transientHistoryOne = new TransientHistory(null);
            fail("Should throw NullArgumentException");
        }
        catch (NullArgumentException ignore) {}

        ArrayList arrayList = new ArrayList();
        arrayList.add("Undeploy");
        transientHistoryOne = new TransientHistory(arrayList);

        assertEquals("Undeploy", transientHistoryOne.get(0));
        assertEquals(1, transientHistoryOne.size());
        transientHistoryOne.clear();
        assertEquals(0, transientHistoryOne.size());

        transientHistoryTwo = new TransientHistory();

        transientHistoryTwo.add("Deploy");
        transientHistoryTwo.add("Exit");
        assertEquals("Deploy", transientHistoryTwo.get(0));
        assertEquals("Exit", transientHistoryTwo.get(1));

        assertEquals(2, transientHistoryTwo.size());
        transientHistoryTwo.clear();
        assertEquals(0, transientHistoryOne.size());
        transientHistoryTwo.add("Undeploy");
        assertEquals(1, transientHistoryTwo.size());

    }
}
