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


package org.apache.geronimo.security.deploy;

import java.io.Serializable;
import java.util.Set;
import java.util.HashSet;

/**
 * @version $Rev$ $Date$
 */
public class SubjectInfo implements Serializable {

    private final String realm;
    private final String id;

    public SubjectInfo(String realm, String id) {
        this.realm = realm;
        this.id = id;
    }

    public String getRealm() {
        return realm;
    }

    public String getId() {
        return id;
    }
}
