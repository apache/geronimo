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

import org.apache.geronimo.common.CloneableObject;

/**
 * An integer counter class.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/24 20:51:22 $
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
