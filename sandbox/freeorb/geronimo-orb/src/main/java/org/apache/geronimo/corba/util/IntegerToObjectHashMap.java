/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.util;

import java.util.NoSuchElementException;


/**
 * @version $Revision$ $Date$
 */
public class IntegerToObjectHashMap implements IntegerToObjectMap {

    public static final Object NO_VALUE = null;

    protected int size;

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    protected static int keyHash(int key) {
        return key;
    }

    protected static boolean keyEquals(int key1, int key2) {
        return key1 == key2;
    }

    static int valueHash(Object value) {
        return value == null ? 0 : value.hashCode();
    }

    static boolean valueEquals(Object value1, Object value2) {
        return value1 == null ? value2 == null : value1.equals(value2);
    }

    static class Entry implements IntegerToObjectMap.Entry {

        public int hashCode() {
            return keyHash(getKey()) ^ valueHash(getValue());
        }

        public boolean equals(Object other) {
            if (other instanceof Entry) {
                Entry ent = (Entry) other;
                return keyEquals(getKey(), ent.getKey())
                       && valueEquals(getValue(), ent.getValue());
            } else {
                return false;
            }
        }

        Entry next;

        int key;
        Object value;

        private int hash;

        Entry(int hash, int key, Object value, Entry next) {
            this.key = key;
            this.value = value;
            this.next = next;
            this.hash = hash;
        }

        public int getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

        public Object setValue(Object value2) {
            Object result = this.value;
            this.value = value2;
            return result;
        }

        boolean sameKey(int hash, int key) {
            return keyEquals(getKey(), key);
        }

        /**
         * @return
         */
        public int getHash() {
            return hash;
        }
    }

    /**
     * the hash index
     */
    private Entry[] table;

    /**
     * the current range for table.
     */
    private int range;

    private float ratio;

    private static final int DEFAULT_RANGE = 17;

    /**
     * translate hash code bucket to index
     */
    private int index(int hash) {
        return (hash & 0x7ffffff) % range;
    }

    /**
     * the default and only constructor
     */
    public IntegerToObjectHashMap() {
        clear();
    }

    /**
     * the default and only constructor
     */
    public IntegerToObjectHashMap(int size) {
        clear(size);
    }

    public void clear() {
        clear(DEFAULT_RANGE);
    }

    protected void clear(int initial_range) {
        if (initial_range == 0) {
            initial_range = 3;
        }
        range = initial_range;
        size = 0;
        ratio = 0.75F;
        table = new Entry[range];
    }

    /**
     * return the element with the given key
     */
    public Object get(int key) {
        int hash = keyHash(key);
        return get(hash, key);
    }

    private Object get(int hash, int key) {
        int idx = index(hash);

        for (Entry ent = table[idx]; ent != null; ent = ent.next) {
            if (ent.sameKey(hash, key))
                return ent.value;
        }

        return NO_VALUE;
    }

    /**
     * return the element with the given key
     */
    public boolean containsKey(int key) {
        int hash = keyHash(key);
        return containsKey(hash, key);
    }

    private boolean containsKey(int hash, int key) {
        int idx = index(hash);

        for (Entry ent = table[idx]; ent != null; ent = ent.next) {
            if (ent.sameKey(hash, key))
                return true;
        }

        return false;
    }

    public Object put(int key, Object value) {
        int hash = keyHash(key);
        return put(hash, key, value);
    }

    private Object put(int hash, int key, Object value) {
        int idx = index(hash);

        for (Entry ent = table[idx]; ent != null; ent = ent.next) {
            if (ent.sameKey(hash, key)) {
                return ent.setValue(value);
            }
        }

        if (1.0F * size / range > ratio) {
            grow();
            idx = index(hash);
        }

        table[idx] = new Entry(hash, key, value, table[idx]);

        size += 1;

        return NO_VALUE;
    }

    public Object remove(int key) {
        int hash = keyHash(key);
        return remove(hash, key);
    }

    private Object remove(int hash, int key) {
        int idx = index(hash);

        Entry entry = table[idx];
        if (entry != null) {

            if (entry.sameKey(hash, key)) {
                table[idx] = entry.next;
                size -= 1;
                return entry.getValue();

            } else {
                Entry ahead = entry.next;

                while (ahead != null) {
                    if (ahead.sameKey(hash, key)) {
                        entry.next = ahead.next;
                        size -= 1;
                        return ahead.getValue();
                    }

                    entry = ahead;
                    ahead = ahead.next;
                }
            }
        }

        // it was not found at all!
        return NO_VALUE;
    }

    public Object removeEntry(Entry ent) {
        int hash = ent.getHash();
        int idx = index(hash);

        Entry entry = table[idx];
        if (entry != null) {

            if (entry == ent) {
                table[idx] = entry.next;
                size -= 1;
                return entry.getValue();

            } else {
                Entry ahead = entry.next;

                while (ahead != null) {
                    if (ahead == ent) {
                        entry.next = ahead.next;
                        size -= 1;
                        return ahead.getValue();
                    }

                    entry = ahead;
                    ahead = ahead.next;
                }
            }
        }

        // it was not found at all!
        return NO_VALUE;
    }

    private void grow() {
        int old_range = range;
        Entry[] old_table = table;

        range = old_range * 2 + 1;
        table = new Entry[range];

        for (int i = 0; i < old_range; i++) {
            Entry entry = old_table[i];

            while (entry != null) {
                Entry ahead = entry.next;
                int idx = index(keyHash(entry.getKey()));
                entry.next = table[idx];
                table[idx] = entry;
                entry = ahead;
            }
        }
    }

    final class EntryIterator implements IntegerToObjectMap.EntryIterator {

        int idx;

        Entry entry;

        EntryIterator() {
            idx = 0;
            entry = table[0];
            locateNext();
        }

        private void locateNext() {
            // we reached the end of a list
            while (entry == null) {
                // goto next bucket
                idx += 1;
                if (idx == range) {
                    // we reached the end
                    return;
                }

                // entry is the first element of this bucket
                entry = table[idx];
            }
        }

        public boolean hasNext() {
            return (entry != null);
        }

        public IntegerToObjectMap.Entry next() {
            Entry result = entry;

            if (result == null) {
                throw new NoSuchElementException();
            } else {
                entry = entry.next;
                locateNext();
                return result;
            }
        }

        public void remove() {
            Entry remove = entry;

            entry = entry.next;
            locateNext();

            IntegerToObjectHashMap.this.removeEntry(remove);
        }
    }

    public IntegerToObjectMap.EntryIterator entryIterator() {
        return new EntryIterator();
    }

    /**
     * @param map
     */
    public void putAll(IntegerToObjectMap map) {
        IntegerToObjectMap.EntryIterator it = map.entryIterator();
        while (it.hasNext()) {
            IntegerToObjectMap.Entry ent = it.next();
            int o = ent.getKey();
            put(o, ent.getValue());
        }
    }

    public IntIterator keyIterator() {
        final IntegerToObjectMap.EntryIterator iter = entryIterator();
        return new IntIterator() {

            public int next() {
                IntegerToObjectMap.Entry ent = iter.next();
                return ent.getKey();
            }

            public boolean hasNext() {
                return iter.hasNext();
            }

            public int size() {
                return IntegerToObjectHashMap.this.size();
            }

        };
    }

}
