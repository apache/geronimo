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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

/**
 * Abstract implementation of Container interface.
 * 
 * @version $Revision: 1.2 $ $Date: 2003/08/15 14:11:26 $ 
 * 
*/
public abstract class AbstractContainer
    extends AbstractComponent
    implements Container
{
    private ArrayList components = new ArrayList();

    /**
     * A Container cannot be itself contained in a Container. 
     *
     * NB. Do we agree this is the model?
     * @return always null
     */
    public Container getContainer()
    {
        return null;
    }

    /**
     * A Container cannot be itself contained in a Container.
     *     
     * NB. Do we agree this is the model?
     * @throws java.lang.UnsupportedOperationException
     */
    public void setContainer()
    {
        throw new UnsupportedOperationException("Cannot call setContainer on a Container");
    }

    /**
     * Add a component to the set for a Container.
     * Subclasses might like to override this in order
     * to check their state before allowing the addition.
     *
     * @param component 
     */
    public void addComponent(Component component)
    {
        if (component == null)
            return;

        components.add(component);
    }

    /**
     * Get all the Components known to the Container
     *
     * @return an immutable List of Components
     */
    public List getComponents()
    {
        return Collections.unmodifiableList(components);
    }

    /**
     * Remove a Component from the Container.
     * If the Component is not in the Container,
     * an Exception is thrown.
     *
     * Subclasses might want to override this, for
     * example to disallow Component addition/removal
     * after the Container is started.
     *
     * @param component the Component to remove
     */
    public void removeComponent(Component component) throws Exception
    {
        if (component == null)
            return;

        components.remove(component);
    }

    /**
     * Start the Container, and all of its Components.
     *
     * @exception Exception if an error occurs
     */
    public void startRecursive() throws Exception
    {
        //start the container itself
        start();

        //start the Components
        Iterator itor = components.iterator();
        while (itor.hasNext())
        {
            //only start stopped or failed Components as per JSR77
            Component c = (Component) itor.next();
            if ((c.getStateInstance() == State.STOPPED)
                || 
                (c.getStateInstance() == State.FAILED))
                c.startRecursive();
        }

        //NOTE: it is perfectly possible that some Components will be
        //in the RUNNING state and some of them in the STOPPED or FAILED state
    }


    /* -------------------------------------------------------------------------------------- */
    /** Do a recursive stop.
     * 
     * @see org.apache.geronimo.common.StateManageable#stop()
     */
    public void stop()
    {
        // Stop all the Components in reverse insertion order
        try
        {
            setState(State.STOPPING);

            for (ListIterator iterator =
                components.listIterator(components.size());
                iterator.hasPrevious();
                )
            {
                Interceptor interceptor = (Interceptor) iterator.previous();
                interceptor.stop();
            }
            setState(State.STOPPED);
        }
        finally
        {
            if (getStateInstance() != State.STOPPED)
                setState(State.FAILED);
        }
    }

}
