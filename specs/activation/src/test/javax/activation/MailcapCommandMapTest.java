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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.activation;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MailcapCommandMapTest extends TestCase {
    private MailcapCommandMap map;

    public void testAdd() {
        map.addMailcap("foo/bar ;; x-java-view=Foo; x-java-edit=Bar");
        CommandInfo info = map.getCommand("foo/bar", "view");
        assertEquals("view", info.getCommandName());
        assertEquals("Foo", info.getCommandClass());
        info = map.getCommand("foo/bar", "edit");
        assertEquals("edit", info.getCommandName());
        assertEquals("Bar", info.getCommandClass());
    }

    public void testExplicitWildcard() {
        map.addMailcap("foo/bar ;; x-java-view=Bar");
        map.addMailcap("foo/* ;; x-java-view=Star");
        CommandInfo info = map.getCommand("foo/bar", "view");
        assertEquals("view", info.getCommandName());
        assertEquals("Bar", info.getCommandClass());
        info = map.getCommand("foo/foo", "view");
        assertEquals("view", info.getCommandName());
        assertEquals("Star", info.getCommandClass());
        info = map.getCommand("foo/*", "view");
        assertEquals("view", info.getCommandName());
        assertEquals("Star", info.getCommandClass());
        info = map.getCommand("foo", "view");
        assertEquals("view", info.getCommandName());
        assertEquals("Star", info.getCommandClass());
    }

    public void testImplicitWildcard() {
        map.addMailcap("foo/bar ;; x-java-view=Bar");
        map.addMailcap("foo ;; x-java-view=Star");
        CommandInfo info = map.getCommand("foo/bar", "view");
        assertEquals("view", info.getCommandName());
        assertEquals("Bar", info.getCommandClass());
        info = map.getCommand("foo/foo", "view");
        assertEquals("view", info.getCommandName());
        assertEquals("Star", info.getCommandClass());
        info = map.getCommand("foo", "view");
        assertEquals("view", info.getCommandName());
        assertEquals("Star", info.getCommandClass());
    }

    protected void setUp() throws Exception {
        super.setUp();
        map = new MailcapCommandMap();
    }
}
