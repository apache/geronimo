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

package javax.security.jacc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Method;
import java.security.*;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 01:55:12 $
 */
public final class EJBMethodPermission extends Permission {

    private final static String NEW_METHOD_INTERFACES = "org.apache.security.jacc.EJBMethodPermission.methodInterfaces";
    private static String[] methodInterfaces;
    static {
        String newMethodInterfaces = (String)AccessController.doPrivileged(new
            PrivilegedAction() {
                public Object run() {
                    return System.getProperty(NEW_METHOD_INTERFACES);
                }
            });

        if (newMethodInterfaces != null) {
            newMethodInterfaces = newMethodInterfaces + ",Home,LocalHome,Remote,Local,ServiceEndpoint";
        } else {
            newMethodInterfaces = "Home,LocalHome,Remote,Local,ServiceEndpoint";
        }

        methodInterfaces = newMethodInterfaces.split(",", -1);
    }
    private transient int cachedHashCode = 0;
    private transient MethodSpec methodSpec;

    public EJBMethodPermission(String name, String spec) {
        super(name);

        methodSpec = new MethodSpec(spec);
    }

    public EJBMethodPermission(String EJBName, String methodName, String methodInterface, String[] methodParams) {
        super(EJBName);

        methodSpec = new MethodSpec(methodName, methodInterface, methodParams);
    }

    public EJBMethodPermission(String EJBName, String methodInterface, Method method) {
        super(EJBName);

        if (method == null) throw new IllegalArgumentException("Parameter method must not be null");

        methodSpec = new MethodSpec(methodInterface, method);
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof EJBMethodPermission)) return false;

        EJBMethodPermission other = (EJBMethodPermission)o;
        return getName().equals(other.getName()) && methodSpec.equals(other.methodSpec);
    }

    public String getActions() {
        return methodSpec.getActions();
    }

    public int hashCode() {
        if (cachedHashCode == 0) {
            cachedHashCode = getName().hashCode() ^ methodSpec.hashCode();
        }
        return cachedHashCode;
    }

    public boolean implies(Permission permission) {
        if (permission == null || !(permission instanceof EJBMethodPermission)) return false;

        EJBMethodPermission other = (EJBMethodPermission)permission;
        return getName().equals(other.getName()) && methodSpec.implies(other.methodSpec);
    }

    // TODO should return a real PermissionCollection
    public PermissionCollection newPermissionCollection() {
    	return null;
    }

    private synchronized void readObject(ObjectInputStream in) throws IOException {
        methodSpec = new MethodSpec(in.readUTF());
    }

    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(methodSpec.getActions());
    }

    private class MethodSpec {
        private String methodName;
        private String methodInterface;
        private String methodParams;
        private String actions;

        public MethodSpec(String actionString) {
            if (actionString == null || actionString.length() == 0) {
                methodName = null;
                methodInterface = null;
                methodParams = null;
                actions = "";
            } else {
                String[] tokens = actionString.split(",", 3);

                switch (tokens.length) {
                    case 1: {
                        methodName = emptyNullCheck(tokens[0]);
                        methodInterface = null;
                        methodParams = null;
                        break;
                    }
                    case 2: {
                        if (tokens[1].length() == 0) throw new IllegalArgumentException("This format of actions requires a method interface");
                        checkMethodInterface(tokens[1]);

                        methodName = emptyNullCheck(tokens[0]);
                        methodInterface = emptyNullCheck(tokens[1]);
                        methodParams = null;
                        break;
                    }
                    case 3: {
                        checkMethodInterface(tokens[1]);
                        if (tokens[2].indexOf(',') > -1) {
                            String[] test = tokens[2].split(",", -1);
                            for (int i=0; i<test.length; i++) {
                                if (test[i].length() == 0) throw new IllegalArgumentException("Invalid type name");
                            }
                        }

                        methodName = emptyNullCheck(tokens[0]);
                        methodInterface = emptyNullCheck(tokens[1]);
                        methodParams = emptyNullCheck(tokens[2]);
                    }
                }
                actions = actionString;
            }
        }

        public MethodSpec(String methodName, String methodInterface, String[] methodParamsArray) {
            checkMethodInterface(methodInterface);

            methodName = emptyNullCheck(methodName);
            methodInterface = emptyNullCheck(methodInterface);

            if (methodParamsArray == null) {
                methodParams = null;
            } else if (methodParamsArray.length == 0) {
                methodParams = "";
            } else {
                if (methodParamsArray[0] == null || methodParamsArray[0].length() == 0) throw new IllegalArgumentException("Invalid type name");

                StringBuffer buffer = new StringBuffer(methodParamsArray[0]);
                for (int i=1; i<methodParamsArray.length; i++) {
                    if (methodParamsArray[i] == null || methodParamsArray[i].length() == 0) throw new IllegalArgumentException("Invalid type name");

                    buffer.append(",");
                    buffer.append(methodParamsArray[i]);
                }
                methodParams = buffer.toString();
            }

            initActions();
        }

        public MethodSpec(String methodInterface, Method method) {
            checkMethodInterface(methodInterface);

            methodName = method.getName();
            methodInterface = emptyNullCheck(methodInterface);

            Class[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 0) {
                methodParams = "";
            } else {
                StringBuffer buffer = new StringBuffer(paramTypes[0].getName());
                for (int i=1; i<paramTypes.length; i++) {
                    buffer.append(",");
                    buffer.append(paramTypes[i].getName());
                }
                methodParams = buffer.toString();
            }

            initActions();
        }

        public boolean equals(MethodSpec spec) {
            return actions.equals(spec.actions);
        }

        public String getActions() {
            return actions;
        }

        public int hashCode() {
            return actions.hashCode();
        }

        public boolean implies(MethodSpec methodSpec) {
            if (methodName == null || methodName.equals(methodSpec.methodName)) {
                if (methodInterface == null || methodInterface.equals(methodSpec.methodInterface)) {
                    if (methodParams == null || methodParams.equals(methodSpec.methodParams)) {
                        return true;
                    } else return false;
                } else return false;
            } else return false;
        }

        private void initActions() {
            if (methodParams == null) {
                if (methodInterface == null) {
                    if (methodName == null) {
                        actions = ",,";
                    } else {
                        actions = methodName;
                    }
                } else {
                    if (methodName == null) {
                        actions = "," + methodInterface;
                    } else {
                        actions = methodName + "," + methodInterface;
                    }
                }
            } else {
                if (methodInterface == null) {
                    if (methodName == null) {
                        actions = ",," + methodParams;
                    } else {
                        actions = methodName + ",," + methodParams;
                    }
                } else {
                    if (methodName == null) {
                        actions = "," + methodInterface + "," + methodParams;
                    } else {
                        actions = methodName + "," + methodInterface + "," + methodParams;
                    }
                }
            }
        }

        private void checkMethodInterface(String methodInterface) {
            if (methodInterface == null || methodInterface.length() == 0) return;

            for (int i=0; i<methodInterfaces.length; i++) {
                if (methodInterfaces[i].equals(methodInterface)) return;
            }
            throw new IllegalArgumentException("Invalid method interface");
        }

        /**
         * For the method name, method interface, and method parameters, a
         * value of <CODE>null</CODE> indicates a wildcard value.  This
         * function is used to check if we are passed a <CODE>null</CODE>
         * or empty string, which indicates a wildcard.
         *
         * @param name   The name to be checked.
         *
         * @return <CODE>null</CODE> if we are passed a <CODE>null</CODE> or empty string else
         *         we return the name.
         */
        private String emptyNullCheck(String name) {
            if (name != null && name.length() == 0) {
                return null;
            } else {
                return name;
            }
        }
    }
}

