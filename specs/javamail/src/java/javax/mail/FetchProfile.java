/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//
package javax.mail;
import java.util.HashMap;
import java.util.Map;
/**
 * @version $Revision: 1.2 $ $Date: 2003/08/16 04:29:52 $
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
        public boolean equals(Object other) {
            if (other == null || other.getClass() != this.getClass()) {
                return false;
            }
            return ((Item) other)._header.equals(_header);
        }
        String getHeader() {
            return _header;
        }
        public int hashCode() {
            return _header.hashCode();
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
