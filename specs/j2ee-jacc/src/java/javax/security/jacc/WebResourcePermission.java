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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.security.jacc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Permission;
import java.security.PermissionCollection;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:38 $
 */
public final class WebResourcePermission extends Permission {
    private transient int cachedHashCode = 0;
    private transient URLPatternSpec urlPatternSpec;
    private transient HTTPMethodSpec httpMethodSpec;

    public WebResourcePermission(HttpServletRequest request) {
        super(request.getServletPath());

        urlPatternSpec = new URLPatternSpec(request.getServletPath());
        httpMethodSpec = new HTTPMethodSpec(request.getMethod());
    }

    public WebResourcePermission(String name, String actions) {
        super(name);

        urlPatternSpec = new URLPatternSpec(name);
        httpMethodSpec = new HTTPMethodSpec(actions);
    }

    public WebResourcePermission(String urlPattern, String[] HTTPMethods) {
        super(urlPattern);

        urlPatternSpec = new URLPatternSpec(urlPattern);
        httpMethodSpec = new HTTPMethodSpec(HTTPMethods);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof WebResourcePermission)) return false;

        WebResourcePermission other = (WebResourcePermission)o;
        return urlPatternSpec.equals(other.urlPatternSpec) && httpMethodSpec.equals(other.httpMethodSpec);
    }

    public String getActions() {
        return httpMethodSpec.getActions();
    }

    public int hashCode() {
        if (cachedHashCode == 0) {
            cachedHashCode = urlPatternSpec.hashCode() ^ httpMethodSpec.hashCode();
        }
        return cachedHashCode;
    }

    public boolean implies(Permission permission) {
        if (permission == null || !(permission instanceof WebResourcePermission)) return false;

        WebResourcePermission other = (WebResourcePermission)permission;
        return urlPatternSpec.implies(other.urlPatternSpec) && httpMethodSpec.implies(other.httpMethodSpec);
    }

    public PermissionCollection newPermissionCollection() {
    	return new WebResourcePermissionCollection();
    }

    private synchronized void readObject(ObjectInputStream in) throws IOException {
        urlPatternSpec = new URLPatternSpec(in.readUTF());
        httpMethodSpec = new HTTPMethodSpec(in.readUTF());
    }

    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(urlPatternSpec.getPatternSpec());
        out.writeUTF(httpMethodSpec.getActions());
    }
}

