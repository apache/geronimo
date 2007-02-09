/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.security;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class SSLConnectionManager {
    private static final Set listeners = new HashSet();
    private static long nextId = 0;

    public static void register(SSLConnectionListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public static void unregister(SSLConnectionListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    public synchronized static long allocateId() {
        return nextId++;
    }

    public static void fireOpen(long connectionId) {
        Set copy = null;

        synchronized (listeners) {
            copy = new HashSet(listeners);
        }

        for (Iterator iter = copy.iterator(); iter.hasNext();) {
            ((SSLConnectionListener) iter.next()).open(connectionId);
        }
    }

    public static void fireClose(long connectionId) {
        Set copy = null;

        synchronized (listeners) {
            copy = new HashSet(listeners);
        }

        for (Iterator iter = copy.iterator(); iter.hasNext();) {
            ((SSLConnectionListener) iter.next()).close(connectionId);
        }
    }
}
