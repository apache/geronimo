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
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */
package javax.enterprise.deploy.spi.status;

import javax.enterprise.deploy.spi.TargetModuleID;
import javax.enterprise.deploy.spi.exceptions.OperationUnsupportedException;

/**
 * The ProgressObject interface tracks and reports the progress of the
 * deployment activities: distribute, start, stop, undeploy.
 *
 * This class has an <i>optional</i> cancel method.  The support of the cancel
 * function can be tested by the isCancelSupported method.
 *
 * The ProgressObject structure allows the user the option of polling for
 * status or to provide a callback.
 *
 * @version $Revision: 1.2 $ $Date: 2003/09/04 05:41:21 $
 */
public interface ProgressObject {
    /**
     * Retrieve the status of this activity.
     *
     * @return An object containing the status information.
     */
    public DeploymentStatus getDeploymentStatus();

    /**
     * Retrieve the list of TargetModuleIDs successfully processed or created
     * by the associated DeploymentManager operation.
     *
     * @return a list of TargetModuleIDs.
     */
    public TargetModuleID[] getResultTargetModuleIDs();

    /**
     * Return the ClientConfiguration object associated with the
     * TargetModuleID.
     *
     * @return ClientConfiguration for a given TargetModuleID or <tt>null</tt>
     *         if none exists.
     */
    public ClientConfiguration getClientConfiguration(TargetModuleID id);

    /**
     * Tests whether the vendor supports a cancel operation for this
     * deployment action.
     *
     * @return <tt>true</tt> if this platform allows this action to be
     *         canceled.
     */
    public boolean isCancelSupported();

    /**
     * (optional) A cancel request on an in-process operation stops all further
     * processing of the operation and returns the environment to it original
     * state before the operation was executed.  An operation that has run to
     * completion cannot be cancelled.
     *
     * @throws OperationUnsupportedException occurs when this optional command
     *         is not supported by this implementation.
     */
    public void cancel() throws OperationUnsupportedException;

    /**
     * Tests whether the vendor supports a stop operation for the deployment
     * action.
     *
     * @return <tt>true</tt> if this platform allows this action to be
     *         stopped.
     */
    public boolean isStopSupported();

    /**
     * (optional) A stop request on an in-process operation allows the
     * operation on the current TargetModuleID to run to completion but does
     * not process any of the remaining unprocessed TargetModuleID objects.
     * The processed TargetModuleIDs must be returned by the method
     * getResultTargetModuleIDs.
     *
     * @throws OperationUnsupportedException occurs when this optional command
     *         is not supported by this implementation.
     */
    public void stop() throws OperationUnsupportedException;

    /**
     * Add a listener to receive progress events on deployment actions.
     *
     * @param pol the listener to receive events
     */
    public void addProgressListener(ProgressListener pol);

    /**
     * Remove a progress listener.
     *
     * @param pol the listener to remove
     */
    public void removeProgressListener(ProgressListener pol);
}
