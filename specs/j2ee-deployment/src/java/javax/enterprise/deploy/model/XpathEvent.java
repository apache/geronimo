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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.enterprise.deploy.model;

import java.beans.PropertyChangeEvent;

/**
 * An Event class describing DDBeans being added to or removed from a J2EE
 * application, or updated in place.
 *
 * @version $Revision: 1.3 $ $Date: 2004/02/25 09:58:34 $
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