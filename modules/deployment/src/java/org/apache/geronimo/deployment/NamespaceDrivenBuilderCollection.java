/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.apache.geronimo.deployment;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Collection;
import java.util.List;

import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.common.DeploymentException;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev:$ $Date:$
 */
public class NamespaceDrivenBuilderCollection {

    private final Collection builders;
    private final Set namespaces = new HashSet();

    public NamespaceDrivenBuilderCollection(Collection builders) {
        this.builders = builders;
        if (builders instanceof ReferenceCollection) {
            ((ReferenceCollection)builders).addReferenceCollectionListener(new ReferenceCollectionListener() {

                public void memberAdded(ReferenceCollectionEvent event) {
                    addBuilder(event.getMember());
                }

                public void memberRemoved(ReferenceCollectionEvent event) {
                    Object builder = event.getMember();
                    String namespace = ((NamespaceDrivenBuilder)builder).getNamespace();
                    namespaces.remove(namespace);
                }
            });
        }
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            Object builder = iterator.next();
            addBuilder(builder);
        }
    }

    private void addBuilder(Object builder) {
        String namespace = ((NamespaceDrivenBuilder)builder).getNamespace();
        if (namespaces.contains(namespace)) {
            throw new IllegalArgumentException("Duplicate namespace in builder set: " + namespace);
        }
        namespaces.add(namespace);
    }

    public void build(XmlObject container, DeploymentContext applicationContext, DeploymentContext moduleContext) throws DeploymentException {
        for (Iterator iterator = builders.iterator(); iterator.hasNext();) {
            NamespaceDrivenBuilder builder = (NamespaceDrivenBuilder) iterator.next();
            builder.build(container, applicationContext, moduleContext);
        }
    }

    public void addLoaders(List loaderList) {
    }
}
