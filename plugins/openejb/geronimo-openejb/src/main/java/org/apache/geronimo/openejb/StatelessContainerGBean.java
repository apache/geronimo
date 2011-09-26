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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.openejb;

import org.apache.geronimo.gbean.annotation.GBean;
import org.apache.geronimo.gbean.annotation.ParamSpecial;
import org.apache.geronimo.gbean.annotation.ParamReference;
import org.apache.geronimo.gbean.annotation.ParamAttribute;
import org.apache.geronimo.gbean.annotation.SpecialAttributeType;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.openejb.assembler.classic.StatelessSessionContainerInfo;
import org.apache.openejb.util.Duration;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

/**
 * @version $Rev$ $Date$
 */
@GBean
public class StatelessContainerGBean extends EjbContainer {

    /**
     * Specifies the time an invokation should wait for an instance of the pool to become available.
     * 
     * After the timeout is reached, if an instance in the pool cannot be obtained, the method invocation will fail.
     * 
     */
    private long accessTimeout;
    
    /**
     * PostConstruct methods are invoked on all instances in the pool
     * when the bean is undeployed and its pool is closed.  The
     * CloseTimeout specifies the maximum time to wait for the pool to
     *  close and PostConstruct methods to be invoked.
     */
    private int closeTimeout;

    /**
     * Specifies the size of the bean pools for this
     * stateless SessionBean container.
     */

    private int maxSize;
    
    
    /** Specifies the minimum number of bean instances that should be
     * in this stateless SessionBean container  
     */
    private int minSize;
     
     
	/**
	 * Specifies the maximum time that an instance should be allowed to sit idly
	 * in the pool without use before it should be retired and removed.
	 */
    private int idleTimeout;
     

    /**
     * StrictPooling tells the container what to do when the pool
     * reaches it's maximum size and there are incoming requests
     * that need instances.
     * <p/>
     * With strict pooling, requests will have to wait for instances
     * to become available. The pool size will never grow beyond the
     * the set PoolSize value.
     * <p/>
     * Without strict pooling, the container will create temporary
     * instances to meet demand. The instances will last for just one
     * method invocation and then are removed.
     */
    private boolean strictPooling;


    public StatelessContainerGBean(
            @ParamSpecial(type = SpecialAttributeType.abstractName) AbstractName abstractName,
            @ParamReference(name = "OpenEjbSystem") OpenEjbSystem openEjbSystem,
            @ParamAttribute(name = "provider") String provider,
            @ParamAttribute(name = "maxSize") int maxSize,
            @ParamAttribute(name = "minSize") int minSize,
            @ParamAttribute(name = "strictPooling") boolean strictPooling,
            @ParamAttribute(name = "accessTimeout") long accessTimeout,
            @ParamAttribute(name = "closeTimeout") int closeTimeout,
            @ParamAttribute(name = "idleTimeout") int idleTimeout,            
            @ParamAttribute(name = "properties") Properties properties) {
        super(abstractName, StatelessSessionContainerInfo.class, openEjbSystem, provider, "STATELESS", properties);

        setMaxSize(maxSize);
        setMinSize(minSize);
        setStrictPooling(strictPooling);
        setAccessTimeout(accessTimeout);
        setCloseTimeout(closeTimeout);
        setIdleTimeout(idleTimeout);     
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
        set("MaxSize", Integer.toString(maxSize));
    }

    public int getMinSize() {
        return minSize;
    }

    public void setMinSize(int minSize) {
        this.minSize = minSize;
        set("MinSize", Integer.toString(minSize));
    }

    public boolean isStrictPooling() {
        return strictPooling;
    }

    public void setStrictPooling(boolean strictPooling) {
        this.strictPooling = strictPooling;
        set("StrictPooling", Boolean.toString(strictPooling));
    }

    public long getAccessTimeout() {
        return accessTimeout;
    }

    public void setAccessTimeout(long accessTimeout) {
        this.accessTimeout = accessTimeout;
        Duration accessTimeoutDuration = new Duration(accessTimeout, TimeUnit.SECONDS);
        set("AccessTimeout", accessTimeoutDuration.toString());
    }

    public int getCloseTimeout() {
        return closeTimeout;
    }

    public void setCloseTimeout(int closeTimeout) {
        this.closeTimeout = closeTimeout;
        Duration closeTimeoutDuration = new Duration(closeTimeout, TimeUnit.MINUTES);
        set("CloseTimeout", closeTimeoutDuration.toString());
    }

    public int getIdleTimeout() {
        return idleTimeout;
    }    

    public void setIdleTimeout(int idleTimeout) {
        this.idleTimeout=idleTimeout;
        Duration idleTimeoutDuration = new Duration(idleTimeout, TimeUnit.MINUTES);
        set("IdleTimeout", idleTimeoutDuration.toString());
    }

}
