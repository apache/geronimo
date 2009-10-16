/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.mavenplugins.car;

import org.apache.geronimo.gbean.GAttributeInfo;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GReferenceInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.ReferencePatterns;
import org.apache.geronimo.kernel.config.ManageableAttributeStore;
import org.apache.geronimo.kernel.repository.Artifact;
import org.osgi.framework.Bundle;

import java.io.IOException;
import java.util.Collection;

/**
 * ???
 *
 * @version $Rev$ $Date$
 */
public class MavenAttributeStore
    implements ManageableAttributeStore
{
    public MavenAttributeStore() {
    }

    public Collection applyOverrides(Artifact configurationName, Collection<GBeanData> datas, Bundle bundle) {
        return datas;
    }

    public void setValue(Artifact configurationName, AbstractName gbeanName, GAttributeInfo attribute, Object value, Bundle bundle) {
    }

    public void setReferencePatterns(Artifact configurationName, AbstractName gbean, GReferenceInfo reference, ReferencePatterns patterns) {
    }

    public void setShouldLoad(Artifact configurationName, AbstractName gbean, boolean load) {
    }

    public void addGBean(Artifact configurationName, GBeanData gbeanData, Bundle bundle) {
    }

    public void save() throws IOException {
    }

    public static final GBeanInfo GBEAN_INFO;

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

    static {
        GBeanInfoBuilder builder = GBeanInfoBuilder.createStatic(MavenAttributeStore.class);
        builder.addInterface(ManageableAttributeStore.class);
        GBEAN_INFO = builder.getBeanInfo();
    }
}
