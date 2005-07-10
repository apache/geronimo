/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package javax.security.jacc;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.PrivilegedAction;
import java.util.LinkedList;
import java.util.HashMap;
import java.util.Enumeration;
import java.util.Collections;

/**
 * @version $Rev$ $Date$
 */
public final class EJBMethodPermission extends Permission implements Serializable {

    private final static String NEW_METHOD_INTERFACES = "org.apache.security.jacc.EJBMethodPermission.methodInterfaces";
    private static String[] methodInterfaces;

    static {
        String newMethodInterfaces = (String) AccessController.doPrivileged(new
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

    private transient int cachedHashCode;
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

        EJBMethodPermission other = (EJBMethodPermission) o;
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

        EJBMethodPermission other = (EJBMethodPermission) permission;
        return getName().equals(other.getName()) && methodSpec.implies(other.methodSpec);
    }

    public PermissionCollection newPermissionCollection() {
        return new EJBMethodPermissionCollection();
    }

    private synchronized void readObject(ObjectInputStream in) throws IOException {
        methodSpec = new MethodSpec(in.readUTF());
    }

    private synchronized void writeObject(ObjectOutputStream out) throws IOException {
        out.writeUTF(methodSpec.getActions());
    }

    private static class MethodSpec {
        protected String methodName;
        protected String methodInterface;
        protected String methodParams;
        protected String actions;

        public MethodSpec(String actionString) {
            if (actionString == null || actionString.length() == 0) {
                methodName = null;
                methodInterface = null;
                methodParams = null;
                actions = "";
            } else {
                String[] tokens = actionString.split(",", 3);

                switch (tokens.length) {
                    case 1:
                        {
                            methodName = emptyNullCheck(tokens[0]);
                            methodInterface = null;
                            methodParams = null;
                            break;
                        }
                    case 2:
                        {
                            if (tokens[1].length() == 0) throw new IllegalArgumentException("This format of actions requires a method interface");
                            checkMethodInterface(tokens[1]);

                            methodName = emptyNullCheck(tokens[0]);
                            methodInterface = emptyNullCheck(tokens[1]);
                            methodParams = null;
                            break;
                        }
                    case 3:
                        {
                            checkMethodInterface(tokens[1]);
                            if (tokens[2].indexOf(',') > -1) {
                                String[] test = tokens[2].split(",", -1);
                                for (int i = 0; i < test.length; i++) {
                                    if (test[i].length() == 0) throw new IllegalArgumentException("Invalid type name");
                                }
                            }

                            methodName = emptyNullCheck(tokens[0]);
                            methodInterface = emptyNullCheck(tokens[1]);
                            methodParams = tokens[2];
                        }
                }
                actions = actionString;
            }
        }

        public MethodSpec(String mthdName, String mthdInterface, String[] methodParamsArray) {
            checkMethodInterface(mthdInterface);

            methodName = emptyNullCheck(mthdName);
            methodInterface = emptyNullCheck(mthdInterface);

            if (methodParamsArray == null) {
                methodParams = null;
            } else if (methodParamsArray.length == 0) {
                methodParams = "";
            } else {
                if (methodParamsArray[0] == null || methodParamsArray[0].length() == 0) throw new IllegalArgumentException("Invalid type name");

                StringBuffer buffer = new StringBuffer(methodParamsArray[0]);
                for (int i = 1; i < methodParamsArray.length; i++) {
                    if (methodParamsArray[i] == null || methodParamsArray[i].length() == 0) throw new IllegalArgumentException("Invalid type name");

                    buffer.append(",");
                    buffer.append(methodParamsArray[i]);
                }
                methodParams = buffer.toString();
            }

            initActions();
        }

        public MethodSpec(String mthdInterface, Method method) {
            checkMethodInterface(mthdInterface);

            methodName = method.getName();
            methodInterface = emptyNullCheck(mthdInterface);

            Class[] paramTypes = method.getParameterTypes();
            if (paramTypes.length == 0) {
                methodParams = "";
            } else {
                StringBuffer buffer = new StringBuffer(paramTypes[0].getName());
                for (int i = 1; i < paramTypes.length; i++) {
                    buffer.append(",");
                    buffer.append(paramTypes[i].getName());
                }
                methodParams = buffer.toString();
            }

            initActions();
        }

        public boolean equals(MethodSpec spec) {
            return implies(spec) && spec.implies(this);
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
                    } else
                        return false;
                } else
                    return false;
            } else
                return false;
        }

        private void initActions() {
            if (methodParams == null) {
                if (methodInterface == null) {
                    if (methodName == null) {
                        actions = "";
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

            for (int i = 0; i < methodInterfaces.length; i++) {
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
         * @param name The name to be checked.
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

    private static final class EJBMethodPermissionCollection extends PermissionCollection {

        private LinkedList collection = new LinkedList();
        private HashMap permissions = new HashMap();
        private static final String WILDCARD = new String("$WILDCARD");

        /**
         * Adds a permission object to the current collection of permission objects.
         *
         * @param permission the Permission object to add.
         *
         * @exception SecurityException -  if this PermissionCollection object
         *                                 has been marked readonly
         */

        public void add(Permission permission) {

            if (isReadOnly()) throw new IllegalArgumentException("Read only collection");

            if (!(permission instanceof EJBMethodPermission)) throw new IllegalArgumentException("Wrong permission type");

            if (collection.contains(permission)) return;
            else collection.add(permission);

            EJBMethodPermission p = (EJBMethodPermission)permission;
            EJBMethodPermission.MethodSpec spec = p.methodSpec;
            Object test =  permissions.get(p.getName());

            if (test instanceof Boolean) return;

            if (spec.methodName == null && spec.methodInterface == null && spec.methodParams == null) {
                permissions.put(p.getName(), new Boolean(true));
                return;
            }

            HashMap methods = (HashMap)test;
            if (methods == null) {
                methods = new HashMap();
                permissions.put(p.getName(), methods);
            }

            Object methodKey = (spec.methodName == null || spec.methodName.length() == 0? WILDCARD:spec.methodName);
            HashMap interfaces = (HashMap)methods.get(methodKey);
            if (interfaces == null) {
                interfaces = new HashMap();
                methods.put(methodKey, interfaces);
            }

            Object interfaceKey = (spec.methodInterface == null || spec.methodInterface.length() == 0? WILDCARD:spec.methodInterface);
            HashMap parameters = (HashMap)interfaces.get(interfaceKey);
            if (parameters == null) {
                parameters = new HashMap();
                interfaces.put(interfaceKey, parameters);
            }



            // an empty string for a parameter spec indicates a method w/ no parameters
            Object parametersKey = (spec.methodParams == null? WILDCARD:spec.methodParams);
            Object parameter = parameters.get(parametersKey);
            if (parameter == null) {
                parameter = new Boolean(true);
                parameters.put(parametersKey, parameter);
            }

        }

        /**
         * Checks to see if the specified permission is implied by
         * the collection of Permission objects held in this PermissionCollection.
         *
         * @param permission the Permission object to compare.
         *
         * @return true if "permission" is implied by the  permissions in
         * the collection, false if not.
         */
        public boolean implies(Permission permission) {

            if (!(permission instanceof EJBMethodPermission)) return false;

            EJBMethodPermission p = (EJBMethodPermission)permission;

            EJBMethodPermission.MethodSpec spec = p.methodSpec;
            Object test = permissions.get(p.getName());

            if (test == null) return false;
            if (test instanceof Boolean) return true;

            HashMap methods = (HashMap)test;

            Object methodKey = (spec.methodName == null || spec.methodName.length() == 0? WILDCARD:spec.methodName);
            HashMap interfaces = (HashMap)methods.get(methodKey);

            if (methodImplies(interfaces, spec)) return true;
            if (methodKey != WILDCARD) {
                return methodImplies((HashMap)methods.get(WILDCARD), spec);
            }

            return false;
        }



        protected boolean methodImplies(HashMap interfaces, EJBMethodPermission.MethodSpec spec) {

            if (interfaces == null) return false;

            Object interfaceKey = (spec.methodInterface == null || spec.methodInterface.length() == 0? WILDCARD:spec.methodInterface);
            HashMap parameters = (HashMap)interfaces.get(interfaceKey);

            if (interfaceImplies(parameters, spec)) return true;
            if (interfaceKey != WILDCARD) {
                return interfaceImplies((HashMap)interfaces.get(WILDCARD), spec);
            }

            return false;
        }



        protected boolean interfaceImplies(HashMap parameters, EJBMethodPermission.MethodSpec spec) {

            if (parameters == null) return false;

            // An empty string for a parameter spec indicates a method w/ no parameters
            // so we won't convert an empty string to a wildcard.
            Object parametersKey = (spec.methodParams == null? WILDCARD:spec.methodParams);
            Object parameter = parameters.get(parametersKey);

            if (parameter != null) return true;
            if (parametersKey != WILDCARD) {
                return parameters.containsKey(WILDCARD);
            }

            return false;
        }



        /**
         * Returns an enumeration of all the Permission objects in the collection.
         *
         * @return an enumeration of all the Permissions.
         */
        public Enumeration elements() {
            return Collections.enumeration(collection);
        }
    }
}

