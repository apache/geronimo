/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop.client;

import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.properties.PropertyMap;
import org.apache.geronimo.interop.rmi.iiop.Protocol;
import org.apache.geronimo.interop.util.ListUtil;
import org.apache.geronimo.interop.util.NamedValueList;
import org.apache.geronimo.interop.util.StringUtil;

public class UrlInfo {
    public static UrlInfo getInstance(String url) {
        UrlInfo object = new UrlInfo();
        object.init(url);
        return object;
    }

    public static List getList(String urls) {
        ArrayList list = new ArrayList(3);
        for (Iterator i = ListUtil.getCommaSeparatedList(urls).iterator(); i.hasNext();) {
            String url = (String) i.next();
            list.add(getInstance(url));
        }
        return list;
    }

    private int         protocol;
    private String      host;
    private int         port;
    private String      objectKey;
    private PropertyMap properties;

    protected void init(String urlString) {
        int cssPos = urlString.indexOf("://");
        if (cssPos == -1) {
            throw new IllegalArgumentException(urlString);
        }
        protocol = Protocol.getNumber(urlString.substring(0, cssPos));
        try {
            URL url = new URL("http" + urlString.substring(cssPos));
            host = url.getHost();
            port = url.getPort();
            if (port == -1) {
                switch (protocol) {
                    case Protocol.HTTP:
                        port = 80; // see http://www.iana.org/assignments/port-numbers
                        break;
                    case Protocol.HTTPS:
                        port = 443; // see http://www.iana.org/assignments/port-numbers
                        break;
                    case Protocol.IIOP:
                        port = 683; // see http://www.iana.org/assignments/port-numbers
                        break;
                    case Protocol.IIOPS:
                        port = 684; // see http://www.iana.org/assignments/port-numbers
                        break;
                    default:
                        throw new IllegalStateException("url = " + urlString);
                }
            }
            objectKey = url.getFile();
            if (objectKey == null) {
                objectKey = "";
            }
            int queryPos = objectKey.indexOf('?');
            if (queryPos != -1) {
                objectKey = objectKey.substring(0, queryPos);
            }
            objectKey = StringUtil.removePrefix(objectKey, "/");
            if (objectKey.length() == 0) {
                objectKey = "NameService";
            }
            String query = url.getQuery();
            if (query == null) {
                query = "";
            }
            String props = StringUtil.removePrefix(query, "?").replace('&', ',');
            properties = new NamedValueList(props).getProperties();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public int getProtocol() {
        return protocol;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public PropertyMap getProperties() {
        return properties;
    }

    public String toString() {
        return Protocol.getName(protocol) + "://" + host + ":" + port + "/" + objectKey;
    }
}
