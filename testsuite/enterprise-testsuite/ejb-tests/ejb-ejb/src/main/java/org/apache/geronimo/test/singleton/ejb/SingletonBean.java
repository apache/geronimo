/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.test.singleton.ejb;

import javax.annotation.Resource;
import javax.ejb.Singleton;

@Singleton
public class SingletonBean implements SingletonLocal, SingletonRemote {
    /**
     * Updated by the StartupSingletonBean's @PostConstruct
     * If we read this value before lookup of the StartupSingletonBean we can
     * determine if the PostConstruct was called
     */
    private boolean startupInvoked = false;

    public String sayHi(String name) {
        return "Singleton Hello " + name;
    }

    public boolean isStartupInvoked()
    {
    	return startupInvoked;
    }

    public void setStartupInvoked() {
        startupInvoked = true;
    }
}
