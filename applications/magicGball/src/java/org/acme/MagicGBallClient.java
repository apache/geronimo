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

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

/**
 * Run this app client from the command line and pass in a question as an argument.
 *
 * @version $Rev: 46301 $ $Date: 2004-09-18 13:03:59 -0700 (Sat, 18 Sep 2004) $
 */
public class MagicGBallClient {
    public static void main(String[] args) throws Exception{
        if (args.length < 1){
            System.err.println("Please ask a question");
            System.exit(-1);
        }

        MagicGBallClient magicGBallClient = new MagicGBallClient();
        for (int i = 0; i < args.length; i++) {
            magicGBallClient.ask(args[i]);
        }
    }

	public void ask(String question) throws Exception {
        Context ctx = new InitialContext();
        Object o = ctx.lookup("java:comp/env/mGball");
        MagicGBallHome ejbHome = (MagicGBallHome) PortableRemoteObject.narrow(o, MagicGBallHome.class);
        MagicGBall mGball = ejbHome.create();
        String answer = mGball.ask(question);

        System.out.println(answer);
	}

}
