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

import java.io.Serializable;

/**
 * Simulates a stop watch with a <em>lap</em> counter.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 09:29:37 $
 */
public class StopWatch
    extends CloneableObject
    implements Serializable
{
    /** Total time */
    protected long total = 0;
    
    /** Start time */
    protected long start = -1;
    
    /** Stop time */
    protected long stop = -1;
    
    /** The <i>lap</i> count */
    protected int count = 0;
    
    /** Is the watch started */
    protected boolean running = false;
    
    /**
     * Default constructor.
     */
    public StopWatch() {}
    
    /**
     * Construct a StopWatch.
     *
     * @param running    Start the watch
     */
    public StopWatch(final boolean running)
    {
        if (running) start();
    }
    
    /**
     * Start the watch.
     *
     * @param reset   True to reset the watch prior to starting.
     */
    public void start(final boolean reset)
    {
        if (!running) {
            if (reset) reset();
            start = System.currentTimeMillis();
            running = true;
        }
    }
    
    /**
     * Start the watch.
     */
    public void start()
    {
        start(false);
    }
    
    /**
     * Stop the watch.
     *
     * @return  Elapsed time or 0 if the watch was never started.
     */
    public long stop()
    {
        long lap = 0;
        
        if (running) {
            count++;
            stop = System.currentTimeMillis();
            lap = stop - start;
            total += lap;
            running = false;
        }
        
        return lap;
    }
    
    /**
     * Reset the watch.
     */
    public void reset()
    {
        start = -1;
        stop = -1;
        total = 0;
        count = 0;
        running = false;
    }
    
    /**
     * Get the <i>lap</i> count.
     *
     * @return  The <i>lap</i> count.
     */
    public int getLapCount()
    {
        return count;
    }
    
    /**
     * Get the elapsed <i>lap</i> time since the watch was started.
     *
     * @return  Elapsed <i>lap</i> time or 0 if the watch was never started
     */
    public long getLapTime()
    {
        if (start == -1) {
            return 0;
        }
        else if (running) {
            return System.currentTimeMillis() - start;
        }
        else {
            return stop - start;
        }
    }
    
    /**
     * Get the average <i>lap</i> time since the watch was started.
     *
     * @return  Average <i>lap</i> time since the watch was started.
     */
    public long getAverageLapTime()
    {
        return (count == 0) ? 0 : getLapTime() / getLapCount();
    }
    
    /**
     * Get the elapsed time since the watch was created or last reset.
     *
     * @return  Elapsed time or 0 if the watch was never started.
     */
    public long getTime()
    {
        if (start == -1) {
            return 0;
        }
        else if (running) {
            return total + System.currentTimeMillis() - start;
        }
        else {
            return total;
        }
    }
    
    /**
     * Check if the watch is running.
     *
     * @return  True if the watch is running.
     */
    public boolean isRunning()
    {
        return running;
    }
    
    /**
     * Convert to a duration value.
     */
    public Duration toDuration()
    {
        return new Duration(getTime());
    }
    
    /**
     * Return a string representation.
     */
    public String toString()
    {
        StringBuffer buff = new StringBuffer();
        
        if (running) {
            // the watch has not been stopped
            formatElapsedTime(buff, getTime());
            
            // add the current lap time too if there is more than one lap
            if (count >= 1) {
                buff.append(", count=").append(count);
                buff.append(", current=");
                formatElapsedTime(buff, getLapTime());
            }
        }
        else {
            // the watch has been stopped
            formatElapsedTime(buff, getTime());
            
            // add extra info if there is more than one lap
            if (count > 1) {
                buff.append(", count=").append(count);
                buff.append(", average=");
                formatElapsedTime(buff, getAverageLapTime());
            }
        }
        
        return buff.toString();
    }
    
    private void formatElapsedTime(final StringBuffer buff, final long lapsed)
    {
        buff.append(new Duration(lapsed));
    }


    /////////////////////////////////////////////////////////////////////////
    //                                Wrappers                             //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Base wrapper class for other wrappers.
     */
    private static class Wrapper
        extends StopWatch
    {
        protected StopWatch watch;
        
        public Wrapper(final StopWatch watch) {
            this.watch = watch;
        }
        
        public void start(final boolean reset) {
            watch.start(reset);
        }
        
        public void start() {
            watch.start();
        }
        
        public long stop() {
            return watch.stop();
        }
        
        public void reset() {
            watch.reset();
        }
        
        public long getLapTime() {
            return watch.getLapTime();
        }
        
        public long getAverageLapTime() {
            return watch.getAverageLapTime();
        }
        
        public int getLapCount() {
            return watch.getLapCount();
        }
        
        public long getTime() {
            return watch.getTime();
        }
        
        public boolean isRunning() {
            return watch.isRunning();
        }
        
        public Duration toDuration()
        {
            return watch.toDuration();
        }
        
        public String toString() {
            return watch.toString();
        }
    }

    /**
     * Return a synchronized stop watch.
     *
     * @param watch    StopWatch to synchronize.
     * @return         Synchronized stop watch.
     */
    public static StopWatch makeSynchronized(final StopWatch watch)
    {
        return new Wrapper(watch) {
            public synchronized void start(final boolean reset) {
                this.watch.start(reset);
            }

            public synchronized void start() {
                this.watch.start();
            }

            public synchronized long stop() {
                return this.watch.stop();
            }

            public synchronized void reset() {
                this.watch.reset();
            }

            public synchronized long getLapTime() {
                return this.watch.getLapTime();
            }

            public synchronized long getAverageLapTime() {
                return this.watch.getAverageLapTime();
            }

            public synchronized int getLapCount() {
                return this.watch.getLapCount();
            }

            public synchronized long getTime() {
                return this.watch.getTime();
            }

            public synchronized boolean isRunning() {
                return this.watch.isRunning();
            }
            
            public synchronized Duration toDuration()
            {
                return this.watch.toDuration();
            }
            
            public synchronized String toString() {
                return this.watch.toString();
            }
        };
    }
}
