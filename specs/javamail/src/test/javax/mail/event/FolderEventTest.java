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
 * @version $Rev$ $Date$
 */
public class FolderEventTest extends TestCase {
    public FolderEventTest(String name) {
        super(name);
    }
    public void testEvent() {
        doEventTests(FolderEvent.CREATED);
        doEventTests(FolderEvent.RENAMED);
        doEventTests(FolderEvent.DELETED);
        try {
            FolderEvent event = new FolderEvent(this, null, -12345);
            fail("Expected exception due to invalid type " + event.getType());
        } catch (IllegalArgumentException e) {
        }
    }
    private void doEventTests(int type) {
        FolderEvent event = new FolderEvent(this, null, type);
        assertEquals(this, event.getSource());
        assertEquals(type, event.getType());
        FolderListenerTest listener = new FolderListenerTest();
        event.dispatch(listener);
        assertEquals("Unexpcted method dispatched", type, listener.getState());
    }
    public static class FolderListenerTest implements FolderListener {
        private int state = 0;
        public void folderCreated(FolderEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = FolderEvent.CREATED;
        }
        public void folderDeleted(FolderEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = FolderEvent.DELETED;
        }
        public void folderRenamed(FolderEvent event) {
            if (state != 0) {
                fail("Recycled Listener");
            }
            state = FolderEvent.RENAMED;
        }
        public int getState() {
            return state;
        }
    }
}
