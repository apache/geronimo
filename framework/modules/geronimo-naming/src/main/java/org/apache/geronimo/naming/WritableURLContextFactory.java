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

import java.util.Collections;

import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.xbean.naming.context.ContextAccess;
import org.apache.xbean.naming.context.WritableContext;

/**
 * @version $Rev$ $Date$
 */
public class WritableURLContextFactory extends AbstractURLContextFactory {

    private final Context context;

    public WritableURLContextFactory(String urlScheme) throws NamingException {
        super(urlScheme);
        context = new WritableContext(urlScheme + ":", Collections.<String, Object>emptyMap(), ContextAccess.MODIFIABLE, false);
    }

    @Override
    protected Context getContext() throws NamingException {
        return context;
    }
}
