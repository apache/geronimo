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
package javax.mail.internet;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Session;
/**
 * @version $Revision: 1.1 $ $Date: 2003/08/16 01:55:48 $
 */
public class InternetAddress extends Address implements Cloneable {
    private static final InternetAddress[] IA_ARRAY = new InternetAddress[0];
    public static InternetAddress getLocalAddress(Session session) {
        try {
            Properties properties = session.getProperties();
            String mailFrom = properties.getProperty("mail.from");
            if (mailFrom == null) {
                String mailUser = properties.getProperty("mail.user");
                String mailHost = properties.getProperty("mail.host");
                if (mailUser == null) {
                    mailUser = System.getProperty("user.name");
                }
                if (mailHost == null) {
                    try {
                        mailHost = InetAddress.getLocalHost().getHostName();
                    } catch (SecurityException e) {
                        mailHost = "localhost";
                    }
                }
                return new InternetAddress(mailUser + "@" + mailHost);
            } else {
                return new InternetAddress(mailFrom);
            }
        } catch (Exception e) {
            return null;
        }
    }
    public static InternetAddress[] parse(String address)
        throws AddressException {
        return parse(address, true);
    }
    public static InternetAddress[] parse(String addresses, boolean strict)
        throws AddressException {
        // split up address into component parts
        // addresses are name <addr> or addr (name), sometimes with name in quotes
        // comma separated
        List result = new LinkedList();
        StringTokenizer tokenizer = new StringTokenizer(addresses, ",");
        while (tokenizer.hasMoreTokens()) {
            String address = tokenizer.nextToken();
            result.add(new InternetAddress(address));
        }
        return (InternetAddress[]) result.toArray(IA_ARRAY);
    }
    public static InternetAddress[] parseHeader(
        String addresses,
        boolean strict)
        throws AddressException {
        return parse(addresses, strict);
    }
    public static String toString(Address[] addresses) {
        StringBuffer result = new StringBuffer();
        boolean first = true;
        for (int a = 0; a < addresses.length; a++) {
            Address address = addresses[a];
            if (first) {
                first = false;
            } else {
                result.append(",");
            }
            result.append(address.toString());
        }
        return result.toString();
    }
    public static String toString(Address[] addresses, int used) {
        String result = toString(addresses);
        if (result.length() > used) {
            // TODO No idea what to do here
        }
        return result;
    }
    protected String address;
    protected String encodedPersonal;
    protected String personal;
    public InternetAddress() {
    }
    public InternetAddress(String address) throws AddressException {
        int lt = address.indexOf("<");
        int gt = address.indexOf(">");
        if (lt != -1 && gt != -1) {
            setAddress(address.substring(lt + 1, gt));
            // everyting else is the name
            try {
                setPersonal(
                    (address.substring(0, lt) + address.substring(gt + 1))
                        .trim());
            } catch (UnsupportedEncodingException e) {
                throw new AddressException(e.getMessage());
            }
        } else {
            setAddress(address);
        }
    }
    public InternetAddress(String address, boolean strict)
        throws AddressException {
        // parse address strictly?
    }
    public InternetAddress(String address, String personal)
        throws UnsupportedEncodingException {
        this(address, personal, MimeUtility.getDefaultJavaCharset());
    }
    public InternetAddress(String address, String personal, String charset)
        throws UnsupportedEncodingException {
        this.address = address;
        this.personal = personal;
        // TODO Encode personal with given charset
    }
    public Object clone() {
        InternetAddress ia = new InternetAddress();
        ia.address = address;
        ia.personal = personal;
        ia.encodedPersonal = encodedPersonal;
        return ia;
    }
    public boolean equals(Object other) {
        return super.equals(other)
            && ((InternetAddress) other).personal.equals(personal)
            && ((InternetAddress) other).address.equals(address);
    }
    public String getAddress() {
        return address;
    }
    public InternetAddress[] getGroup(boolean strict) throws AddressException {
        // TODO Not implemented
        return null;
    }
    public String getPersonal() {
        return personal;
    }
    public String getType() {
        return "rfc822";
    }
    public int hashCode() {
        // hash of the name and address
        return personal.hashCode() + address.hashCode();
    }
    public boolean isGroup() {
        // TODO Not implemented
        return false;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setPersonal(String name) throws UnsupportedEncodingException {
        setPersonal(name, null);
    }
    public void setPersonal(String name, String encoding)
        throws UnsupportedEncodingException {
        encodedPersonal =
            MimeUtility.encodeText(
                name,
                MimeUtility.getDefaultJavaCharset(),
                encoding);
        personal = name;
    }
    public String toString() {
        return encodedPersonal + " <" + address + ">";
    }
    public String toUnicodeString() {
        return personal + " <" + address + ">";
    }
    public void validate() throws AddressException {
        // TODO Not implemented
    }
}
