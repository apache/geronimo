/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.webservices.saaj;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class SAAJUniverse {
        
    private static final Log LOG = LogFactory.getLog(SAAJUniverse.class);
    
    enum Type { DEFAULT, AXIS1, AXIS2, SUN }
    
    public static final Type DEFAULT = Type.DEFAULT;
    public static final Type SUN = Type.SUN;
    public static final Type AXIS1 = Type.AXIS1;
    public static final Type AXIS2 = Type.AXIS2;

    private Type oldUniverse;
    private boolean universeSet = false;
    
    private static ThreadLocal<Type> currentUniverse = new InheritableThreadLocal<Type>();
    
    public void set(Type newUniverse) {
        if (this.universeSet) {
            throw new RuntimeException("Universe must be unset first");
        } else {
            this.oldUniverse = getCurrentUniverse();
            setCurrentUniverse(newUniverse);   
            this.universeSet = true;            
            LOG.debug("Set universe: " + this + " " + newUniverse);
        }
    }
    
    public void unset() {
        if (this.universeSet) {
            setCurrentUniverse(this.oldUniverse);
            this.universeSet = false;
            LOG.debug("Restored universe: " + this);
        } else {
            LOG.warn("Universe was not set: " + this);
        }
    }
    
    static Type getCurrentUniverse() {
        return currentUniverse.get();
    }
    
    static void setCurrentUniverse(Type universe) {
        currentUniverse.set(universe);
    }
   
}
