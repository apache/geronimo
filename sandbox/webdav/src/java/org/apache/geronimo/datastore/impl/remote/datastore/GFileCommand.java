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

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import javax.naming.OperationNotSupportedException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.datastore.GFile;
import org.apache.geronimo.datastore.impl.remote.messaging.GInputStream;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2004/02/25 13:36:15 $
 */
public class GFileCommand
    implements CommandWithProxy
{

    private static final Log log = LogFactory.getLog(CommandWithProxy.class);
    
    private static final Map GFILE_METHODS;
    
    static {
        Map tmpMethods = new HashMap();
        Class clazz = GFile.class;
        Method[] methods = clazz.getMethods();
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            tmpMethods.put(method.getName(), method);
        }
        GFILE_METHODS = tmpMethods;
    }
    
    private transient GFileManagerProxy proxy;

    private final Integer id;
    private final String methodName;
    private final Object[] parameters;
    
    public GFileCommand(Integer anID,
        String aMethodName, Object[] anArrOfParams) {
        id = anID;
        methodName = aMethodName;
        parameters = anArrOfParams;
    }

    public void setProxyGFileManager(GFileManagerProxy aProxy) {
        proxy = aProxy;
    }
    
    public CommandResult execute() {
        GFile gFile = proxy.retrieveGFile(id);
        Method method = (Method) GFILE_METHODS.get(methodName);
        if ( null != method ) {
            return invokeMethod(method, gFile);
        }
        return new CommandResult(false,
            new OperationNotSupportedException("Method {" + methodName + 
                "} does not exist."));
    }

    private CommandResult invokeMethod(Method aMethod, GFile aFile) {
        CommandResult result;
        try {
            Object opaque = aMethod.invoke(aFile, parameters);
            if ( opaque instanceof InputStream ) {
                // If the returned value is an InputStream. One wraps it with
                // a GInputStream, which will encode the InputStream into the
                // stream send back to the requester.
                // TODO migrate to replaceObject and readResolve.
                opaque = new GInputStream((InputStream) opaque);
            }
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
