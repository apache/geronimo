/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package org.apache.geronimo.common;

import java.io.Serializable;

/**
 * Simulates a stop watch with a <em>lap</em> counter.
 *
 * @version $Revision: 1.4 $ $Date: 2004/02/25 09:57:02 $
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
        return (count == 0) ? 0 : total / getLapCount();
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
