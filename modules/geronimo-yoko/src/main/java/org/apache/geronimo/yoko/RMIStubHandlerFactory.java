/**
*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  See the NOTICE file distributed with
*  this work for additional information regarding copyright ownership.
*  The ASF licenses this file to You under the Apache License, Version 2.0
*  (the "License"); you may not use this file except in compliance with
*  the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/ 

package org.apache.geronimo.yoko;

/**
 * Implementation of the yoko StubInitializer class to provide instances 
 * of RMIStubHandlers to Stub instances. 
 */
public class RMIStubHandlerFactory implements org.apache.yoko.rmi.util.stub.StubInitializer {
    public RMIStubHandlerFactory() {
    }
    
    /**
     * Provide an RMIStub instance with an RMIStubHandler instance.  This version
     * instantiates a new handler for each stub instance because the handler 
     * needs to be initialized with the appropriate execution context. 
     * 
     * @return An instance of StubHandler that hooks the RMI stub invocation into the openejb
     *         context.
     */
    public Object getStubHandler() {
        return new RMIStubHandler(); 
    }
}

