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

package org.apache.geronimo.common.jmx;

import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.RuntimeErrorException;
import javax.management.RuntimeMBeanException;
import javax.management.RuntimeOperationsException;

/**
 * A simple helper to rethrow and/or decode those pesky 
 * JMX exceptions.
 *      
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:57:02 $
 */
public class JMXExceptionDecoder
{
    /**
     * Attempt to decode the given Throwable.  If it
     * is a container JMX exception, then the target
     * is returned.  Otherwise the argument is returned.
     */
    public static Throwable decode(final Throwable t)
    {
        if (t instanceof MBeanException) {
            return ((MBeanException)t).getTargetException();
        }
        if (t instanceof ReflectionException) {
            return ((ReflectionException)t).getTargetException();
        }
        if (t instanceof RuntimeOperationsException) {
            return ((RuntimeOperationsException)t).getTargetException();
        }
        if (t instanceof RuntimeMBeanException) {
            return ((RuntimeMBeanException)t).getTargetException();
        }
        if (t instanceof RuntimeErrorException) {
            return ((RuntimeErrorException)t).getTargetError();
        }
        
        // can't decode
        return t;
    }
    
    /**
     * Decode and rethrow the given Throwable.  If it
     * is a container JMX exception, then the target
     * is thrown.  Otherwise the argument is thrown.
     */
    public static void rethrow(final Exception e)
        throws Exception
    {
        if (e instanceof MBeanException) {
            throw ((MBeanException)e).getTargetException();
        }
        if (e instanceof ReflectionException) {
            throw ((ReflectionException)e).getTargetException();
        }
        if (e instanceof RuntimeOperationsException) {
            throw ((RuntimeOperationsException)e).getTargetException();
        }
        if (e instanceof RuntimeMBeanException) {
            throw ((RuntimeMBeanException)e).getTargetException();
        }
        if (e instanceof RuntimeErrorException) {
            throw ((RuntimeErrorException)e).getTargetError();
        }
        
        // can't decode
        throw e;
    }
}
