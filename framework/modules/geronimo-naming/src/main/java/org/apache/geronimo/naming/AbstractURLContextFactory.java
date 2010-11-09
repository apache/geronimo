/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.naming;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;

public abstract class AbstractURLContextFactory implements ObjectFactory {
    public String urlScheme;

    public AbstractURLContextFactory(String urlScheme) {
        this.urlScheme = urlScheme + ":";
    }

    @Override
    public Object getObjectInstance(Object o, Name name, Context context, Hashtable<?, ?> hashtable) throws Exception {
        if (o == null) {
            return getContext();
        }
        if (o instanceof String) {
            return getContext().lookup((String) o);
        }
        if (o instanceof String[]) {
            for (String s : (String[]) o) {
                if (s.startsWith(urlScheme)) {
                    return getContext().lookup(s);
                }
            }
        }
        return null;
        //throw new NamingException("Could not locate a way to look up " + o + " in url context for " + urlScheme);
    }

    protected abstract Context getContext() throws NamingException;
}