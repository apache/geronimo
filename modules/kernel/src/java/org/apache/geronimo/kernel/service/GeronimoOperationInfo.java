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

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;

import net.sf.cglib.MethodProxy;

/**
 * Describes an operation on a GeronimoMBean.  This extension allows the properties to be mutable during setup,
 * and once the MBean is deployed an imutable copy of will be made.  This class also adds support to
 * direct the operation to a specific target in a multi target GeronimoMBean.  It also supports caching of the
 * invocation result, which can reduce the number of calls on the target.
 *
 * @version $Revision: 1.1 $ $Date: 2003/09/08 04:38:35 $
 */
public final class GeronimoOperationInfo extends MBeanOperationInfo {
    /**
     * Is this class still mutable from users.
     */
    private final boolean immutable;

    /**
     * The name of this method.
     */
    private String name;

    /**
     * Parameters of this method.
     */
    private final List parameters = new LinkedList();

    /**
     * Parameter types of this operation.
     */
    private final String[] parameterTypes;

    /**
     * Return type of the operation.
     * This is set during the transition to running.
     */
    private final String returnType;

    /**
     * A user displayable description of this method.
     */
    private String description;

    /**
     * Impact of the method as defined in the JMX spec.
     */
    private int impact;

    /**
     * Logical name of the target.
     */
    private String targetName;

    /**
     * Target method name.
     */
    private String targetMethodName;

    /**
     * The maximum ammount ot time in seconds that a cached value is valid.
     */
    long cacheTimeLimit;

    //
    // Runtime information -- this is not exposed to clients
    //

    /**
     * The object on which the method will be invoked.
     */
    Object target;

    /**
     * The method that will be called on the target.
     */
    final MethodProxy methodProxy;

    /**
     * The cached result of the method.
     */
    Object value;

    /**
     * Time stamp from when the value field was last updated.
     */
    long lastUpdate;
    private final int hashCode = System.identityHashCode(this);

    public GeronimoOperationInfo() {
        super(null, null, null, null, MBeanOperationInfo.UNKNOWN);
        immutable = false;
        methodProxy = null;
        returnType = null;
        parameterTypes = null;
    }

    GeronimoOperationInfo(GeronimoOperationInfo source, GeronimoMBeanInfo parent) {
        super(null, null, null, null, MBeanOperationInfo.UNKNOWN);
        this.immutable = true;

        //
        // Required
        //
        if (source.name == null) {
            throw new IllegalArgumentException("Operation name is null");
        }
        name = source.name;


        //
        // Optional
        //
        description = source.description;
        impact = source.impact;
        cacheTimeLimit = source.cacheTimeLimit;

        //
        // Optional (derived)
        //
        if (source.target != null) {
            target = source.target;
            targetName = source.targetName;
        } else if (source.targetName == null) {
            target = parent.getTarget();
        } else {
            targetName = source.targetName;
            target = parent.getTarget(targetName);
        }

        if (source.targetMethodName == null) {
            targetMethodName = name;
        } else {
            targetMethodName = source.targetMethodName;
        }

        parameterTypes = new String[source.parameters.size()];
        Class[] types = new Class[source.parameters.size()];
        for (int i = 0; i < source.parameters.size(); i++) {
            GeronimoParameterInfo parameterInfo = (GeronimoParameterInfo) source.parameters.get(i);
            parameterInfo = new GeronimoParameterInfo(parameterInfo, this);
            parameters.add(parameterInfo);
            types[i] = parameterInfo.getTypeClass();
            parameterTypes[i] = parameterInfo.getType();
        }

        //
        // Derived
        //
        Method method = null;
        try {
            method = target.getClass().getMethod(targetMethodName, types);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("Target does not have specifed method:" +
                    " target=" + target +
                    " methodName=" + targetMethodName);
        } catch (SecurityException e) {
            e.printStackTrace();  //To change body of catch statement use Options | File Templates.
        }
        returnType = method.getReturnType().getName();
        methodProxy = MethodProxy.create(method, method);
    }

    public String getName() {
        return name;
    }

    public void setName(String operationName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.name = operationName;
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

    public int getImpact() {
        return impact;
    }

    public void setImpact(int impact) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        if (impact != MBeanOperationInfo.ACTION &&
                impact != MBeanOperationInfo.ACTION_INFO &&
                impact != MBeanOperationInfo.INFO &&
                impact != MBeanOperationInfo.UNKNOWN) {
            throw new IllegalArgumentException("Unknow impact type: " + impact);
        }
        this.impact = impact;
    }

    public String getReturnType() {
        return returnType;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.targetName = targetName;
    }

    public String getTargetMethodName() {
        return targetMethodName;
    }

    public void setMethodName(String targetMethodName) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.targetMethodName = targetMethodName;
    }

    public long getCacheTimeLimit() {
        return cacheTimeLimit;
    }

    public void setCacheTimeLimit(long cacheTimeLimit) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        this.cacheTimeLimit = cacheTimeLimit;
    }

    public String getCachePolicy() {
        if (cacheTimeLimit < 0) {
            return GeronimoMBeanInfo.NEVER;
        } else if (cacheTimeLimit == 0) {
            return GeronimoMBeanInfo.ALWAYS;
        } else {
            return "" + cacheTimeLimit;
        }

    }

    public void setCachePolicy(String cacheTimeLimit) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        if (cacheTimeLimit == null || cacheTimeLimit.length() == 0) {
            throw new IllegalArgumentException("cacheTimeLimit is null");
        }
        if (GeronimoMBeanInfo.ALWAYS.equalsIgnoreCase(cacheTimeLimit)) {
            this.cacheTimeLimit = 0;
        } else if (GeronimoMBeanInfo.NEVER.equalsIgnoreCase(cacheTimeLimit)) {
            this.cacheTimeLimit = -1;
        } else {
            this.cacheTimeLimit = Long.parseLong(cacheTimeLimit);
        }
    }

    public List getParameterList() {
        return Collections.unmodifiableList(parameters);
    }

    public String[] getParameterTypes() {
        return parameterTypes;
    }

    public MBeanParameterInfo[] getSignature() {
        return (MBeanParameterInfo[]) parameters.toArray(new MBeanParameterInfo[parameters.size()]);
    }

    public void addParameterInfo(GeronimoParameterInfo parameterInfo) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        parameters.add(parameterInfo);
    }

    public void addParameterInfo(String name, String type, String description) {
        if (immutable) {
            throw new IllegalStateException("Data is no longer mutable");
        }
        GeronimoParameterInfo parameterInfo = new GeronimoParameterInfo();
        parameterInfo.setName(name);
        parameterInfo.setType(type);
        parameterInfo.setDescription(description);
        parameters.add(parameterInfo);
    }

    public int hashCode() {
        return hashCode;
    }

    public boolean equals(Object object) {
        return (this == object);
    }

    public String toString() {
        return "[GeronimoOperationInfo: name=" + name + " description=" + description + "]";
    }
}
