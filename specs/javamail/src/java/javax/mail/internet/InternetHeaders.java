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

package javax.mail.internet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.mail.Address;
import javax.mail.Header;
import javax.mail.MessagingException;

/**
 * Class that represents the RFC822 headers associated with a message.
 *
 * @version $Rev$ $Date$
 */
public class InternetHeaders {
    // RFC822 imposes an ordering on its headers so we use a LinkedHashedMap
    private final LinkedHashMap headers = new LinkedHashMap();

    /**
     * Create an empty InternetHeaders
     */
    public InternetHeaders() {
        // we need to initialize the headers in the correct order
        // fields: dates source destination optional-field
        // dates:
        setHeaderList("Date", null);
        setHeaderList("Resent-Date", null);
        // source: trace originator resent
        // trace: return received
        setHeaderList("Return-path", null);
        setHeaderList("Received", null);
        // originator: authentic reply-to
        setHeaderList("Sender", null);
        setHeaderList("From", null);
        setHeaderList("Reply-To", null);
        // resent: resent-authentic resent-reply-to
        setHeaderList("Resent-Sender", null);
        setHeaderList("Resent-From", null);
        setHeaderList("Resent-Reply-To", null);
        // destination:
        setHeaderList("To", null);
        setHeaderList("Resent-To", null);
        setHeaderList("cc", null);
        setHeaderList("Resent-cc", null);
        setHeaderList("bcc", null);
        setHeaderList("Resent-bcc", null);
        // optional-field:
        setHeaderList("Message-ID", null);
        setHeaderList("Resent-Message-ID", null);
        setHeaderList("In-Reply-To", null);
        setHeaderList("References", null);
        setHeaderList("Keywords", null);
        setHeaderList("Subject", null);
        setHeaderList("Comments", null);
        setHeaderList("Encrypted", null);
    }

    /**
     * Create a new InternetHeaders initialized by reading headers from the stream.
     *
     * @param in the RFC822 input stream to load from
     * @throws MessagingException if there is a problem pasring the stream
     */
    public InternetHeaders(InputStream in) throws MessagingException {
        load(in);
    }

    /**
     * Read and parse the supplied stream and add all headers to the current set.
     *
     * @param in the RFC822 input stream to load from
     * @throws MessagingException if there is a problem pasring the stream
     */
    public void load(InputStream in) throws MessagingException {
        try {
            StringBuffer name = new StringBuffer(32);
            StringBuffer value = new StringBuffer(128);
            done: while (true) {
                int c = in.read();
                char ch = (char) c;
                if (c == -1) {
                    break;
                } else if (c == 13) {
                    // empty line terminates header
                    in.read(); // skip LF
                    break;
                } else if (Character.isWhitespace(ch)) {
                    // handle continuation
                    do {
                        c = in.read();
                        if (c == -1) {
                            break done;
                        }
                        ch = (char) c;
                    } while (Character.isWhitespace(ch));
                }  else {
                    // new header
                    if (name.length() > 0) {
                        addHeader(name.toString().trim(), value.toString().trim());
                    }
                    name.setLength(0);
                    value.setLength(0);
                    while (true) {
                        name.append((char)c);
                        c = in.read();
                        if (c == -1) {
                            break done;
                        } else if (c == ':') {
                            break;
                        }
                    }
                    c = in.read();
                    if (c == -1) {
                        break done;
                    }
                }

                while (c != 13) {
                    ch = (char) c;
                    value.append(ch);
                    c = in.read();
                    if (c == -1) {
                        break done;
                    }
                }
                // skip LF
                in.read();
            }
            if (name.length() > 0) {
                addHeader(name.toString().trim(), value.toString().trim());
            }
        } catch (IOException e) {
            throw new MessagingException("Error loading headers", e);
        }
    }

    /**
     * Return all the values for the specified header.
     *
     * @param name the header to return
     * @return the values for that header, or null if the header is not present
     */
    public String[] getHeader(String name) {
        List headers = getHeaderList(name);
        if (headers == null) {
            return null;
        } else {
            String[] result = new String[headers.size()];
            for (int i = 0; i < headers.size(); i++) {
                InternetHeader header = (InternetHeader) headers.get(i);
                result[i] = header.getValue();
            }
            return result;
        }
    }

    /**
     * Return the values for the specified header as a single String.
     * If the header has more than one value then all values are concatenated
     * together separated by the supplied delimiter.
     *
     * @param name      the header to return
     * @param delimiter the delimiter used in concatenation
     * @return the header as a single String
     */
    public String getHeader(String name, String delimiter) {
        List list = getHeaderList(name);
        if (list == null) {
            return null;
        } else if (list.isEmpty()) {
            return "";
        } else if (list.size() == 1) {
            return ((InternetHeader) list.get(0)).getValue();
        } else {
            StringBuffer buf = new StringBuffer(20 * list.size());
            buf.append(((InternetHeader) list.get(0)).getValue());
            for (int i = 1; i < list.size(); i++) {
                buf.append(delimiter);
                buf.append(((InternetHeader) list.get(i)).getValue());
            }
            return buf.toString();
        }
    }

    /**
     * Set the value of the header to the supplied value; any existing
     * headers are removed.
     *
     * @param name  the name of the header
     * @param value the new value
     */
    public void setHeader(String name, String value) {
        List list = new ArrayList();
        list.add(new InternetHeader(name, value));
        setHeaderList(name, list);
    }

    /**
     * Add a new value to the header with the supplied name.
     *
     * @param name  the name of the header to add a new value for
     * @param value another value
     */
    public void addHeader(String name, String value) {
        List list = getHeaderList(name);
        if (list == null) {
            list = new ArrayList();
            headers.put(name.toLowerCase(), list);
        }
        list.add(new InternetHeader(name, value));
    }

    /**
     * Remove all header entries with the supplied name
     *
     * @param name the header to remove
     */
    public void removeHeader(String name) {
        List list = getHeaderList(name);
        list.clear();
    }

    /**
     * Return all headers.
     *
     * @return an Enumeration<Header> containing all headers
     */
    public Enumeration getAllHeaders() {
        List result = new ArrayList(headers.size() * 2);
        Iterator it = headers.values().iterator();
        while (it.hasNext()) {
            List list = (List) it.next();
            if (list != null) {
                result.addAll(list);
            }
        }
        return Collections.enumeration(result);
    }

    public Enumeration getMatchingHeaders(String[] names) {
        // todo implement
        throw new UnsupportedOperationException();
    }

    public Enumeration getNonMatchingHeaders(String[] names) {
        Set exclude = new HashSet(names.length);
        for (int i = 0; i < names.length; i++) {
            String name = names[i];
            exclude.add(name.toLowerCase());
        }
        List result = new ArrayList(headers.size());
        for (Iterator i = headers.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            if (!exclude.contains(((String)entry.getKey()).toLowerCase())) {
                result.addAll((List)entry.getValue());
            }
        }
        return Collections.enumeration(result);
    }

    public void addHeaderLine(String line) {
        // todo implement
        throw new UnsupportedOperationException();
    }

    public Enumeration getAllHeaderLines() {
        // todo implement
        throw new UnsupportedOperationException();
    }

    public Enumeration getMatchingHeaderLines(String[] names) {
        // todo implement
        throw new UnsupportedOperationException();
    }

    public Enumeration getNonMatchingHeaderLines(String[] names) {
        // todo implement
        throw new UnsupportedOperationException();
    }


    /**
     * Return a header as a list of InternetAddresses
     *
     * @param name   the header to get
     * @param strict whether the header should be strictly parser; see {@link InternetAddress#parseHeader(java.util.List, String, boolean, boolean)}
     * @return
     */
    InternetAddress[] getHeaderAsAddresses(String name, boolean strict) throws MessagingException {
        List addrs = new ArrayList();
        List headers = getHeaderList(name);
        if (headers == null) {
            return null;
        }
        for (Iterator i = headers.iterator(); i.hasNext();) {
            InternetHeader header = (InternetHeader) i.next();
            InternetAddress.parseHeader(addrs, header.getValue(), strict, true);
        }
        return (InternetAddress[]) addrs.toArray(new InternetAddress[addrs.size()]);
    }

    void setHeader(String name, Address[] addresses) {
        List list = new ArrayList(addresses.length);
        for (int i = 0; i < addresses.length; i++) {
            Address address = addresses[i];
            list.add(address.toString());
        }
        headers.put(name, list);
    }

    private List getHeaderList(String name) {
        return (List) headers.get(name.toLowerCase());
    }

    private void setHeaderList(String name, List list) {
        headers.put(name.toLowerCase(), list);
    }

    void writeTo(OutputStream out, String[] ignore) throws IOException {
        Map map = new LinkedHashMap(headers);
        if (ignore != null) {
            map.keySet().removeAll(Arrays.asList(ignore));
        }
        for (Iterator i = map.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            String name = (String) entry.getKey();
            List headers = (List) entry.getValue();
            if (headers != null) {
                for (int j = 0; j < headers.size(); j++) {
                    InternetHeader header = (InternetHeader) headers.get(j);
                    out.write(name.getBytes());
                    out.write(':');
                    out.write(header.getValue().getBytes());
                    out.write(13);
                    out.write(10);
                }
            }
        }
    }

    private static class InternetHeader extends Header {
        public InternetHeader(String name, String value) {
            super(name, value);
        }

        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof InternetHeader == false) return false;
            final InternetHeader other = (InternetHeader) obj;
            return getName().equalsIgnoreCase(other.getName());
        }

        public int hashCode() {
            return getName().toLowerCase().hashCode();
        }
    }
}
