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

import org.apache.geronimo.common.CloneableObject;

/**
 * An integer counter class.
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:02 $
 */
public class Counter
    extends CloneableObject
    implements Serializable, Cloneable
{
    /** The current count */
    private int count;
    
    /**
     * Construct a Counter with a starting value.
     *
     * @param count   Starting value for counter.
     */
    public Counter(final int count) {
       this.count = count;
    }
    
    /**
     * Construct a Counter.
     */
    public Counter() {}
    
    /**
     * Increment the counter. (Optional operation)
     *
     * @return  The incremented value of the counter.
     */
    public int increment() {
        return ++count;
    }
    
    /**
     * Decrement the counter. (Optional operation)
     *
     * @return  The decremented value of the counter.
     */
    public int decrement() {
        return --count;
    }
    
    /**
     * Return the current value of the counter.
     *
     * @return  The current value of the counter.
     */
    public int getCount() {
        return count;
    }
    
    /**
     * Reset the counter to zero. (Optional operation)
     */
    public void reset() {
        this.count = 0;
    }
    
    /**
     * Check if the given object is equal to this.
     *
     * @param obj  Object to test equality with.
     * @return     True if object is equal to this.
     */
    public boolean equals(final Object obj) {
        if (obj == this) return true;
        
        if (obj != null && obj.getClass() == getClass()) {
            return ((Counter)obj).count == count;
        }
        
        return false;
    }
    
    /**
     * Return a string representation of this.
     *
     * @return  A string representation of this.
     */
    public String toString() {
        return String.valueOf(count);
    }


    /////////////////////////////////////////////////////////////////////////
    //                                Wrappers                             //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Base wrapper class for other wrappers.
     */
    private static class Wrapper
        extends Counter
    {
        /** The wrapped counter */
        protected final Counter counter;
        
        public Wrapper(final Counter counter) {
            this.counter = counter;
        }
        
        public int increment() {
            return counter.increment();
        }
        
        public int decrement() {
            return counter.decrement();
        }
        
        public int getCount() {
            return counter.getCount();
        }
        
        public void reset() {
            counter.reset();
        }
        
        public boolean equals(final Object obj) {
            return counter.equals(obj);
        }
        
        public String toString() {
            return counter.toString();
        }
        
        public Object clone() {
            return counter.clone();
        }
    }
    
    /**
     * Return a synchronized counter.
     *
     * @param counter    Counter to synchronize.
     * @return           Synchronized counter.
     */
    public static Counter makeSynchronized(final Counter counter) {
        return new Wrapper(counter) {
            public synchronized int increment() {
                return this.counter.increment();
            }

            public synchronized int decrement() {
                return this.counter.decrement();
            }

            public synchronized int getCount() {
                return this.counter.getCount();
            }

            public synchronized void reset() {
                this.counter.reset();
            }

            public synchronized int hashCode() {
                return this.counter.hashCode();
            }

            public synchronized boolean equals(final Object obj) {
                return this.counter.equals(obj);
            }

            public synchronized String toString() {
                return this.counter.toString();
            }

            public synchronized Object clone() {
                return this.counter.clone();
            }
        };
    }
    
    /**
     * Returns a directional counter.
     *
     * @param counter       Counter to make directional.
     * @param increasing    True to create an increasing only
     *                      or false to create a decreasing only.
     * @return              A directional counter.
     */
    public static Counter makeDirectional(final Counter counter,
                                          final boolean increasing)
    {
        Counter temp;
        if (increasing) {
            temp = new Wrapper(counter) {
                public int decrement() {
                    throw new UnsupportedOperationException();
                }
                
                public void reset() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        else {
            temp = new Wrapper(counter) {
                public int increment() {
                    throw new UnsupportedOperationException();
                }
            };
        }
        
        return temp;
    }
}
