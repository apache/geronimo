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

package org.apache.geronimo.system.repository;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.system.serverinfo.ServerInfo;

/**
 * @version $Rev$ $Date$
 */
public class ReadOnlyRepository implements Repository, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(ReadOnlyRepository.class);
    private final URI root;
    private final ServerInfo serverInfo;
    private URI rootURI;

    public ReadOnlyRepository(File root) {
        this(root.toURI());
    }

    public ReadOnlyRepository(URI rootURI) {
        this.root = null;
        this.serverInfo = null;
        this.rootURI = rootURI;
    }

    public ReadOnlyRepository(URI root, ServerInfo serverInfo) {
        this.root = root;
        this.serverInfo = serverInfo;
    }

    public boolean hasURI(URI uri) {
        uri = resolve(uri);
        if ("file".equals(uri.getScheme())) {
            File f = new File(uri);
            return f.exists() && f.canRead();
        } else {
            try {
                uri.toURL().openStream().close();
                return true;
            } catch (IOException e) {
                return false;
            }
        }
    }

    public URL getURL(URI uri) throws MalformedURLException {
        return resolve(uri).toURL();
    }

    private URI resolve(final URI uri) {
        String[] bits = uri.toString().split("/");
        StringBuffer buf = new StringBuffer(bits[0]).append('/');
        String type = bits.length >= 4? bits[3]: "jar";
        buf.append(type).append('s').append('/').append(bits[1]).append('-').append(bits[2]).append('.').append(type);
        return rootURI.resolve(buf.toString());
    }
    
    public void doStart() throws Exception {
        if (rootURI == null) {
            rootURI = serverInfo.resolve(root);
        }
        log.debug("Repository root is " + rootURI);
    }

    public void doStop() throws Exception {
    }

    public void doFail() {
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(ReadOnlyRepository.class);

        infoFactory.addAttribute("root", URI.class, true);

        infoFactory.addReference("ServerInfo", ServerInfo.class, "GBean");

        infoFactory.addInterface(Repository.class);

        infoFactory.setConstructor(new String[]{"root", "ServerInfo"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
