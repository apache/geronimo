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
import java.util.LinkedList;
import java.util.List;
import javax.mail.Address;
/**
 * @version $Revision: 1.1 $ $Date: 2003/08/16 01:55:48 $
 */
public class NewsAddress extends Address {
    private static final String _separator = ",";
    protected String newsgroup;
    protected String host;
    public NewsAddress() {
    }
    public NewsAddress(String newsgroup) {
        setNewsgroup(newsgroup);
    }
    public void setNewsgroup(String newsgroup) {
        int at;
        if ((at = newsgroup.indexOf("@")) != -1) {
            this.newsgroup = newsgroup.substring(0, at);
            this.host = newsgroup.substring(at + 1);
        } else {
            this.newsgroup = newsgroup;
        }
    }
    public NewsAddress(String newsgroup, String host) {
        setNewsgroup(newsgroup);
        setHost(host);
    }
    public String getType() {
        return "news";
    }
    public String getHost() {
        return host;
    }
    public String getNewsgroup() {
        return newsgroup;
    }
    public void setHost(String string) {
        host = string;
    }
    public boolean equals(Object other) {
        if (other == null || other.getClass() != this.getClass()) {
            return false;
        }
        NewsAddress address = (NewsAddress) other;
        return (
            address.host == host || host != null && host.equals(address.host))
            && (address.newsgroup == newsgroup
                || newsgroup != null
                && newsgroup.equals(address.newsgroup));
    }
    public String toString() {
        return newsgroup + (host == null ? "" : "@" + host);
    }
    public int hashCode() {
        return toString().hashCode();
    }
    public static String toString(Address[] addresses) {
        // build up a comma separated list of addresses
        StringBuffer result = new StringBuffer();
        for (int a = 0; a < addresses.length; a++) {
            result.append(addresses[a].toString());
            if (a > 0) {
                result.append(_separator);
            }
        }
        return result.toString();
    }
    public static NewsAddress[] parse(String address) throws AddressException {
        List result = new LinkedList();
        address = address + _separator;
        int sep;
        int last = 0;
        String na;
        while ((sep = address.indexOf(_separator)) != -1) {
            na = address.substring(last, sep);
            result.add(new NewsAddress(na));
            last = sep + _separator.length();
        }
        return (NewsAddress[]) result.toArray(NEWSADDRESS_ARRAY);
    }
    private static final NewsAddress[] NEWSADDRESS_ARRAY = new NewsAddress[0];
}
