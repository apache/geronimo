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

package org.apache.geronimo.deployment.util;

import java.net.URL;
import java.io.IOException;

/**
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:39 $
 */
public class URLInfo {
    private final URL url;
    private final URLType type;

    public URLInfo(URL url, URLType type) {
        assert url != null : "url was null";
        assert type != null : "type was null";
        this.url = url;
        this.type = type;
    }

    public URLInfo(URL url) throws IOException {
        this(url, URLType.getType(url));
    }

    public URLType getType() {
        return type;
    }

    public URL getUrl() {
        return url;
    }

    public String toString() {
        return url + " (" + type.toString() + ")";
    }

    public boolean equals(Object obj) {
        if (obj instanceof URLInfo) {
            return this.url.equals(((URLInfo) obj).url);
        }
        return false;
    }

    public int hashCode() {
        return url.hashCode();
    }

}
