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

import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecurityPermission;

/**
 * Abstract factory and finder class for obtaining the instance of the class
 * that implements the PolicyConfigurationFactory of a provider. The factory
 * will be used to instantiate PolicyConfiguration objects that will be used
 * by the deployment tools of the container to create and manage policy
 * contexts within the Policy Provider.
 *
 * Implementation classes must have a public no argument constructor that may
 * be used to create an operational instance of the factory implementation class.
 * @see java.security.Permission
 * @see PolicyConfiguration
 * @see PolicyContextException
 * @version $Rev$ $Date$
 */
public abstract class PolicyConfigurationFactory {

    private final static String FACTORY_NAME = "javax.security.jacc.PolicyConfigurationFactory.provider";
    private static PolicyConfigurationFactory policyConfigurationFactory;

    /**
     * This static method uses a system property to find and instantiate (via a
     * public constructor) a provider specific factory implementation class.
     * The name of the provider specific factory implementation class is
     * obtained from the value of the system property,<p>
     * <code>javax.security.jacc.PolicyConfigurationFactory.provider</code>.
     * @return the singleton instance of the provider specific
     * PolicyConfigurationFactory implementation class.
     * @throws ClassNotFoundException when the class named by the system
     * property could not be found including because the value of the system
     * property has not be set.
     * @throws PolicyContextException if the implementation throws a checked
     * exception that has not been accounted for by the
     * getPolicyConfigurationFactory method signature. The exception thrown by
     * the implementation class will be encapsulated (during construction) in
     * the thrown PolicyContextException
     */
    public static PolicyConfigurationFactory getPolicyConfigurationFactory() throws ClassNotFoundException, PolicyContextException {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) sm.checkPermission(new SecurityPermission("setPolicy"));

        if (policyConfigurationFactory != null) return policyConfigurationFactory;

        final String[] factoryClassName = { null };
        try {
            policyConfigurationFactory = (PolicyConfigurationFactory)AccessController.doPrivileged(new
                PrivilegedExceptionAction() {
                    public Object run() throws Exception
                    {
                        factoryClassName[0] = System.getProperty(FACTORY_NAME);

                        if (factoryClassName[0] == null) throw new ClassNotFoundException("Property " + FACTORY_NAME + " not set");

                        return Class.forName(factoryClassName[0]).newInstance();
                    }
                });
        } catch(PrivilegedActionException pae) {
            if (pae.getException() instanceof ClassNotFoundException) {
                throw (ClassNotFoundException)pae.getException();
            } else if (pae.getException() instanceof InstantiationException) {
                throw new ClassNotFoundException(factoryClassName[0] + " could not be instantiated");
            } else if (pae.getException() instanceof IllegalAccessException) {
                throw new ClassNotFoundException("Illegal access to " + factoryClassName);
            }
            throw new PolicyContextException(pae.getException());
        }

        return policyConfigurationFactory;
    }

    /**
     * This method is used to obtain an instance of the provider specific class
     * that implements the PolicyConfiguration interface that corresponds to
     * the identified policy context within the provider. The methods of the
     * PolicyConfiguration interface are used to define the policy statements
     * of the identified policy context.<p>
     *
     * If at the time of the call, the identified policy context does not exist
     * in the provider, then the policy context will be created in the provider
     * and the Object that implements the context's PolicyConfiguration
     * Interface will be returned. If the state of the identified context is
     * "deleted" or "inService" it will be transitioned to the "open" state as
     * a result of the call. The states in the lifecycle of a policy context
     * are defined by the PolicyConfiguration interface.<p>
     *
     * For a given value of policy context identifier, this method must always
     * return the same instance of PolicyConfiguration and there must be at
     * most one actual instance of a PolicyConfiguration with a given policy
     * context identifier (during a process context). <p>
     *
     * To preserve the invariant that there be at most one PolicyConfiguration
     * object for a given policy context, it may be necessary for this method
     * to be thread safe.
     * @param contextID A String identifying the policy context whose
     * PolicyConfiguration interface is to be returned. The value passed to
     * this parameter must not be null.
     * @param remove A boolean value that establishes whether or not the policy
     * statements of an existing policy context are to be removed before its
     * PolicyConfiguration object is returned. If the value passed to this
     * parameter is true, the policy statements of an existing policy context
     * will be removed. If the value is false, they will not be removed.
     * @return an Object that implements the PolicyConfiguration Interface
     * matched to the Policy provider and corresponding to the identified
     * policy context.
     * @throws PolicyContextException if the implementation throws a checked
     * exception that has not been accounted for by the getPolicyConfiguration
     * method signature. The exception thrown by the implementation class will
     * be encapsulated (during construction) in the thrown PolicyContextException.
     */
    public abstract javax.security.jacc.PolicyConfiguration getPolicyConfiguration(String contextID, boolean remove) throws PolicyContextException;

    /**
     * This method determines if the identified policy context exists with
     * state "inService" in the Policy provider associated with the factory.
     * @param contextID A string identifying a policy context
     * @return true if the identified policy context exists within the provider
     * and its state is "inService", false otherwise.
     * @throws PolicyContextException if the implementation throws a checked
     * exception that has not been accounted for by the inService method
     * signature. The exception thrown by the implementation class will be
     * encapsulated (during construction) in the thrown PolicyContextException.
     */
    public abstract boolean inService(String contextID) throws PolicyContextException;
}
