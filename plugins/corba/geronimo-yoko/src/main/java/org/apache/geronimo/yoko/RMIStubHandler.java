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

import org.apache.geronimo.corba.CorbaApplicationServer;
import org.apache.openejb.core.ServerFederation;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.yoko.rmi.impl.MethodDescriptor;
import org.apache.yoko.rmi.impl.RMIStub;

/**
 * This class is the InvocationHandler for instances of POAStub. When a client
 * calls a remote method, this is translated to a call to the invoke() method in
 * this class.
 */
public class RMIStubHandler extends org.apache.yoko.rmi.impl.RMIStubHandler {
    // the application server singleton
    private static CorbaApplicationServer corbaApplicationServer = new CorbaApplicationServer();

    public Object invoke(RMIStub stub, MethodDescriptor method, Object[] args) throws Throwable {
        // object types must bbe written in the context of the corba application server
        // which properly write replaces our objects for corba
        ApplicationServer oldApplicationServer = ServerFederation.getApplicationServer();

        ServerFederation.setApplicationServer(corbaApplicationServer);

        try {
            // let the super class handle everything.  We just need to wrap the context
            return super.invoke(stub, method, args);

        } finally {
            ServerFederation.setApplicationServer(oldApplicationServer);
        }
    }

}

