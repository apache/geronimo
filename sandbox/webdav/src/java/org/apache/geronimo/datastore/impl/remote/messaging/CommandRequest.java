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

package org.apache.geronimo.datastore.impl.remote.messaging;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/03/03 13:10:07 $
 */
public class CommandRequest
    implements Serializable
{

    private static final Log log = LogFactory.getLog(CommandRequest.class);
    
    private transient Object target;
    private final String methodName;
    private final Object[] parameters;
    
    public CommandRequest(String aMethodName, Object[] anArrOfParams) {
        methodName = aMethodName;
        parameters = anArrOfParams;
    }
    
    public void setTarget(Object aTarget) {
        target = aTarget;
    }
    
    public CommandResult execute() {
        Class clazz = target.getClass();
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if ( method.getName().equals(methodName) ) {
                return invokeMethod(method);
            }
        }
        return new CommandResult(false,
            new OperationNotSupportedException("Method {" + methodName + 
                "} does not exist."));
    }
    
    private CommandResult invokeMethod(Method aMethod) {
        CommandResult result;
        try {
            Object opaque = aMethod.invoke(target, parameters);
            return new CommandResult(true, opaque);
        } catch (IllegalArgumentException e) {
            return new CommandResult(false, e);
        } catch (IllegalAccessException e) {
            return new CommandResult(false, e);
        } catch (InvocationTargetException e) {
            return new CommandResult(false, e);
        }
    }

}
