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

package org.apache.geronimo.naming.reference;

import javax.naming.NamingException;

import org.apache.xbean.naming.reference.SimpleReference;
import org.osgi.framework.Bundle;

/**
 * @version $Rev$ $Date$
 */
public class BundleReference extends SimpleReference implements BundleAwareReference {

    protected transient Bundle bundle;

    public Object getContent() throws NamingException {
        return bundle;
    }
    
    @Override
    public String getClassName() {
        return Bundle.class.getName();
    }
    
    public void setBundle(Bundle bundle) {
        this.bundle = bundle;
    }
}
