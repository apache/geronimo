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
package org.apache.geronimo.aries;

import org.apache.aries.application.management.AriesApplication;
import org.apache.aries.application.management.AriesApplicationContext.ApplicationState;
import org.apache.aries.application.management.AriesApplicationEvent;

/**
 * @version $Rev:385232 $ $Date$
 */
public class GeronimoApplicationEvent extends AriesApplicationEvent {

    private final AriesApplication application;
    private final ApplicationState state;
    
    public GeronimoApplicationEvent(AriesApplication application, ApplicationState state) {
        this.application = application;
        this.state = state;
    }
    
    @Override
    public AriesApplication getApplication() {
        return application;
    }

    @Override
    public ApplicationState getType() {
        return state;
    }
    
    public String toString() {
        return application.getApplicationMetadata().getApplicationScope() + ": " + state;
    }
          
}
