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

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Session;

/**
 * A representation of an Internet email address as specified by RFC822 in
 * conjunction with a human-readable personal name that can be encoded as
 * specified by RFC2047.
 * A typical address is "user@host.domain" and personal name "Joe User"
 *
 * @version $Rev$ $Date$
 */
public class InternetAddress extends Address implements Cloneable {
    /**
     * The address in RFC822 format.
     */
    protected String address;

    /**
     * The personal name in RFC2047 format.
     * Subclasses must ensure that this field is updated if the personal field
     * is updated; alternatively, it can be invalidated by setting to null
     * which will cause it to be recomputed.
     */
    protected String encodedPersonal;

    /**
     * The personal name as a Java String.
     * Subclasses must ensure that this field is updated if the encodedPersonal field
     * is updated; alternatively, it can be invalidated by setting to null
     * which will cause it to be recomputed.
     */
    protected String personal;

    public InternetAddress() {
    }

    public InternetAddress(String address) throws AddressException {
        this(address, true);
    }

    public InternetAddress(String address, boolean strict) throws AddressException {
        init(this, address);
        if (strict) {
            validate();
        }
    }

    public InternetAddress(String address, String personal) throws UnsupportedEncodingException {
        this(address, personal, null);
    }

    public InternetAddress(String address, String personal, String charset) throws UnsupportedEncodingException {
        this.address = address;
        setPersonal(personal, charset);
    }

    /**
     * Clone this object.
     *
     * @return a copy of this object as created by Object.clone()
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new Error();
        }
    }

    /**
     * Return the type of this address.
     *
     * @return the type of this address; always "rfc822"
     */
    public String getType() {
        return "rfc822";
    }

    /**
     * Set the address.
     * No validation is performed; validate() can be used to check if it is valid.
     *
     * @param address the address to set
     */
    public void setAddress(String address) {
        this.address = address;
    }

    /**
     * Set the personal name.
     * The name is first checked to see if it can be encoded; if this fails then an
     * UnsupportedEncodingException is thrown and no fields are modified.
     *
     * @param name    the new personal name
     * @param charset the charset to use; see {@link MimeUtility#encodeWord(String, String, String) MimeUtilityencodeWord}
     * @throws UnsupportedEncodingException if the name cannot be encoded
     */
    public void setPersonal(String name, String charset) throws UnsupportedEncodingException {
        encodedPersonal = MimeUtility.encodeWord(name, charset, null);
        personal = name;
    }

    /**
     * Set the personal name.
     * The name is first checked to see if it can be encoded using {@link MimeUtility#encodeWord(String)}; if this fails then an
     * UnsupportedEncodingException is thrown and no fields are modified.
     *
     * @param name the new personal name
     * @throws UnsupportedEncodingException if the name cannot be encoded
     */
    public void setPersonal(String name) throws UnsupportedEncodingException {
        encodedPersonal = MimeUtility.encodeWord(name);
        personal = name;
    }

    /**
     * Return the address.
     *
     * @return the address
     */
    public String getAddress() {
        return address;
    }

    /**
     * Return the personal name.
     * If the personal field is null, then an attempt is made to decode the encodedPersonal
     * field using {@link MimeUtility#decodeWord(String)}; if this is sucessful, then
     * the personal field is updated with that value and returned; if there is a problem
     * decoding the text then the raw value from encodedPersonal is returned.
     *
     * @return the personal name
     */
    public String getPersonal() {
        if (personal == null && encodedPersonal != null) {
            try {
                personal = MimeUtility.decodeWord(encodedPersonal);
            } catch (ParseException e) {
                return encodedPersonal;
            } catch (UnsupportedEncodingException e) {
                return encodedPersonal;
            }
        }
        return personal;
    }

    /**
     * Return the encoded form of the personal name.
     * If the encodedPersonal field is null, then an attempt is made to encode the
     * personal field using {@link MimeUtility#encodeWord(String)}; if this is
     * successful then the encodedPersonal field is updated with that value and returned;
     * if there is a problem encoding the text then null is returned.
     *
     * @return the encoded form of the personal name
     */
    private String getEncodedPersonal() {
        if (encodedPersonal == null && personal != null) {
            try {
                encodedPersonal = MimeUtility.encodeWord(personal);
            } catch (UnsupportedEncodingException e) {
                // as we could not encode this, return null
                return null;
            }
        }
        return encodedPersonal;
    }

    /**
     * Add RFC822 quotes to a String if needed.
     * It is assumed the text passed in has already been converted to US-ASCII.
     *
     * @param buf  a buffer to write into
     * @param text the text to quote
     * @return the supplied buffer
     */
    private StringBuffer quote(StringBuffer buf, String text) {
        boolean noQuotesNeeded = true;
        for (int i = 0; i < text.length() && noQuotesNeeded; i++) {
            noQuotesNeeded = isAtom(text.charAt(i));
        }
        if (noQuotesNeeded) {
            buf.append(text);
        } else {
            buf.append('"');
            for (int i = 0; i < text.length(); i++) {
                char c = text.charAt(i);
                if (c == '"' || c == '\\') {
                    buf.append('\\');
                }
                buf.append(c);
            }
            buf.append('"');
        }
        return buf;
    }

    /**
     * Return a string representation of this address using only US-ASCII characters.
     *
     * @return a string representation of this address
     */
    public String toString() {
        String p = getEncodedPersonal();
        if (p == null) {
            return address;
        } else {
            StringBuffer buf = new StringBuffer(p.length() + 8 + address.length() + 3);
            quote(buf, p).append("< ").append(address).append(">");
            return buf.toString();
        }
    }

    /**
     * Return a string representation of this address using Unicode characters.
     *
     * @return a string representation of this address
     */
    public String toUnicodeString() {
        String p = getPersonal();
        if (p == null) {
            return address;
        } else {
            StringBuffer buf = new StringBuffer(p.length() + 8 + address.length() + 3);
            quote(buf, p).append("< ").append(address).append(">");
            return buf.toString();
        }
    }

    /**
     * Compares two addresses for equality.
     * We define this as true if the other object is an InternetAddress
     * and the two values returned by getAddress() are equal in a
     * case-insensitive comparison.
     *
     * @param o the other object
     * @return true if the addresses are the same
     */
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof InternetAddress)) return false;

        InternetAddress other = (InternetAddress) o;
        String myAddress = getAddress();
        return myAddress == null ? (other.getAddress() == null) : myAddress.equalsIgnoreCase(other.getAddress());
    }

    /**
     * Return the hashCode for this address.
     * We define this to be the hashCode of the address after conversion to lowercase.
     *
     * @return a hashCode for this address
     */
    public int hashCode() {
        return (address == null) ? 0 : address.toLowerCase().hashCode();
    }

    /**
     * Return true is this address is an RFC822 group address in the format
     * <code>phrase ":" [#mailbox] ";"</code>.
     * We check this by seeing stripping the leading phrase (which, for tolerance,
     * we consider optional) and then checking if the first and last characters are
     * ':' and ';' respectively.
     *
     * @return true is this address represents a group
     */
    public boolean isGroup() {
        if (address == null) {
            return false;
        }

        int start = skipSpace(address, 0);
        int index = expectPhrase(address, start);
        if (index > start) {
            start = skipSpace(address, index);
        }

        return address.charAt(start) == ':' && address.charAt(address.length() - 1) == ';';
    }

    /**
     * Return the members of a group address.
     *
     * If strict is true and the address does not contain an initial phrase then an AddressException is thrown.
     * Otherwise the phrase is skipped and the remainder of the address is checked to see if it is a group.
     * If it is, the content and strict flag are passed to parseHeader to extract the list of addresses;
     * if it is not a group then null is returned.
     *
     * @param strict whether strict RFC822 checking should be performed
     * @return an array of InternetAddress objects for the group members, or null if this address is not a group
     * @throws AddressException if there was a problem parsing the header
     */
    public InternetAddress[] getGroup(boolean strict) throws AddressException {
        if (address == null) {
            return null;
        }
        int start = skipSpace(address, 0);
        int index = expectPhrase(address, start);
        if (index == start && strict) {
            throw new AddressException("Missing phrase");
        } else if (index > start) {
            start = skipSpace(address, index);
        }
        if (address.charAt(start) == ':' && address.charAt(address.length() - 1) == ';') {
            return parseHeader(address.substring(1, address.length() - 1), strict);
        } else {
            return null;
        }
    }

    /**
     * Return an InternetAddress representing the current user.
     * <P/>
     * If session is not null, we first look for an address specified in its
     * "mail.from" property; if this is not set, we look at its "mail.user"
     * and "mail.host" properties and if both are not null then an address of
     * the form "${mail.user}@${mail.host}" is created.
     * If this fails to give an address, then an attempt is made to create
     * an address by combining the value of the "user.name" System property
     * with the value returned from InetAddress.getLocalHost().getHostName().
     * Any SecurityException raised accessing the system property or any
     * UnknownHostException raised getting the hostname are ignored.
     * <P/>
     * Finally, an attempt is made to convert the value obtained above to
     * an InternetAddress. If this fails, then null is returned.
     *
     * @param session used to obtain mail properties
     * @return an InternetAddress for the current user, or null if it cannot be determined
     */
    public static InternetAddress getLocalAddress(Session session) {
        String address = null;
        if (session != null) {
            address = session.getProperty("mail.from");
            if (address == null) {
                String user = session.getProperty("mail.user");
                String host = session.getProperty("mail.host");
                if (user != null && host != null) {
                    address = user + '@' + host;
                }
            }
        }
        if (address == null) {
            try {
                String user = System.getProperty("user.name");
                String host = InetAddress.getLocalHost().getHostName();
                if (user != null && host != null) {
                    address = user + '@' + host;
                }
            } catch (UnknownHostException e) {
                // ignore
            } catch (SecurityException e) {
                // ignore
            }
        }
        if (address != null) {
            try {
                return new InternetAddress(address);
            } catch (AddressException e) {
                // ignore
            }
        }
        return null;
    }

    /**
     * Convert the supplied addresses into a single String of comma-separated text as
     * produced by {@link InternetAddress#toString() toString()}.
     * No line-break detection is performed.
     *
     * @param addresses the array of addresses to convert
     * @return a one-line String of comma-separated addresses
     */
    public static String toString(Address[] addresses) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        if (addresses.length == 1) {
            return addresses[0].toString();
        } else {
            StringBuffer buf = new StringBuffer(addresses.length * 32);
            buf.append(addresses[0].toString());
            for (int i = 1; i < addresses.length; i++) {
                buf.append(", ");
                buf.append(addresses[i].toString());
            }
            return buf.toString();
        }
    }

    /**
     * Convert the supplies addresses into a String of comma-separated text,
     * inserting line-breaks between addresses as needed to restrict the line
     * length to 72 characters. Splits will only be introduced between addresses
     * so an address longer than 71 characters will still be placed on a single
     * line.
     *
     * @param addresses the array of addresses to convert
     * @param used      the starting column
     * @return a String of comma-separated addresses with optional line breaks
     */
    public static String toString(Address[] addresses, int used) {
        if (addresses == null || addresses.length == 0) {
            return null;
        }
        if (addresses.length == 1) {
            String s = addresses[0].toString();
            if (used + s.length() > 72) {
                s = "\r\n  " + s;
            }
            return s;
        } else {
            StringBuffer buf = new StringBuffer(addresses.length * 32);
            for (int i = 0; i < addresses.length; i++) {
                String s = addresses[1].toString();
                if (i == 0) {
                    if (used + s.length() + 1 > 72) {
                        buf.append("\r\n  ");
                        used = 2;
                    }
                } else {
                    if (used + s.length() + 1 > 72) {
                        buf.append(",\r\n  ");
                        used = 2;
                    } else {
                        buf.append(", ");
                        used += 2;
                    }
                }
                buf.append(s);
                used += s.length();
            }
            return buf.toString();
        }
    }

    /**
     * Parse addresses out of the string with basic checking.
     *
     * @param addresses the addresses to parse
     * @return an array of InternetAddresses parsed from the string
     * @throws AddressException if addresses checking fails
     */
    public static InternetAddress[] parse(String addresses) throws AddressException {
        return parse(addresses, true, false);
    }

    /**
     * Parse addresses out of the string.
     *
     * @param addresses the addresses to parse
     * @param strict if true perform detailed checking, if false just perform basic checking
     * @return an array of InternetAddresses parsed from the string
     * @throws AddressException if address checking fails
     */
    public static InternetAddress[] parse(String addresses, boolean strict) throws AddressException {
        return parse(addresses, strict, false);
    }

    /**
     * Parse addresses out of the string.
     *
     * @param addresses the addresses to parse
     * @param strict if true perform detailed checking, if false perform little checking
     * @return an array of InternetAddresses parsed from the string
     * @throws AddressException if address checking fails
     */
    public static InternetAddress[] parseHeader(String addresses, boolean strict) throws AddressException {
        return parse(addresses, strict, true);
    }

    /**
     * Parse addresses with increasing degrees of RFC822 compliance checking.
     *
     * @param addresses the string to parse
     * @param strict if true, performs basic address checking
     * @param veryStrict if true performs detailed address checking
     * @return an array of InternetAddresses parsed from the string
     * @throws AddressException if address checking fails
     */
    private static InternetAddress[] parse(String addresses, boolean strict, boolean veryStrict) throws AddressException {
        List addrs = new ArrayList();

        // todo we need to parse the addresses per the RFC822 spec with certain relaxations which are not documented by JavaMail
        // for now, we ignore all flags and simply tokenize based on commas

        StringTokenizer tok = new StringTokenizer(addresses, ",");
        while (tok.hasMoreTokens()) {
            String text = tok.nextToken().trim();
            InternetAddress addr = new InternetAddress();
            init(addr, text);
            if (strict || veryStrict) {
                addr.validate();
            }
            addrs.add(addr);
        }

        return (InternetAddress[]) addrs.toArray(new InternetAddress[addrs.size()]);
    }

    private static void init(InternetAddress addr, String text) {
        addr.address = text;
        addr.personal = null;
        addr.encodedPersonal = null;
    }

    public void validate() throws AddressException {
        // TODO Not implemented
    }

    private int expectPhrase(String s, int index) {
        int start = index;
        index = expectWord(s, index);
        while (index != start) {
            start = skipSpace(s, index);
            index = expectWord(s, start);
        }
        return index;
    }

    private int expectWord(String s, int index) {
        if (index == s.length()) {
            return index;
        }
        char c = s.charAt(index);
        if (c == '"') {
            index++;
            while (index < s.length()) {
                c = s.charAt(index);
                if (c == '"') {
                    index++;
                    break;
                } else if (c == '\\') {
                    if (index != s.length()) {
                        index++;
                    }
                } else if (c == '\r') {
                    return index;
                }
                index++;
            }
        } else {
            while (index < s.length() && isAtom(s.charAt(index))) {
                index++;
            }
        }
        return index;
    }

    private int skipSpace(String s, int index) {
        while (index < s.length()) {
            char c = s.charAt(index);
            if (isSpace(c)) {
                index++;
            } else if (c == '(') {
                index = skipComment(s, index);
            } else {
                return index;
            }
        }
        return index;
    }

    private int skipComment(String s, int index) {
        index++;
        int nest = 1;
        while (index < s.length() && nest > 0) {
            char c = s.charAt(index++);
            if (c == ')') {
                nest -= 1;
            } else if (c == '\\') {
                if (index == s.length()) {
                    return index;
                }
                index++;
            } else if (c == '(') {
                nest += 1;
            } else if (c == '\r') {
                index -= 1;
                return index;
            }
        }
        return index;
    }

    private static final byte[] CHARMAP = {
        0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,  0x06, 0x02, 0x06, 0x02, 0x02, 0x06, 0x02, 0x02,
        0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,  0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02, 0x02,
        0x04, 0x00, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00,  0x01, 0x01, 0x00, 0x00, 0x01, 0x00, 0x01, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x01, 0x00,

        0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x00, 0x01, 0x01, 0x01, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
        0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,  0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x02,
    };

    private static final byte FLG_SPECIAL = 1;
    private static final byte FLG_CONTROL = 2;
    private static final byte FLG_SPACE = 4;

    private static boolean isSpace(char c) {
        if (c > '\u007f') {
            return false;
        } else {
            return (CHARMAP[c] & FLG_SPACE) != 0;
        }
    }

    private static boolean isAtom(char c) {
        if (c > '\u007f') {
            return true;
        } else if (c == ' ') {
            return false;
        } else {
            return (CHARMAP[c] & (FLG_SPECIAL | FLG_CONTROL)) == 0;
        }
    }
}
