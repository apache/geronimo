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
package org.apache.geronimo.kernel.service;

import org.apache.geronimo.kernel.service.GeronimoMBeanContext;


/**
 * An optional interface for targets of a GeronimoMBean.  When a target implements this interface, the target
 * will get a regerence to the GeronimoMBeanContext, and will get life-cycle callbacks.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:38:35 $
 */
public interface GeronimoMBeanTarget {
    /**
     * Sets the GeronimoMBeanContext.  This is called before doStart and with a null context after stop.
     *
     * @param context the new context; will be null after stop
     */
    void setMBeanContext(GeronimoMBeanContext context);

    /**
     * Checks if the target is ready to start.  A target can delay the start of the GeronimoBean by returning
     * false from this method.
     *
     * @return true if the target is ready to start; false otherwise
     */
    boolean canStart();

    /**
     * Starts the target.  This method is called by the GeronimoMBean to inform the target that the MBean is about to
     * start.  This is called immediately before moving to the running state.
     */
    void doStart();

    /**
     * Checks if the target is ready to stop.  A target can delay the stopping of the GeronimoBean by returning
     * false from this method.
     *
     * @return true if the target is ready to stop; false otherwise
     */
    boolean canStop();

    /**
     * Stops the target.  This method is called by the GeronimoMBean to inform the target that the MBean is about to
     * stop.  This is called immediately before moving to the stopped state.
     */
    void doStop();

    /**
     * Fails the MBean.  This method is called by the GeronimoMBean to inform the target that the MBean is about to
     * fail.  This is called immediately before moving to the failed state.
     */
    void doFail();
}
