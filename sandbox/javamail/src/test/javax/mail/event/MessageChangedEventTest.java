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
import junit.framework.TestCase;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:30 $
 */
public class MessageChangedEventTest extends TestCase {
    public MessageChangedEventTest(String name) {
        super(name);
    }
    public void testEvent() {
        doEventTests(MessageChangedEvent.ENVELOPE_CHANGED);
        doEventTests(MessageChangedEvent.FLAGS_CHANGED);
        try {
            MessageChangedEvent event =
                new MessageChangedEvent(this, -12345, null);
            fail(
                "Expected exception due to invalid type "
                    + event.getMessageChangeType());
        } catch (IllegalArgumentException e) {
        }
    }
    private void doEventTests(int type) {
        MessageChangedEvent event = new MessageChangedEvent(this, type, null);
        assertEquals(this, event.getSource());
        assertEquals(type, event.getMessageChangeType());
        MessageChangedListenerTest listener = new MessageChangedListenerTest();
        event.dispatch(listener);
        assertEquals("Unexpcted method dispatched", type, listener.getState());
    }
    public static class MessageChangedListenerTest
        implements MessageChangedListener {
        private int state = 0;
        public void messageChanged(MessageChangedEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = event.getMessageChangeType();
        }
        public int getState() {
            return state;
        }
    }
}
