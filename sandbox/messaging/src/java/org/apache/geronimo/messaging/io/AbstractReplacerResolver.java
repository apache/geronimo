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

package org.apache.geronimo.messaging.io;

import java.io.IOException;

/**
 * Base implementation for the ReplacerResolver contracts.
 *
 * @version $Rev$ $Date$
 */
public abstract class AbstractReplacerResolver implements ReplacerResolver {

    private volatile boolean isOffline;
    private volatile ReplacerResolver next;

    public void online() {
        isOffline = false;
    }
    
    public void offline() {
        isOffline = true;
    }
    
    public boolean isOffline() {
        return isOffline;
    }
    
    public ReplacerResolver append(ReplacerResolver anHandler) {
        ReplacerResolver onlineNext = getNext();
        if ( null != onlineNext ) {
            onlineNext.append(anHandler);
        } else {
            next = anHandler;
        }
        return anHandler;
    }

    public ReplacerResolver getNext() {
        ReplacerResolver onlineNext = next;
        while ( null != onlineNext && onlineNext.isOffline() ) {
            onlineNext = onlineNext.getNext();
        }
        if ( next != onlineNext ) {
            next = onlineNext;
        }
        return onlineNext;
    }
    
    public Object replaceObject(Object obj) throws IOException {
        Object replaced = customReplaceObject(obj);
        if ( null != replaced ) {
            return replaced;
        }
        if ( null == next ) {
            return obj;
        }
        replaced =  getNext().replaceObject(obj);
        if ( null != replaced ) {
            return replaced;
        }
        return obj;
    }

    protected abstract Object customReplaceObject(Object obj)
        throws IOException;
    
    public Object resolveObject(Object obj) throws IOException {
        Object resolved = customResolveObject(obj);
        if ( null != resolved ) {
            return resolved;
        }
        if ( null == next ) {
            return obj;
        }
        resolved = getNext().resolveObject(obj);
        if ( null != resolved ) {
            return resolved;
        }
        return obj;
    }

    protected abstract Object customResolveObject(Object obj)
        throws IOException;
    
}
