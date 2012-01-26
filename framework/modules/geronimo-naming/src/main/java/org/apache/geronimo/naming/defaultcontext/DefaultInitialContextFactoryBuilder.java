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


package org.apache.geronimo.naming.defaultcontext;

import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.InitialContextFactoryBuilder;
import org.apache.xbean.naming.context.WritableContext;

/**
 * OSGI service that supplies a singleton InitialContextFactoryBuilder for the default / context using a WritableContext.
 *
 * @version $Rev:$ $Date:$
 */
public class DefaultInitialContextFactoryBuilder implements InitialContextFactoryBuilder {
    private final Context initialContext;

    public DefaultInitialContextFactoryBuilder() throws NamingException {
        initialContext = new WritableContext();
    }

    @Override
    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> hashtable) throws NamingException {
        return initialContextFactory;
    }

    private final InitialContextFactory initialContextFactory = new InitialContextFactory() {
        @Override
        public Context getInitialContext(Hashtable<?, ?> hashtable) throws NamingException {
            return initialContext;
        }
    };

}
