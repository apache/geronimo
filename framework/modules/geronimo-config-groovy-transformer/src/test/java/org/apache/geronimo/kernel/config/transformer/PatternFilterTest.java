/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.kernel.config.transformer;

import java.io.File;
import java.util.regex.Pattern;

import junit.framework.TestCase;

/**
 *
 * @version $Rev:$ $Date:$
 */
public class PatternFilterTest extends TestCase {

    private PatternFilter filter;

    @Override
    protected void setUp() throws Exception {
        Pattern pattern = Pattern.compile("prefix-(.*).groovy");
        filter = new PatternFilter(pattern);
    }
    
    public void testMatchingFileReturnsTrue() throws Exception {
        assertTrue(filter.accept(new File("mock/dir/prefix-123.groovy")));
    }
    
    public void testNotMatchingFileReturnsFalse() throws Exception {
        assertFalse(filter.accept(new File("mock/dir/undefined-123.groovy")));
    }
    
}
