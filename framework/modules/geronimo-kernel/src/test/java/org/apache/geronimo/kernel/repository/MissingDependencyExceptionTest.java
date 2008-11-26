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


package org.apache.geronimo.kernel.repository;

import java.util.Stack;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MissingDependencyExceptionTest extends TestCase {

    public void testWithQuery() throws Exception {
        Artifact a = new Artifact("g", "a", "1", "jar");
        MissingDependencyException e = new MissingDependencyException(a, (Artifact)null);
        assertEquals("Missing dependency: g/a/1/jar", e.getMessage());
        e = new MissingDependencyException("foo", a, (Artifact)null);
        assertEquals("foo\n" +
                "Missing dependency: g/a/1/jar", e.getMessage());

    }

    String withStack = "Missing dependency: g/a/1/jar\n" +
            "Parent stack:\n" +
            "    g/p1/1/car\n";
    public void testWithStack() throws Exception {
        Artifact a = new Artifact("g", "a", "1", "jar");
        Artifact b = new Artifact("g", "p1", "1", "car");
        MissingDependencyException e = new MissingDependencyException(a, b);
        assertEquals(withStack, e.getMessage());
        e = new MissingDependencyException("foo", a, b);
        assertEquals("foo\n" + withStack, e.getMessage());

        Stack<Artifact> stack = new Stack<Artifact>();
        stack.add(b);
        e = new MissingDependencyException(a, stack);
        assertEquals(withStack, e.getMessage());
        e = new MissingDependencyException("foo", a, stack);
        assertEquals("foo\n" + withStack, e.getMessage());

    }

}
