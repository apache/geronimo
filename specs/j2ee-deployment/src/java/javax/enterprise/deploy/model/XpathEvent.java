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
package javax.enterprise.deploy.model;

import java.beans.PropertyChangeEvent;

/**
 * An Event class describing DDBeans being added to or removed from a J2EE
 * application, or updated in place.
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/30 02:16:58 $
 */
public class XpathEvent {
    /**
     * Adding a DDBean
     */
    public static final Object BEAN_ADDED = new Object();
    /**
     * Removing a DDBean
     */
    public static final Object BEAN_REMOVED = new Object();
    /**
     * Changing a DDBean
     */
    public static final Object BEAN_CHANGED = new Object();

    private PropertyChangeEvent pce;
    private DDBean bean;
    private Object type;

    /**
     * A description of a change in the DDBean tree.
     *
     * @param bean The DDBean being added, removed, or updated.
     * @param type Indicates whether this is an add, remove, or update event.
     */
    public XpathEvent(DDBean bean, Object type) {
        this.bean = bean;
        this.type = type;
    }

    /**
     * Gets the underlying property change event, with new and
     * old values.  This is typically used for change events.
     * It is not in the public API, but is included in the
     * downloadable JSR-88 classes.
     */
    public PropertyChangeEvent getChangeEvent() {
        return pce;
    }

    /**
     * Sets the underlying property change event, with new and
     * old values.  This is typically used for change events.
     * It is not in the public API, but is included in the
     * downloadable JSR-88 classes.
     *
     * @param pce The property change event that triggered this XpathEvent.
     */
    public void setChangeEvent(PropertyChangeEvent pce) {
        this.pce = pce;
    }

    /**
     * The bean being added/removed/changed.
     *
     * @return The bean being added/removed/changed.
     */
    public DDBean getBean() {
        return bean;
    }

    /**
     * Is this an add event?
     *
     * @return <code>true</code> if this is an add event.
     */
    public boolean isAddEvent() {
        return BEAN_ADDED == type;
    }

    /**
     * Is this a remove event?
     *
     * @return <code>true</code> if this is a remove event.
     */
    public boolean isRemoveEvent() {
        return BEAN_REMOVED == type;
    }

    /**
     * Is this a change event?
     *
     * @return <code>true</code> if this is a change event.
     */
    public boolean isChangeEvent() {
        return BEAN_CHANGED == type;
    }
}