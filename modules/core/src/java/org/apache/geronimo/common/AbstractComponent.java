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
 * @version $Revision: 1.1 $ $Date: 2003/08/10 20:45:02 $
 */
public class AbstractComponent implements Component {
    private State state = State.NOT_CREATED;
    private Container container;
    protected Log log = LogFactory.getLog(getClass());

    public State getState() {
        return state;
    }

    public final Container getContainer() {
        return container;
    }

    public final void setContainer(Container container) {
        if (state != State.NOT_CREATED && state != State.DESTROYED) {
            throw new IllegalStateException("Set container can only be called while in the not-created or destroyed states: state=" + state);
        }
        if (state == State.NOT_CREATED && container == null) {
            throw new IllegalArgumentException("Interceptor has not been created; container must be NOT null.");
        }
        if (state == State.DESTROYED && container != null) {
            throw new IllegalArgumentException("Interceptor has been destroyed; container must be null.");
        }
        this.container = container;
    }

    public void create() throws Exception {
        if (state != State.NOT_CREATED) {
            throw new IllegalStateException("Can not transition to created state from " + state);
        }
        state = State.STOPPED;
    }

    public void start() throws Exception {
        if (state != State.STOPPED) {
            throw new IllegalStateException("Can not transition to started state from " + state);
        }
        state = State.STARTED;
    }

    public void stop() {
        if (state == State.NOT_CREATED || state == State.DESTROYED) {
            throw new IllegalStateException("Can not transition to started state from " + state);
        } else if (state == State.STOPPED) {
            log.warn("Stop called on an already stopped component; no exception will be thrown but this is a programming error.");
        }
        state = State.STOPPED;
    }

    public void destroy() {
        if (state != State.STOPPED) {
            log.warn("Destroy called on an component in the " + state + " state; no exception will be thrown but this is a programming error.");
        }
        state = State.DESTROYED;
    }
}
