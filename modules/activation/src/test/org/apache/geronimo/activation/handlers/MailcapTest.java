/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.activation.handlers;

import java.io.InputStream;
import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.activation.CommandInfo;

import junit.framework.TestCase;

/**
 * @version $Rev$ $Date$
 */
public class MailcapTest extends TestCase {
    private CommandMap map;

    public void testTextPlainHandler() {
        CommandInfo info = map.getCommand("text/plain", "content-handler");
        assertEquals(TextPlainHandler.class.getName(), info.getCommandClass());
    }

    protected void setUp() throws Exception {
        InputStream is = TextPlainHandler.class.getClassLoader().getResourceAsStream("META-INF/mailcap");
        map = new MailcapCommandMap(is);
        is.close();
    }
}
