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

import javax.resource.spi.work.WorkException;

import org.apache.geronimo.connector.work.WorkerContext;

/**
 * Defines the operations that a pool in charge of the execution of Work
 * instances must expose.
 *  
 * @version $Revision: 1.1 $ $Date: 2003/11/16 22:42:20 $
 */
public interface WorkExecutorPool
{

    /**
     * Executes the specified work. The execution policy (synchronous vs. 
     * asynchronous) is implementation specific. 
     * 
     * @param aWork Work to be executed.
     * 
     * @throws WorkException Indicates that the Work instance can not be 
     * executed or that its execution has thrown an exception.
     */
    public void executeWork(WorkerContext aWork)  throws WorkException;
    
    /**
     * Gets the current number of active threads in the pool.
     * 
     * @return Number of active threads in the pool.
     */
    public int getPoolSize();
    
    /**
     * Gets the minimum number of threads to simultaneously execute.
     * 
     * @return Minimum size.
     */
    public int getMinimumPoolSize();
     
    /**
     * Sets the minimum number of threads to simultaneously execute.
     * 
     * @param aSize Minimum size.
     */
    public void setMinimumPoolSize(int aSize);
    
    /**
     * Gets the maximum number of threads to simultaneously execute.
     * 
     * @return Maximim size.
     */
    public int getMaximumPoolSize();
     
    /**
     * Sets the maximum number of threads to simultaneously execute.
     * 
     * @param Maximum size.
     */
    public void setMaximumPoolSize(int aSize);
     
}
