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

package org.apache.geronimo.test.protocol.mockproto;

import java.net.URLConnection;
import java.net.URL;
import java.net.MalformedURLException;
import java.net.URLStreamHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringBufferInputStream;

import java.util.HashMap;
import java.util.Map;

/**
 * URL handler foe the 'mockproto' protocol.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:44 $
 */
public class Handler
    extends URLStreamHandler
{
    static Map urlMap = new HashMap();

    protected URLConnection openConnection(URL u) throws IOException {
        if (urlMap.containsKey(u)) {
            return (URLConnection) urlMap.get(u);
        } else {
            throw new IOException("Don't have the url in my map");
        }
    }
    
    public static MockURLConnection registerURL(String urlspec, String contents)
        throws MalformedURLException
    {
        URL url = new URL(urlspec);
        MockURLConnection c = new MockURLConnection(url, contents);
        urlMap.put(url, c);
        
        return c;
    }
}

