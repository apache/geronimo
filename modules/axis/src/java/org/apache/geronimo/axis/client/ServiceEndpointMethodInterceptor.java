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
import java.util.Iterator;
import java.util.Set;
import javax.security.auth.Subject;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.apache.axis.client.Call;
import org.apache.geronimo.security.ContextManager;
import org.apache.geronimo.security.jaas.NamedUsernamePasswordCredential;

/**
 * @version $Rev:  $ $Date:  $
 */
public class ServiceEndpointMethodInterceptor implements MethodInterceptor{

    private final GenericServiceEndpoint stub;
    private final OperationInfo[] operations;
    private final String credentialsName;

    public ServiceEndpointMethodInterceptor(GenericServiceEndpoint stub, OperationInfo[] operations, String credentialsName) {
        this.stub = stub;
        this.operations = operations;
        this.credentialsName = credentialsName;
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
        if (credentialsName != null) {
            Subject subject = ContextManager.getCurrentCaller();
            if (subject == null) {
                throw new IllegalStateException("Subject missing but authentication turned on");
            } else {
                Set creds = subject.getPrivateCredentials(NamedUsernamePasswordCredential.class);
                boolean found = false;
                for (Iterator iterator = creds.iterator(); iterator.hasNext();) {
                    NamedUsernamePasswordCredential namedUsernamePasswordCredential = (NamedUsernamePasswordCredential) iterator.next();
                    if (credentialsName.equals(namedUsernamePasswordCredential.getName())) {
                        call.setUsername(namedUsernamePasswordCredential.getUsername());
                        call.setPassword(new String(namedUsernamePasswordCredential.getPassword()));
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    throw new IllegalStateException("no NamedUsernamePasswordCredential found for name "  + credentialsName);
                }
            }
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
