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
import org.apache.management.j2ee.State;

/**
 * A helper implementation of the Component interface that should
 * be used as a base class for Component implementations.
 *
 * @todo this is broken because name is required but there is no way to set it
 *
 * @version $Revision: 1.7 $ $Date: 2003/08/18 13:30:19 $
 */
public class AbstractComponent extends AbstractStateManageable implements Component {
    /**
     *  the Container this Component belongs to
     */
    private Container container;

    /**
     * the identity of this Component
     */
    private String name;


    /**
     * The Container that this Component belongs to
     *
     * @return a <code>Container</code> value
     */
    public Container getContainer() {
        return container;
    }

    /**
     * Sets the container which ownes this component.
     * The contianer can only be set before create() or to null after the destroy().
     *
     * @param container which owns this component
     * @throws java.lang.IllegalStateException if this component is not in the not-created or destroyed state
     * @throws java.lang.IllegalArgumentException if this comonent has not been created and the container
     * parameter is null, or the component has been destroyed and the container parameter is NOT null
     */
    public void setContainer(Container container)
            throws IllegalStateException, IllegalArgumentException {
        if (getStateInstance() != State.STOPPED) {
            throw new IllegalStateException(
                    "Set container can only be called while in the stopped state: state=" + getStateInstance());
        }
        this.container = container;
    }

    /* Start the Component
     * @see org.apache.geronimo.common.AbstractStateManageable#doStart()
     */
    protected void doStart() throws Exception {
    }

    /* Stop the Component
     * @see org.apache.geronimo.common.AbstractStateManageable#doStop()
     */
    protected void doStop() throws Exception {
    }

    /*
     * @see org.apache.geronimo.common.AbstractStateManageable#doNotification(java.lang.String)
     */
    public void doNotification(String eventTypeValue) {
        log.debug("notification: " + eventTypeValue + " from " + this);
    }


    /**
     * Get the unique identity of this Component
     *
     * @return the name (formatted according to JSR 77)
     */
    public String getObjectName() {
        return name;
    }

    /**
     * Two Components are equal if they have the same name;
     *
     * @param component to test
     * @return true if the names are the same, false otherwise
     */
    public boolean equals(Object o) {
        if (o instanceof Component) {
            Component component = (Component)o;
            return component.getObjectName().equals(name);
        }
        return false;
    }


    /**
     * Get a hash value for this Component.
     * This is the hash of the unique name of
     * the Component.
     *
     * @return hash of Component name
     */
    public int hashCode() {
        if (name == null) {
            return 0;
        }
        return name.hashCode();
    }
}
