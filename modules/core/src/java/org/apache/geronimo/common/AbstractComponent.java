/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */
package org.apache.geronimo.common;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 *
 *
 * @version $Revision: 1.3 $ $Date: 2003/08/13 02:12:40 $
 */
public class AbstractComponent implements Component
{
    private State state= State.STOPPED;
    private long startTime;
    private Container container;
    protected Log log= LogFactory.getLog(getClass());

    public State getState()
    {
        return state;
    }

    /**
     * Set the Component state.
     * @param newState
     * @throws IllegalStateException Thrown if the transition is not supported by the JSR77 lifecycle.
     */
    protected void setState(State newState) throws IllegalStateException
    {
        switch (state.getIndex())
        {
            case State.STOPPED_INDEX :
                {
                    switch (state.getIndex())
                    {
                        case State.STARTING_INDEX :
                            break;
                        case State.STOPPED_INDEX :
                        case State.RUNNING_INDEX :
                        case State.STOPPING_INDEX :
                        case State.FAILED_INDEX :
                            throw new IllegalStateException(
                                "Can not transition to " + newState + " state from " + state);
                    }
                    break;
                }

            case State.STARTING_INDEX :
                {
                    switch (state.getIndex())
                    {
                        case State.RUNNING_INDEX :
                        case State.FAILED_INDEX :
                        case State.STOPPING_INDEX :
                            break;
                        case State.STOPPED_INDEX :
                        case State.STARTING_INDEX :
                            throw new IllegalStateException(
                                "Can not transition to " + newState + " state from " + state);
                    }
                    break;
                }

            case State.RUNNING_INDEX :
                {
                    switch (state.getIndex())
                    {
                        case State.STOPPING_INDEX :
                        case State.FAILED_INDEX :
                            break;
                        case State.STOPPED_INDEX :
                        case State.STARTING_INDEX :
                        case State.RUNNING_INDEX :
                            throw new IllegalStateException(
                                "Can not transition to " + newState + " state from " + state);

                    }
                    break;
                }

            case State.STOPPING_INDEX :
                {
                    switch (state.getIndex())
                    {
                        case State.STOPPED_INDEX :
                        case State.FAILED_INDEX :
                            break;
                        case State.STARTING_INDEX :
                        case State.RUNNING_INDEX :
                        case State.STOPPING_INDEX :
                            throw new IllegalStateException(
                                "Can not transition to " + newState + " state from " + state);
                    }
                    break;
                }

            case State.FAILED_INDEX :
                {
                    switch (state.getIndex())
                    {
                        case State.STARTING_INDEX :
                        case State.STOPPING_INDEX :
                            break;
                        case State.STOPPED_INDEX :
                        case State.RUNNING_INDEX :
                        case State.FAILED_INDEX :
                            throw new IllegalStateException(
                                "Can not transition to " + newState + " state from " + state);
                    }
                    break;
                }
        }
        log.debug("State changed from " + state + " to " + newState);
        if (newState==State.RUNNING)
            startTime= System.currentTimeMillis();
        state= newState;

    }

    public long getStartTime()
    {
        return startTime;
    }

    public final Container getContainer()
    {
        return container;
    }

    public final void setContainer(Container container)
    {
        if (state != State.STOPPED)
        {
            throw new IllegalStateException(
                "Set container can only be called while in the stopped state: state=" + state);
        }
        this.container= container;
    }

    public void start() throws Exception
    {
        try
        {
            setState(State.STARTING);
            doStart();
            setState(State.RUNNING);
        }
        finally
        {
            if (state != State.RUNNING)
                setState(State.FAILED);
        }
    }

    public void startRecursive() throws Exception
    {
        start();
    }

    /**
     * Do the start tasks for the component.  Called in the STARTING state by 
     * the start() and startRecursive() methods to perform the tasks required to 
     * start the component. The default implementation does nothing.
     * @throws Exception
     */
    public void doStart() throws Exception
    {
    }

    public void stop()
    {
        // Do the actual stop tasks
        try
        {
            setState(State.STOPPING);
            doStop();
            setState(State.STOPPED);
        }
        catch (Exception e)
        {
            log.warn("Stop failed", e);
            setState(State.FAILED);
        }
    }

    /**
     * Do the stop tasks for the component.  Called in the STOPPING state by the stop()
     * method to perform the tasks required to stop the component.
     * This implementation does nothing.
     * @throws Exception
     */
    public void doStop() throws Exception
    {
    }

}
