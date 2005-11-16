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

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;

/**
 * @version $Rev$ $Date$
 */
public class MavenAttributeStore implements ManageableAttributeStore {
    public MavenAttributeStore() {
    }

    public Collection setAttributes(URI configurationName, Collection datas) {
        return datas;
    }

    public void setValue(String configurationName, ObjectName gbean, GAttributeInfo attribute, Object value) {
    }

    public void setShouldLoad(String configurationName, ObjectName gbean, boolean load) {
    }

    public void save() throws IOException {
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = new GBeanInfoBuilder(MavenAttributeStore.class);
        builder.addInterface(ManageableAttributeStore.class);
        GBEAN_INFO = builder.getBeanInfo();
    }
}
