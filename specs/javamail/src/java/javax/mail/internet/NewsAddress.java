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
import java.util.LinkedList;
import java.util.List;
import java.util.StringTokenizer;

import javax.mail.Address;
/**
 * @version $Rev$ $Date$
 */
public class NewsAddress extends Address {
    private static final String _separator = ",";
    private static final NewsAddress[] NEWSADDRESS_ARRAY = new NewsAddress[0];
    public static NewsAddress[] parse(String addresses) throws AddressException {
        List result = new LinkedList();
        StringTokenizer tokenizer = new StringTokenizer(addresses, ",");
        while (tokenizer.hasMoreTokens()) {
            String address = tokenizer.nextToken();
            result.add(new NewsAddress(address));
        }
        return (NewsAddress[]) result.toArray(NEWSADDRESS_ARRAY);
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
    protected String host;
    protected String newsgroup;
    public NewsAddress() {
    }
    public NewsAddress(String newsgroup) {
        setNewsgroup(newsgroup);
    }
    public NewsAddress(String newsgroup, String host) {
        setNewsgroup(newsgroup);
        setHost(host);
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
    public String getHost() {
        return host;
    }
    public String getNewsgroup() {
        return newsgroup;
    }
    public String getType() {
        return "news";
    }
    public int hashCode() {
        return toString().hashCode();
    }
    public void setHost(String string) {
        host = string;
    }
    public void setNewsgroup(String newsgroup) {
        newsgroup = newsgroup.trim();
        int at;
        if ((at = newsgroup.indexOf("@")) != -1) {
            this.newsgroup = newsgroup.substring(0, at);
            this.host = newsgroup.substring(at + 1);
        } else {
            this.newsgroup = newsgroup;
        }
    }
    public String toString() {
        return newsgroup + (host == null ? "" : "@" + host);
    }
}
