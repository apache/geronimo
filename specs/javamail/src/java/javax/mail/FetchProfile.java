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

package javax.mail;

import java.util.HashMap;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class FetchProfile {
    public static class Item {
        // Should match Content-Type, Content-Description, Content-Disposition, Size, Line-Count 
        public static final Item CONTENT_INFO = new Item("Content-Info");
        // Should match From, To, Cc, Bcc, Reply-To, Subject, Date, Envelope, Envelope-To
        public static final Item ENVELOPE = new Item("Envelope-To");
        // Can't find any standards for this?
        public static final Item FLAGS = new Item("X-Flags");
        private String _header;

        protected Item(String header) {
            if (header == null) {
                throw new IllegalArgumentException("Header cannot be null");
            }
            _header = header;
        }

        String getHeader() {
            return _header;
        }
    }

    private static final String[] headersType = new String[0];
    private static final Item[] itemsType = new Item[0];
    private Map _items = new HashMap();

    public void add(Item item) {
        _items.put(item._header, item);
    }

    public void add(String header) {
        _items.put(header, new Item(header));
    }

    public boolean contains(Item item) {
        return _items.containsKey(item._header);
    }

    public boolean contains(String header) {
        return _items.containsKey(header);
    }

    public String[] getHeaderNames() {
        return (String[]) _items.keySet().toArray(headersType);
    }

    public Item[] getItems() {
        return (Item[]) _items.values().toArray(itemsType);
    }
}
