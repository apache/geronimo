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

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.resource.spi.work.WorkException;

import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.connector.work.GeronimoWorkManager;
import org.apache.geronimo.connector.work.WorkerContext;

import EDU.oswego.cs.dl.util.concurrent.Latch;

/**
 * WorkExecutorPool which treats the submitted Work instances synchronously. 
 * More accurately, its execute method blocks until the work is completed.
 *  
 * @version $Revision: 1.1 $ $Date: 2003/11/16 22:42:20 $
 */
public class SyncWorkExecutorPool
    extends AbstractWorkExecutorPool
{

    /**
     * Creates a pool with the specified minimum and maximum sizes.
     * 
     * @param aMinSize Minimum size of the work executor pool.
     * @param aMaxSize Maximum size of the work executor pool.
     */
    public SyncWorkExecutorPool(int aMinSize, int aMaxSize) {
        super(aMinSize, aMaxSize);
    }
        
    public void setGeronimoWorkManager( GeronimoWorkManager wm ) {
        wm.setSyncExecutor(this);
    }

    /**
     * In the case of a synchronous execution, the Work has been executed and
     * one needs to retrieve the WorkException thrown during this execution, if
     * any.
     * 
     * @exception WorkException Not thrown.
     * @exception InterruptedException Indicates that this work execution
     * has been interrupted.
     */
    public void doExecute(WorkerContext aWork)
        throws WorkException, InterruptedException {
        Latch latch = aWork.provideEndLatch();
        super.execute(aWork);
        latch.acquire();
    }
    
    public static GeronimoMBeanInfo getGeronimoMBeanInfo() throws Exception {
        try {
            GeronimoMBeanInfo rc =AbstractWorkExecutorPool.getGeronimoMBeanInfo();
            rc.setTargetClass(SyncWorkExecutorPool.class);
            rc.addEndpoint(new GeronimoMBeanEndpoint("GeronimoWorkManager", GeronimoWorkManager.class, new ObjectName("geronimo.jca:role=WorkManager"), true));
            return rc;
        } catch (MalformedObjectNameException e) {
            throw new RuntimeException("GeronimoMBeanInfo could not be gernerated.", e);
        }
    }
    
}
