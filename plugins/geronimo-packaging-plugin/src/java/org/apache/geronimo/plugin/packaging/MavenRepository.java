/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.plugin.packaging;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.repository.Repository;

/**
 * Implementation of Repository that maps to the local Maven repository.
 *
 * @version $Rev$ $Date$
 */
public class MavenRepository implements Repository {
    private final URI root;

    public MavenRepository(File root) {
        this.root = root.toURI();
    }

    public boolean hasURI(URI uri) {
        uri = root.resolve(uri);
        if ("file".equals(uri.getScheme())) {
            return new File(uri).canRead();
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
        uri = root.resolve(uri);
        return uri.toURL();
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = new GBeanInfoBuilder(MavenRepository.class);
        builder.addInterface(Repository.class);
        builder.addAttribute("root", File.class, true);
        builder.setConstructor(new String[]{"root"});
        GBEAN_INFO = builder.getBeanInfo();
    }
}
