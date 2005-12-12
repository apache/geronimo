/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.testframework;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

class VMControllerImpl extends UnicastRemoteObject implements VMController {
    /**
     * 
     */
    private final RemoteTest remoteTest;

    public VMControllerImpl(RemoteTest remoteTest) throws RemoteException {
        super();

        this.remoteTest = remoteTest;
    }

    public void invoke(String methodName) throws Exception {
        try {
            Method method = this.remoteTest.getClass().getMethod(methodName,null);
            RemoteTestUtil.invokeAndRethrow(this.remoteTest, method);
        }
        catch(Throwable t) {
            throw new InvocationTargetException(t);
        }
    }

    public void exit(int code) throws Exception {
        System.out.println("Exiting VM peacefully");
        synchronized(terminateMonitor) {
            terminateMonitor.notify();
        }
    }
    
    public Object terminateMonitor = new Object();
}
