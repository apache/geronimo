/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.testsupport;

import org.w3c.dom.Document;

import junit.framework.TestCase;

public class DOMUtilsTest extends TestCase {
    
    public void testSame() throws Exception {
        String expected = "<foo x1=\"bar\">hello</foo>";
        Document expectedDoc = DOMUtils.load(expected);        
        DOMUtils.compareNodes(expectedDoc, expectedDoc);               
    } 
    
    public void testElement() throws Exception {
        String expected = "<foo>hello</foo>";
        String actual = "<bar>hello</bar>";
        compare(expected, actual);
    }
    
    public void testElementNS() throws Exception {
        String expected = "<foo>hello</foo>";
        String actual = "<foo xmlns=\"foo\">hello</foo>";
        compare(expected, actual);
    }
    
    public void testElementChildren() throws Exception {
        String expected = "<foo><x1/><x2/></foo>";
        String actual = "<foo><x1/><x2/><x3/></foo>";
        compare(expected, actual);
    }
    
    public void testChildrenType() throws Exception {
        String expected = "<foo><x1/></foo>";
        String actual = "<foo>text</foo>";
        compare(expected, actual);
    }
    
    public void testText() throws Exception {
        String expected = "<foo>hello</foo>";
        String actual = "<bar>helloFoo</bar>";
        compare(expected, actual);
    }

    public void testAttribute() throws Exception {
        String expected = "<foo x1=\"bar\">hello</foo>";
        String actual = "<foo x1=\"bar2\">hello</foo>";
        compare(expected, actual);
    }
    
    public void testAttributeNS() throws Exception {
        String expected = "<foo xmlns:b=\"foo\" b:x1=\"bar\">hello</foo>";
        String actual = "<foo xmlns:c=\"foo\" x1=\"bar2\">hello</foo>";
        compare(expected, actual);
    }
    
    private void compare(String x1, String x2) throws Exception {
        Document d1 = DOMUtils.load(x1);
        Document d2 = DOMUtils.load(x2);   
        
        try {
            DOMUtils.compareNodes(d1, d2);
            fail("Did not throw exception");
        } catch (Exception e) {
            e.printStackTrace(System.out);
        }
    }
}
