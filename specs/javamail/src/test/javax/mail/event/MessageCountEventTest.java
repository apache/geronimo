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

package javax.mail.event;
import javax.mail.Folder;
import javax.mail.TestData;
import junit.framework.TestCase;
/**
 * @version $Rev$ $Date$
 */
public class MessageCountEventTest extends TestCase {
    public MessageCountEventTest(String name) {
        super(name);
    }
    public void testEvent() {
        doEventTests(MessageCountEvent.ADDED);
        doEventTests(MessageCountEvent.REMOVED);
        try {
            doEventTests(-12345);
            fail("Expected exception due to invalid type -12345");
        } catch (IllegalArgumentException e) {
        }
    }
    private void doEventTests(int type) {
        Folder folder = TestData.getTestFolder();
        MessageCountEvent event =
            new MessageCountEvent(folder, type, false, null);
        assertEquals(folder, event.getSource());
        assertEquals(type, event.getType());
        MessageCountListenerTest listener = new MessageCountListenerTest();
        event.dispatch(listener);
        assertEquals("Unexpcted method dispatched", type, listener.getState());
    }
    public static class MessageCountListenerTest
        implements MessageCountListener {
        private int state = 0;
        public void messagesAdded(MessageCountEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = MessageCountEvent.ADDED;
        }
        public void messagesRemoved(MessageCountEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = MessageCountEvent.REMOVED;
        }
        public int getState() {
            return state;
        }
    }
}
