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
package org.apache.geronimo.jmx;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.management.MBeanNotificationInfo;

/**
 * Describes a notification of a GeronimoMBean.  This extension allows the properties to be mutable during setup,
 * and once the MBean is deployed an imutable copy of will be made.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/05 02:38:32 $
 */
public final class GeronimoNotificationInfo extends MBeanNotificationInfo {
    private final boolean immutable;
    private String name;
    private String description;
    private final Set notificationTypes = new HashSet();
    private final int hashCode = System.identityHashCode(this);

    public GeronimoNotificationInfo() {
        super(null, null, null);
        immutable = false;
    }

    GeronimoNotificationInfo(GeronimoNotificationInfo source, GeronimoMBeanInfo parent) {
        super(null, null, null);
        immutable = true;
        name = source.name;
        description = source.name;
        notificationTypes.addAll(source.notificationTypes);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.description = description;
    }

    public String[] getNotifTypes() {
        return (String[]) notificationTypes.toArray(new String[notificationTypes.size()]);
    }

    public void addNotificationType(String notificationType) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        notificationTypes.add(notificationType);
    }

    public void addAllNotificationTypes(String[] notificationTypes) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        for (int i = 0; i < notificationTypes.length; i++) {
            this.notificationTypes.add(notificationTypes[i]);
        }
    }

    public void addAllNotificationTypes(Collection notificationTypes) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        for (Iterator iterator = notificationTypes.iterator(); iterator.hasNext();) {
            String notificationType = (String) iterator.next();
            this.notificationTypes.add(notificationType);
        }
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object object) {
        return (this == object);
    }

    public String toString() {
        return "[GeronimoNotificationInfo: name=" + name + " description=" + description + "]";
    }
}
