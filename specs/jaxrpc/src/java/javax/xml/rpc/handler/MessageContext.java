/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.xml.rpc.handler;

import java.util.Iterator;

/**
 * The interface <code>MessageContext</code> abstracts the message
 * context that is processed by a handler in the <code>handle</code>
 * method.
 *
 * <p>The <code>MessageContext</code> interface provides methods to
 * manage a property set. <code>MessageContext</code> properties
 * enable handlers in a handler chain to share processing related
 * state.
 *
 * @version 1.0
 */
public interface MessageContext {

    /**
     * Sets the name and value of a property associated with the
     * <code>MessageContext</code>. If the <code>MessageContext</code>
     * contains a value of the same property, the old value is replaced.
     *
     * @param  name ame of the property associated with the
     *         <code>MessageContext</code>
     * @param  value Value of the property
     * @throws java.lang.IllegalArgumentException If some aspect
     *         the property is prevents it from being stored
     *         in the context
     * @throws java.lang.UnsupportedOperationException If this method is
     *         not supported.
     */
    public abstract void setProperty(String name, Object value);

    /**
     * Gets the value of a specific property from the
     * <code>MessageContext</code>.
     *
     * @param name the name of the property whose value is to be
     *        retrieved
     * @return the value of the property
     * @throws java.lang.IllegalArgumentException if an illegal
     *        property name is specified
     */
    public abstract Object getProperty(String name);

    /**
     * Removes a property (name-value pair) from the
     * <code>MessageContext</code>.
     *
     * @param  name the name of the property to be removed
     *
     * @throws java.lang.IllegalArgumentException if an illegal
     *        property name is specified
     */
    public abstract void removeProperty(String name);

    /**
     * Returns true if the <code>MessageContext</code> contains a property
     * with the specified name.
     * @param   name Name of the property whose presense is to be tested
     * @return  Returns true if the MessageContext contains the
     *     property; otherwise false
     */
    public abstract boolean containsProperty(String name);

    /**
     * Returns an Iterator view of the names of the properties
     * in this <code>MessageContext</code>.
     *
     * @return Iterator for the property names
     */
    public abstract Iterator getPropertyNames();
}

