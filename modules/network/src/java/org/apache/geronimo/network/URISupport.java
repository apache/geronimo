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
package org.apache.geronimo.network;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;


/**
 * @version $Revision: 1.1 $ $Date: 2004/03/10 02:14:27 $
 */
public final class URISupport {

    static public Properties parseQueryParameters(URI uri) {
        Properties rc = new Properties();
        if (uri.getQuery() == null)
            return rc;
        // TODO : implement me.

        return rc;
    }

    /**
     * @param queryParams
     */
    static public String toQueryString(Properties queryParams) {
        // TODO : implement me.
        return null;
    }

    static public URI setFragment(URI uri, String fragment) throws URISyntaxException {
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), uri.getPath(), uri.getQuery(), fragment);
    }

    static public URI setPath(URI uri, String path) throws URISyntaxException {
        return new URI(uri.getScheme(), uri.getUserInfo(), uri.getHost(), uri.getPort(), path, uri.getQuery(), uri.getFragment());
    }
}
