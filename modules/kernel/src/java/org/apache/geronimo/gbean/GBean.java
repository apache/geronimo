/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.geronimo.gbean;

import org.apache.geronimo.gbean.WaitingException;


/**
 * An optional interface for a GBean.  When a GBean implements this interface, the implementation
 * will get life-cycle callbacks.
 *
 * @version $Revision: 1.6 $ $Date: 2004/03/10 09:59:00 $
 */
public interface GBean {
    /**
     * Sets the operating context for the GBean.
     * @param context the context object or null
     */
    void setGBeanContext(GBeanContext context);

    /**
     * Starts the GBean.  This informs the GBean that it is about to transition to the running state.
     * @throws org.apache.geronimo.gbean.WaitingException if the target is waiting for an external condition before it can fully start
     * @throws java.lang.Exception if the target failed to start; this will cause a transition to the failed state
     */
    void doStart() throws WaitingException, Exception;

    /**
     * Stops the target.  This informs the GBean that it is about to transition to the stopped state.
     * @throws org.apache.geronimo.gbean.WaitingException if the target is waiting for an external condition before it can fully stop
     */
    void doStop() throws WaitingException, Exception;

    /**
     * Fails the GBean.  This informs the GBean that it is about to transition to the failed state.
     */
    void doFail();
}
