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

package org.apache.geronimo.datastore.impl.remote.datastore;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class ProxyCommand
    implements Serializable, CommandWithProxy
{

    private static final Log log = LogFactory.getLog(CommandWithProxy.class);
    
    private static final Map PROXY_METHODS;
    
    static {
        Map tmpMethods = new HashMap();
        Class clazz = GFileManagerProxy.class;
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            tmpMethods.put(method.getName(), method);
        }
        PROXY_METHODS = tmpMethods;
    }
    
    private transient GFileManagerProxy proxy;

    private final String methodName;
    private final Object[] parameters;
    
    public ProxyCommand(String aMethodName, Object[] anArrOfParams) {
        methodName = aMethodName;
        parameters = anArrOfParams;
    }
    
    public CommandResult execute() {
        Class clazz = proxy.getClass();
        Method method = (Method) PROXY_METHODS.get(methodName);
        if ( null != method ) {
            return invokeMethod(method);
        }
        return new CommandResult(false,
            new OperationNotSupportedException("Method {" + methodName + 
                "} does not exist."));
    }
    
    public void setProxyGFileManager(GFileManagerProxy aProxy) {
        proxy = aProxy;
    }

    private CommandResult invokeMethod(Method aMethod) {
        CommandResult result;
        try {
            Object opaque = aMethod.invoke(proxy, parameters);
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
