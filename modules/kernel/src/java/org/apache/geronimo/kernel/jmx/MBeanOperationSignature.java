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
package org.apache.geronimo.kernel.jmx;

import java.lang.reflect.Method;
import java.util.List;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;

/**
 * This is a key class based on a MBean operation name and parameters.
 *
 * @version $Revision: 1.3 $ $Date: 2004/01/16 23:31:21 $
 */
public final class MBeanOperationSignature {
    private final static String[] NO_TYPES = new String[0];
    private final String name;
    private final String[] argumentTypes;

    public MBeanOperationSignature(Method method) {
        name = method.getName();
        Class[] parameters = method.getParameterTypes();
        argumentTypes = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            argumentTypes[i] = parameters[i].getName();
        }
    }

    public MBeanOperationSignature(MBeanOperationInfo operationInfo) {
        name = operationInfo.getName();
        MBeanParameterInfo[] parameters = operationInfo.getSignature();
        argumentTypes = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            argumentTypes[i] = parameters[i].getType();
        }
    }

    public MBeanOperationSignature(String name, String[] argumentTypes) {
        this.name = name;
        if (argumentTypes != null) {
            this.argumentTypes = argumentTypes;
        } else {
            this.argumentTypes = NO_TYPES;
        }
    }

    public MBeanOperationSignature(String name, List argumentTypes) {
        this.name = name;
        if (argumentTypes != null) {
            this.argumentTypes = new String[argumentTypes.size()];
            for (int i = 0; i < argumentTypes.size(); i++) {
                this.argumentTypes[i] = (String) argumentTypes.get(i);
            }
        } else {
            this.argumentTypes = NO_TYPES;
        }
    }

    public boolean equals(Object object) {
        if (!(object instanceof MBeanOperationSignature)) {
            return false;
        }

        // match names
        MBeanOperationSignature methodKey = (MBeanOperationSignature) object;
        if (!methodKey.name.equals(name)) {
            return false;
        }

        // match arg length
        int length = methodKey.argumentTypes.length;
        if (length != argumentTypes.length) {
            return false;
        }

        // match each arg
        for (int i = 0; i < length; i++) {
            if (!methodKey.argumentTypes[i].equals(argumentTypes[i])) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        int result = 17;
        result = 37 * result + name.hashCode();
        for (int i = 0; i < argumentTypes.length; i++) {
            result = 37 * result + argumentTypes[i].hashCode();
        }
        return result;
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer(name).append("(");
        for (int i = 0; i < argumentTypes.length; i++) {
            if(i > 0) {
                buffer.append(", ");
            }
            buffer.append(argumentTypes[i]);
        }
        return buffer.append(")").toString();
    }
}
