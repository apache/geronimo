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

package org.apache.geronimo.messaging;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Encapsulates a method invocation.
 *
 * @version $Revision: 1.2 $ $Date: 2004/06/01 13:37:14 $
 */
public class Request
    implements Externalizable
{

    /**
     * Target of the invocation.
     */
    private transient Object target;
    
    /**
     * Name of the method defined by target to be executed.
     */
    private String methodName;
    
    /**
     * Method formal parameters.
     */
    private Class[] parameterTypes;
    
    /**
     * Parameters of methodName. 
     */
    private Object[] parameters;
    
    /**
     * Required for Externalization.
     */
    public Request(){}
    
    /**
     * Wraps a method having the specified name and parameters.
     * 
     * @param aMethodName Method name.
     * @param anArrOfParamTypes Method formal parameters.
     * @param anArrOfParams Parameters.
     */
    public Request(String aMethodName, Class[] anArrOfParamTypes,
        Object[] anArrOfParams) {
        methodName = aMethodName;
        parameterTypes = anArrOfParamTypes;
        parameters = anArrOfParams;
    }
    
    /**
     * Sets the target against which the method is to be executed.
     * @param aTarget
     */
    public void setTarget(Object aTarget) {
        target = aTarget;
    }

    /**
     * Gets the method name to be executed.
     * 
     * @return Returns the methodName.
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Gets the formal parameter of the method wrapped by this instance. 
     * 
     * @return Formal parameters.
     */
    public Class[] getParameterTypes() {
        return parameterTypes;
    }
    
    /**
     * Gets the parameters of the method to be executed.
     * 
     * @return Returns the parameters.
     */
    public Object[] getParameters() {
        return parameters;
    }
    
    /**
     * Executes the command against the specified target.
     * 
     * @return CommandResult wrapping the invocation result. if the command
     * does not exist for the specified target, then the CommandResult instance
     * contains a NoSuchMethodException exception.
     */
    public Result execute() {
        Class clazz = target.getClass();
        try {
            Method method = clazz.getMethod(methodName, parameterTypes);
            Object opaque = method.invoke(target, parameters);
            return new Result(true, opaque);
        } catch (NoSuchMethodException e) {
            return new Result(false, e);
        } catch (InvocationTargetException e) {
            return new Result(false, e.getCause());
        } catch (Throwable e) {
            return new Result(false, e);
        }
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF(methodName);
        out.writeObject(parameterTypes);
        out.writeObject(parameters);
    }

    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        methodName = in.readUTF();
        parameterTypes = (Class[]) in.readObject();
        parameters = (Object[]) in.readObject();
    }

}
