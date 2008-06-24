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

package org.apache.geronimo.concurrent.naming;

import javax.naming.NamingException;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.xbean.naming.reference.SimpleReference;

/**
 * @version $Rev: 608311 $ $Date: 2008/02/28 21:05:44 $
 */
public class ResourceReference extends SimpleReference {
    private final ModuleAwareResourceSource source;
    private final String type;
    private final AbstractName moduleID;

    public ResourceReference(ModuleAwareResourceSource source, String type, AbstractName moduleID) {
        this.source = source;
        this.type = type;
        this.moduleID = moduleID;
    }

    public Object getContent() throws NamingException {
        try {
            return this.source.$getResource(this.moduleID);
        } catch (Throwable e) {
            throw (NamingException)new NamingException("Could not create resource").initCause(e);
        }
    }

    @Override
    public String getClassName() {
        return this.type;
    }
}
