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

package javax.mail;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:07 $
 */
public class URLName {
    private String file;
    private String host;
    private String password;
    private int port;
    private String protocol;
    private String ref;
    private String username;
    protected String fullURL;
    private int hashCode;

    public URLName(String url) {
        parseString(url);
    }

    protected void parseString(String url) {
        URI uri;
        try {
            if (url == null) {
                uri = null;
            } else {
                uri = new URI(url);
            }
        } catch (URISyntaxException e) {
            uri = null;
        }
        if (uri == null) {
            protocol = null;
            host = null;
            port = -1;
            file = null;
            ref = null;
            username = null;
            password = null;
            return;
        }

        protocol = checkBlank(uri.getScheme());
        host = checkBlank(uri.getHost());
        port = uri.getPort();
        file = checkBlank(uri.getPath());
        ref = checkBlank(uri.getFragment());
        String userInfo = checkBlank(uri.getUserInfo());
        if (userInfo == null) {
            username = null;
            password = null;
        } else {
            int pos = userInfo.indexOf(':');
            if (pos == -1) {
                username = userInfo;
                password = null;
            } else {
                username = userInfo.substring(0, pos);
                password = userInfo.substring(pos + 1);
            }
        }
        updateFullURL();
    }

    public URLName(String protocol, String host, int port, String file, String username, String password) {
        this.protocol = checkBlank(protocol);
        this.host = checkBlank(host);
        this.port = port;
        if (file == null || file.length() == 0) {
            this.file = null;
            ref = null;
        } else {
            int pos = file.indexOf('#');
            if (pos == -1) {
                this.file = file;
                ref = null;
            } else {
                this.file = file.substring(0, pos);
                ref = file.substring(pos + 1);
            }
        }
        this.username = checkBlank(username);
        if (this.username != null) {
            this.password = checkBlank(password);
        } else {
            this.password = null;
        }
        updateFullURL();
    }

    public URLName(URL url) {
        protocol = checkBlank(url.getProtocol());
        host = checkBlank(url.getHost());
        port = url.getPort();
        file = checkBlank(url.getFile());
        ref = checkBlank(url.getRef());
        String userInfo = checkBlank(url.getUserInfo());
        if (userInfo == null) {
            username = null;
            password = null;
        } else {
            int pos = userInfo.indexOf(':');
            if (pos == -1) {
                username = userInfo;
                password = null;
            } else {
                username = userInfo.substring(0, pos);
                password = userInfo.substring(pos + 1);
            }
        }
        updateFullURL();
    }

    private static String checkBlank(String target) {
        if (target == null || target.length() == 0) {
            return null;
        } else {
            return target;
        }
    }

    private void updateFullURL() {
        hashCode = 0;
        StringBuffer buf = new StringBuffer(100);
        if (protocol != null) {
            buf.append(protocol).append(':');
            if (host != null) {
                buf.append("//");
                if (username != null) {
                    buf.append(username);
                    if (password != null) {
                        buf.append(':').append(password);
                    }
                    buf.append('@');
                }
                buf.append(host);
                if (port != -1) {
                    buf.append(':').append(port);
                }
                if (file != null) {
                    buf.append(file);
                }
                hashCode = buf.toString().hashCode();
                if (ref != null) {
                    buf.append('#').append(ref);
                }
            }
        }
        fullURL = buf.toString();
    }

    public boolean equals(Object o) {
        if (o instanceof URLName == false) {
            return false;
        }
        URLName other = (URLName) o;
        // check same protocol - false if either is null
        if (protocol == null || other.protocol == null || !protocol.equals(other.protocol)) {
            return false;
        }

        if (port != other.port) {
            return false;
        }

        // check host - false if not (both null or both equal)
        return areSame(host, other.host) && areSame(file, other.file) && areSame(username, other.username) && areSame(password, other.password);
    }

    private static boolean areSame(String s1, String s2) {
        if (s1 == null) {
            return s2 == null;
        } else {
            return s1.equals(s2);
        }
    }

    public int hashCode() {
        return hashCode;
    }

    public String toString() {
        return fullURL;
    }

    public String getFile() {
        return file;
    }

    public String getHost() {
        return host;
    }

    public String getPassword() {
        return password;
    }

    public int getPort() {
        return port;
    }

    public String getProtocol() {
        return protocol;
    }

    public String getRef() {
        return ref;
    }

    public URL getURL() throws MalformedURLException {
        return new URL(fullURL);
    }

    public String getUsername() {
        return username;
    }
}
