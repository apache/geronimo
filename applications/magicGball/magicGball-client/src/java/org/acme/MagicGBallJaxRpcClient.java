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
package org.acme;

import java.net.URL;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;

public class MagicGBallJaxRpcClient {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Please ask a question");
            System.exit(-1);
        }

        try {
            URL wsdlURL = new URL("http://localhost:8000/services/MagicGBall?wsdl");
            String namespaceURI = "http://acme.org/magicGball";
            QName serviceQName = new QName(namespaceURI, "MagicGBallService");
            QName portQName = new QName(namespaceURI, "MagicGBallPort");

            ServiceFactory serviceFactory = ServiceFactory.newInstance();
            Service service = serviceFactory.createService(wsdlURL, serviceQName);
            MagicGBallEndpoint mGball = (MagicGBallEndpoint) service.getPort(portQName, MagicGBallEndpoint.class);

            for (int i = 0; i < args.length; i++) {
                String question = args[i];
                String answer = mGball.ask(question);
                System.out.println(answer);
            }
        } catch (Exception e) {
            System.err.println(e.toString());
        }
    }
}
