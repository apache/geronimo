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
package org.apache.geronimo.axis.client;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;

import javax.security.auth.Subject;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.axis.client.Call;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.UsernamePasswordCredential;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ServiceEndpointMethodInterceptor implements MethodInterceptor{

    private final GenericServiceEndpoint stub;
    private final OperationInfo[] operations;

    public ServiceEndpointMethodInterceptor(GenericServiceEndpoint stub, OperationInfo[] operations) {
        this.stub = stub;
        this.operations = operations;
    }

    public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
        int index = methodProxy.getSuperIndex();
        OperationInfo operationInfo = operations[index];
        if (operationInfo == null) {
            throw new RuntimeException("Operation not mapped: " + method.getName() + " index: " + index + "\n OperationInfos: " + Arrays.asList(operations));
        }
        stub.checkCachedEndpoint();

        Call call = stub.createCall();

        operationInfo.prepareCall(call);

        stub.setUpCall(call);
        Subject subject = ContextManager.getNextCaller();
        if (subject == null) {
            //is this an error?
        } else {
            Set creds = subject.getPrivateCredentials(UsernamePasswordCredential.class);
            if (creds.size() != 1) {
                throw new SecurityException("Non-unique UsernamePasswordCredential, count: " + creds.size());
            }
            UsernamePasswordCredential usernamePasswordCredential = (UsernamePasswordCredential) creds.iterator().next();
            call.setUsername(usernamePasswordCredential.getUsername());
            call.setPassword(new String(usernamePasswordCredential.getPassword()));
        }
        java.lang.Object response = call.invoke(objects);

        if (response instanceof java.rmi.RemoteException) {
            throw (java.rmi.RemoteException)response;
        }
        else {
            stub.extractAttachments(call);
            Class returnType = operationInfo.getOperationDesc().getReturnClass();
            if (response == null || returnType.isAssignableFrom(response.getClass())) {
                return response;
            } else {
                return org.apache.axis.utils.JavaUtils.convert(response, returnType);
            }
        }
    }

}
