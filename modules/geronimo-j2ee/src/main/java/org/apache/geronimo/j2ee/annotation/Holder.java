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


package org.apache.geronimo.j2ee.annotation;

import java.util.List;
import java.io.Serializable;

/**
 * @version $Rev$ $Date$
 */
public class Holder implements Serializable {

    public static final Holder EMPTY = new Holder() {

        @Override
        public void setInjections(List<Injection> injections) {
        }

        @Override
        public void setPostConstruct(LifecycleMethod postConstruct) {
        }

        @Override
        public void setPreDestroy(LifecycleMethod preDestroy) {
        }
    };
    
    private List<Injection> injections;
    private LifecycleMethod postConstruct;
    private LifecycleMethod preDestroy;


    public Holder() {
    }


    public void setInjections(List<Injection> injections) {
        this.injections = injections;
    }

    public void setPostConstruct(LifecycleMethod postConstruct) {
        this.postConstruct = postConstruct;
    }

    public void setPreDestroy(LifecycleMethod preDestroy) {
        this.preDestroy = preDestroy;
    }

    public List<Injection> getInjections() {
        return injections;
    }

    public LifecycleMethod getPostConstruct() {
        return postConstruct;
    }

    public LifecycleMethod getPreDestroy() {
        return preDestroy;
    }
}
