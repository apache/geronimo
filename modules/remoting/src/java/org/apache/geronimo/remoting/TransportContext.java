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

package org.apache.geronimo.remoting;

import java.io.IOException;

/**
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:03 $
 */
abstract public class TransportContext {

    private static final ThreadLocal context = new  ThreadLocal();
    
    public static final TransportContext getTransportContext() {
        return (TransportContext) context.get();
    }

    public static final void setTransportContext(TransportContext c) {
        context.set(c);
    }
    
    public abstract Object writeReplace(Object proxy) throws IOException;

    /**
     * @param obj
     * @return
     */
    public abstract Object readReplace(Object obj) throws IOException;
}
