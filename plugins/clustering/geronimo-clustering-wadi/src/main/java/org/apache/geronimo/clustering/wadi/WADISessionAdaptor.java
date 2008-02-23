/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.clustering.wadi;

import java.util.Map;

import org.apache.geronimo.clustering.Session;

/**
 *
 * @version $Rev$ $Date$
 */
public class WADISessionAdaptor implements Session {
    private static final String ADAPTOR_KEY = "ADAPTOR_KEY";
    private final org.codehaus.wadi.core.session.Session session;
    
    public static WADISessionAdaptor retrieveAdaptor(org.codehaus.wadi.core.session.Session session) {
        WADISessionAdaptor adaptor = (WADISessionAdaptor) session.getLocalStateMap().get(ADAPTOR_KEY);
        if (null == adaptor) {
            throw new IllegalStateException("No registered adaptor");
        }
        return adaptor;
    }

    public WADISessionAdaptor(org.codehaus.wadi.core.session.Session session) {
        if (null == session) {
            throw new IllegalArgumentException("session is required");
        }
        this.session = session;

        session.getLocalStateMap().put(ADAPTOR_KEY, this);
    }

    public String getSessionId() {
        return session.getName();
    }

    public void release() {
        try {
            session.destroy();
        } catch (Exception e) {
            throw new IllegalStateException("Cannot release session " + session, e);
        }
    }

    public Object addState(String key, Object value) {
        return session.addState(key, value);
    }

    public Object getState(String key) {
        return session.getState(key);
     }

    public Object removeState(String key) {
        return session.removeState(key);
    }

    public Map getState() {
        return session.getState();
    }
    
    public void onEndAccess() {
        session.onEndProcessing();
    }

}
