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

package org.apache.geronimo.connector.work.pool;

import org.apache.geronimo.connector.work.WorkerContext;

import EDU.oswego.cs.dl.util.concurrent.Channel;
import EDU.oswego.cs.dl.util.concurrent.PooledExecutor;

/**
 * PooledExecutor enforcing a timed out "blocked execution policy". The works
 * submitted to this pooled executor MUST be a WorkWrapper.
 *
 * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
 */
public class TimedOutPooledExecutor extends PooledExecutor {

    /**
     * Creates a pooled executor. The Channel used to enqueue the submitted
     * Work instance is a queueless synchronous one.
     */
    public TimedOutPooledExecutor() {
        setBlockedExecutionHandler(new TimedOutSpinHandler());
    }

    /**
     * Creates a pooled executor, which uses the provided Channel as its
     * queueing mechanism.
     *
     * @param aChannel Channel to be used to enqueue the submitted Work
     * intances.
     */
    public TimedOutPooledExecutor(Channel aChannel) {
        super(aChannel);
        setBlockedExecutionHandler(new TimedOutSpinHandler());
    }

    /**
     * Executes the provided task, which MUST be an instance of WorkWrapper.
     *
     * @throws IllegalArgumentException Indicates that the provided task is not
     * a WorkWrapper instance.
     */
    public void execute(Runnable aTask) throws InterruptedException {
        if (!(aTask instanceof WorkerContext)) {
            throw new IllegalArgumentException("Please submit a WorkWrapper.");
        }
        super.execute(aTask);
    }

    /**
     * This class implements a time out policy when a work is blocked: it offers
     * the task to the pool until the work has timed out.
     *
     * @version $Revision: 1.1 $ $Date: 2004/01/23 05:56:11 $
     */
    private class TimedOutSpinHandler
            implements PooledExecutor.BlockedExecutionHandler {

        /* (non-Javadoc)
         * @see EDU.oswego.cs.dl.util.concurrent.PooledExecutor.BlockedExecutionHandler#blockedAction(java.lang.Runnable)
         */
        public boolean blockedAction(Runnable arg0) throws InterruptedException {
            WorkerContext work = (WorkerContext) arg0;
            if (!handOff_.offer(arg0, work.getStartTimeout())) {
                // double check.
                if (work.isTimedOut()) {
                    return false;
                }
                return true;
            }
            return true;
        }
    }
}
