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

package org.apache.geronimo.gbean.jmx;

import org.apache.geronimo.gbean.GBeanContext;
import org.apache.geronimo.kernel.management.State;

/**
 * @version $Revision: 1.7 $ $Date: 2004/06/05 00:37:16 $
 */
public final class GBeanMBeanContext implements GBeanContext {
    /**
     * The GeronimoMBean which owns the target.
     */
    private final GBeanMBean gmbean;

    /**
     * Creates a new context for a target.
     *
     * @param gmbean the Geronimo Mbean the contains the target
     */
    public GBeanMBeanContext(GBeanMBean gmbean) {
        this.gmbean = gmbean;
    }

    /**
     * Gets the state of this component as an int.
     * The int return is required by the JSR77 specification.
     *
     * @return the current state of this component
     */
    public int getState() {
        return gmbean.getState();
    }

    /**
     * Attempts to bring the component into the fully running state. If an Exception occurs while
     * starting the component, the component is automaticaly failed.
     * <p/>
     * There is no guarantee that the Geronimo MBean will be running when the method returns.
     *
     * @throws Exception if a problem occurs while starting the component
     */
    public void start() throws Exception {
        gmbean.attemptFullStart();
    }

    /**
     * Attempt to bring the component into the fully stopped state. If an exception occurs while
     * stopping the component, tthe component is automaticaly failed.
     * <p/>
     * There is no guarantee that the Geronimo MBean will be stopped when the method returns.
     *
     * @throws Exception if a problem occurs while stopping the component
     */
    public void stop() throws Exception {
        final int state = gmbean.getState();
        if (state == State.RUNNING_INDEX || state == State.STARTING_INDEX) {
            gmbean.stop();
        } else if (state == State.STOPPING_INDEX) {
            gmbean.attemptFullStop();
        }
    }

    /**
     * Moves this component to the FAILED state.
     * <p/>
     * The component is guaranteed to be in the failed state when the method returns.
     */
    public void fail() {
        gmbean.fail();
    }
}
