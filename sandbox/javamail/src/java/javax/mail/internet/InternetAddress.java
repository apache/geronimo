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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;
import javax.mail.Address;
import javax.mail.Session;
/**
 * @version $Revision: 1.3 $ $Date: 2004/03/10 10:00:28 $
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
