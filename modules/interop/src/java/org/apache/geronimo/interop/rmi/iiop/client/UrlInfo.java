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

    // private data

    private int _protocol;

    private String _host;

    private int _port;

    private String _objectKey;

    private PropertyMap _properties;

    // internal methods

    protected void init(String urlString) {
        int cssPos = urlString.indexOf("://");
        if (cssPos == -1) {
            throw new IllegalArgumentException(urlString);
        }
        _protocol = Protocol.getNumber(urlString.substring(0, cssPos));
        try {
            URL url = new URL("http" + urlString.substring(cssPos));
            _host = url.getHost();
            _port = url.getPort();
            if (_port == -1) {
                switch (_protocol) {
                    case Protocol.HTTP:
                        _port = 80; // see http://www.iana.org/assignments/port-numbers
                        break;
                    case Protocol.HTTPS:
                        _port = 443; // see http://www.iana.org/assignments/port-numbers
                        break;
                    case Protocol.IIOP:
                        _port = 683; // see http://www.iana.org/assignments/port-numbers
                        break;
                    case Protocol.IIOPS:
                        _port = 684; // see http://www.iana.org/assignments/port-numbers
                        break;
                    default:
                        throw new IllegalStateException("url = " + urlString);
                }
            }
            _objectKey = url.getFile();
            if (_objectKey == null) {
                _objectKey = "";
            }
            int queryPos = _objectKey.indexOf('?');
            if (queryPos != -1) {
                _objectKey = _objectKey.substring(0, queryPos);
            }
            _objectKey = StringUtil.removePrefix(_objectKey, "/");
            if (_objectKey.length() == 0) {
                _objectKey = "NameService";
            }
            String query = url.getQuery();
            if (query == null) {
                query = "";
            }
            String props = StringUtil.removePrefix(query, "?").replace('&', ',');
            _properties = new NamedValueList(props).getProperties();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    // public methods

    public int getProtocol() {
        return _protocol;
    }

    public String getHost() {
        return _host;
    }

    public int getPort() {
        return _port;
    }

    public String getObjectKey() {
        return _objectKey;
    }

    public PropertyMap getProperties() {
        return _properties;
    }

    public String toString() {
        return Protocol.getName(_protocol) + "://" + _host + ":" + _port + "/" + _objectKey;
    }
}
