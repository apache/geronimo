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

package org.apache.geronimo.messaging.io;

import java.io.IOException;

import junit.framework.TestCase;

/**
 *
 * @version $Rev$ $Date$
 */
public class AbstractReplacerResolverTest
    extends TestCase
{

    public void testAppend() throws Exception {
        MockReplacerResolver mock1 = new MockReplacerResolver();
        MockReplacerResolver mock2 = new MockReplacerResolver();
        MockReplacerResolver mock3 = new MockReplacerResolver();
        
        mock1.append(mock2);
        mock1.append(mock3);
        
        ReplacerResolver result = mock1.getNext();
        assertEquals(mock2, result);
        result = result.getNext();
        assertEquals(mock3, result);
    }

    public void testOffline() throws Exception {
        MockReplacerResolver mock1 = new MockReplacerResolver();
        MockReplacerResolver mock2 = new MockReplacerResolver();
        MockReplacerResolver mock3 = new MockReplacerResolver();
        
        mock1.append(mock2);
        mock1.append(mock3);
        
        mock2.offline();
        
        ReplacerResolver result = mock1.getNext();
        assertEquals(mock3, result);
        assertTrue(mock2.isOffline());
    }

    public void testReplaceObject() throws Exception {
        MockReplacerResolver mock1 = new MockReplacerResolver();
        MockReplacerResolver mock2 = new MockReplacerResolver();
        MockReplacerResolver mock3 = new MockReplacerResolver();
        mock3.inputReplace = "input";
        mock3.outputReplace = "output";
        
        mock1.append(mock2);
        mock1.append(mock3);
        
        String entry = "";
        Object result = mock1.replaceObject(entry);
        assertEquals(entry, result);

        result = mock1.replaceObject(mock3.inputReplace);
        assertEquals(mock3.outputReplace, result);
    }
    
    public void testResolveObject() throws Exception {
        MockReplacerResolver mock1 = new MockReplacerResolver();
        MockReplacerResolver mock2 = new MockReplacerResolver();
        MockReplacerResolver mock3 = new MockReplacerResolver();
        mock3.inputResolve = "input";
        mock3.outputResolve= "output";
        
        mock1.append(mock2);
        mock1.append(mock3);
        
        String entry = "";
        Object result = mock1.resolveObject(entry);
        assertEquals(entry, result);

        result = mock1.resolveObject(mock3.inputResolve);
        assertEquals(mock3.outputResolve, result);
    }
    
    private static class MockReplacerResolver extends AbstractReplacerResolver {

        private String inputReplace;
        private String outputReplace;
        private String inputResolve;
        private String outputResolve;
        
        protected Object customReplaceObject(Object obj) throws IOException {
            if ( obj.equals(inputReplace) ) {
                return outputReplace;
            }
            return null;
        }

        protected Object customResolveObject(Object obj) throws IOException {
            if ( obj.equals(inputResolve) ) {
                return outputResolve;
            }
            return null;
        }
        
    }
    
}
