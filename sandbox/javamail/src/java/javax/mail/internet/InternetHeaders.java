/**
 *
 * Copyright 2004 The Apache Software Foundation
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.mail.Header;
import javax.mail.MessagingException;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:09 $
 */
public class InternetHeaders {
    private static final String[] STRING_ARRAY = new String[0];
    /**
     * Parse the line into a <code>Header</code?
     * @param line in the form <code>To: Apache Geronimo Users &lt;geronimo-user@apache.org&gt;</code>
     * @return the parsed <code>Header</code>
     */
    private static Header parse(String line) {
        // Could use HeaderTokenizer here, but not really much point
        int colon = line.indexOf(":");
        String name = line.substring(0, colon).trim();
        String value = line.substring(colon + 1).trim();
        return new Header(name, value);
    }
    /**
     * Stores the list of Headers, indexed by name (uppercased)
     */
    private Map _headers = new HashMap();
    /**
     * The last <code>Header</code> we added
     */
    private Header _last;
    /**
     * Stores the list of lines, in the order they were entered.
     */
    private List _lines = new LinkedList();
    public InternetHeaders() {
    }
    public InternetHeaders(InputStream in) throws MessagingException {
        load(in);
    }
    /**
     * Add the given header and original line into the store.
     * A maximum of one of the arguments can be null; if it is, it
     * is dynamically created from the other. If both are <code>null</code>,
     * then a <code>NullPointerException</code> will be raised.
     * @param header the pre-parsed header
     * @param line the header line
     */
    private void add(Header header, String line) {
        if (line == null) {
            line = header.getName() + ": " + header.getValue();
        } else if (header == null) {
            header = parse(line);
        }
        String NAME = header.getName().toUpperCase();
        List list = (List) _headers.get(NAME);
        if (list == null) {
            list = new LinkedList();
            _headers.put(NAME, list);
        }
        list.add(header);
        _lines.add(line);
        _last = header;
    }
    public void addHeader(String name, String value) {
        add(new Header(name, value), null);
    }
    public void addHeaderLine(String line) {
        add(null, line);
    }
    public Enumeration getAllHeaderLines() {
        return Collections.enumeration(_lines);
    }
    public Enumeration getAllHeaders() {
        List result = new LinkedList();
        Iterator it = _headers.values().iterator();
        while (it.hasNext()) {
            List list = (List) it.next();
            result.addAll(list);
        }
        return Collections.enumeration(result);
    }
    public String[] getHeader(String name) {
        List headers = getHeaderList(name);
        if (headers == null) {
            return null;
        } else {
            List result = new LinkedList();
            Iterator it = headers.iterator();
            while (it.hasNext()) {
                Header element = (Header) it.next();
                result.add(element.getValue());
            }
            return (String[]) result.toArray(STRING_ARRAY);
        }
    }
    public String getHeader(String name, String delimiter) {
        List list = getHeaderList(name);
        if (list == null || list.isEmpty()) {
            return null;
        } else {
            Iterator it = list.iterator();
            StringBuffer result = new StringBuffer();
            boolean first = true;
            while (it.hasNext()) {
                if (first) {
                    first = false;
                } else {
                    if (delimiter == null) {
                        break;
                    } else {
                        result.append(delimiter);
                    }
                }
                Header header = (Header) it.next();
                result.append(header.getValue());
            }
            return result.toString();
        }
    }
    private List getHeaderList(String name) {
        return (List) _headers.get(name.toUpperCase());
    }
    private Enumeration getHeaders(String[] names, boolean match) {
        List matches = new ArrayList(names.length);
        for (int i = 0; i < names.length; i++) {
            matches.add(names[i].toUpperCase());
        }
        List result = new LinkedList();
        Iterator entries = _headers.entrySet().iterator();
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            if (matches.contains(entry.getKey()) == match) {
                result.addAll((Collection) entry.getValue());
            }
        }
        return Collections.enumeration(result);
    }
    private Enumeration getLines(String[] names, boolean match) {
        List matches = new ArrayList(names.length);
        for (int i = 0; i < names.length; i++) {
            matches.add(names[i].toUpperCase());
        }
        Iterator it = _lines.iterator();
        List result = new LinkedList();
        while (it.hasNext()) {
            String line = (String) it.next();
            int colon = line.indexOf(":");
            String name = line.substring(0, colon).toUpperCase();
            if (matches.contains(name) == match) {
                result.add(line);
            }

        }
        return Collections.enumeration(result);
    }
    public Enumeration getMatchingHeaderLines(String[] names) {
        return getLines(names, true);
    }
    public Enumeration getMatchingHeaders(String[] names) {
        return getHeaders(names, true);
    }
    public Enumeration getNonMatchingHeaderLines(String[] names) {
        return getLines(names, false);
    }
    public Enumeration getNonMatchingHeaders(String[] names) {
        return getHeaders(names, false);
    }
    public void load(InputStream in) throws MessagingException {
        try {
            StringBuffer buffer = new StringBuffer();
            boolean cr = false;
            boolean lf = false;
            boolean flush = true;
            String line = null;
            while (true) {
                int c = in.read();
                boolean start = buffer.length() == 0;
                boolean white = c == '\t' || c == ' ';
                if (start) {
                    if (line != null) {
                        if (white) {
                            // skip any further whitespace
                            while (c == '\t' || c == ' ')
                                c = in.read();
                            buffer.append(line);
                            buffer.append(' ');
                            // and replace with single whitespace char
                        } else {
                            addHeaderLine(line);
                            line = null;
                        }
                    }
                }
                if (c == '\r') {
                    cr = true;
                } else if (c == '\n') {
                    lf = true;
                } else {
                    buffer.append((char) c);
                    // if we've only got one of them,
                    // followed by a non\r\n character,
                    // pretend we've seen both
                    if (cr || lf) {
                        cr = lf = true;
                    }
                }
                if (cr && lf) {
                    line = buffer.toString().trim();
                    if (line.length() == 0) {
                        break;
                    }
                    buffer = new StringBuffer();
                    cr = lf = false; // reset for next line
                }

            }
        } catch (IOException e) {
            throw new MessagingException(e.getMessage());
        }
    }
    public void removeHeader(String name) {
        String NAME = name.toUpperCase();
        _headers.remove(NAME);
        NAME = NAME + ":";
        // go through list as well
        Iterator it = _lines.iterator();
        while (it.hasNext()) {
            String line = (String) it.next();
            if (line.toUpperCase().startsWith(NAME)) {
                it.remove();
            }
        }
    }
    public void setHeader(String name, String value) {
        removeHeader(name);
        addHeader(name, value);
    }
}
