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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

/**
 * @version $Revision: 1.4 $ $Date: 2003/08/19 02:39:02 $
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
