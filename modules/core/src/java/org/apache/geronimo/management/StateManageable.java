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
package org.apache.geronimo.management;


/**
 * A Java interface the meets the J2EE Management specification for a state manageable object.
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/20 07:13:09 $
 */
public interface StateManageable {
    /**
     * Gets the state of this component as an int.
     * The int return is required by the JSR77 specification.
     * @see #getStateInstance to obtain the State instance
     * @return the current state of this component
     */
    int getState();

    /**
     * Gets the state of this component as a State instance.
     * @return the current state of this component
     */
    State getStateInstance();

    /**
     * Gets the start time of this component
     * @return time in milliseonds since epoch that this component was started.
     */
    long getStartTime();


    /**
     * Transitions the component to the starting state.  This method has access to the
     * container.
     *
     * Normally a component uses this to cache data from other components. The other components will
     * have been created at this stage, but not necessairly started and may not be ready to have methods
     * invoked on them.
     *
     * @throws java.lang.Exception if a problem occurs during the transition
     * @throws java.lang.IllegalStateException if this interceptor is not in the stopped or failed state
     */
    void start() throws Exception, IllegalStateException;

    /**
     * Transitions the component to the starting state.  This method has access to the
     * container.
     *
     * If this Component is a Container, then startRecursive is called on all child Components
     * that are in the STOPPED or FAILED state.
     * Normally a component uses this to cache data from other components. The other components will
     * have been created at this stage, but not necessairly started and may not be ready to have methods
     * invoked on them.
     *
     * @throws java.lang.Exception if a problem occurs during the transition
     * @throws java.lang.IllegalStateException if this interceptor is not in the STOPPED or FAILED state
     */
    void startRecursive() throws Exception, IllegalStateException;

    /**
     * Transitions the component to the stopping state.  This method has access to the
     * container.
     *
     * If this is Component is a Container, then all its child components must be in the
     * STOPPED or FAILED State.
     *
     * Normally a component uses this to drop references to data cached in the start method.
     * The other components will not necessairly have been stopped at this stage and may not be ready
     * to have methods invoked on them.
     *
     * @throws java.lang.Exception if a problem occurs during the transition
     * @throws java.lang.IllegalStateException if this interceptor is not in the STOPPED or FAILED state
     */
    void stop() throws Exception, IllegalStateException;

}
