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

package org.apache.geronimo.messaging.reference;

import java.io.IOException;

import org.apache.geronimo.messaging.io.AbstractReplacerResolver;

/**
 * ReplacerResolver for Referenceable and Reference.
 *
 * @version $Revision: 1.1 $ $Date: 2004/05/11 12:06:42 $
 */
public class ReferenceReplacerResolver
    extends AbstractReplacerResolver
{

    /**
     * ReferenceManager to be used to register Referenceables and build proxies.
     */
    private final ReferenceableManager manager;

    /**
     * 
     * @param aManager ReferenceManager to be used to manage the
     * resolved/replaced ReferenceInfo/Reference.
     */
    public ReferenceReplacerResolver(ReferenceableManager aManager) {
        if ( null == aManager ) {
            throw new IllegalArgumentException("Manager is required.");
        }
        manager = aManager;
    }
    
    protected Object customReplaceObject(Object obj) throws IOException {
        if ( obj instanceof Referenceable ) {
            return manager.register((Referenceable) obj);
        } else if ( obj instanceof Reference ) {
            // Reference are not marshalled. They are replaced by their
            // ReferenceableInfo.
            return ((Reference) obj).getReferenceableInfo();
        }
        return null;
    }

    protected Object customResolveObject(Object obj) throws IOException {
        if ( obj instanceof ReferenceableInfo ) {
            ReferenceableInfo info = (ReferenceableInfo) obj;
            return manager.factoryProxy(info);
        }
        return null;
    }
    
}
