/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba.security.config.tss;

import javax.security.auth.Subject;
import java.io.Serializable;

import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.CSIIOP.AS_ContextSec;
import org.omg.IOP.Codec;

import org.apache.geronimo.corba.security.SASException;


/**
 * @version $Rev: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public abstract class TSSASMechConfig implements Serializable {

    public abstract short getSupports();

    public abstract short getRequires();

    public abstract AS_ContextSec encodeIOR(ORB orb, Codec codec) throws Exception;

    public static TSSASMechConfig decodeIOR(AS_ContextSec context) {
        TSSASMechConfig result = null;

        if (context.target_supports == 0) {
            result = new TSSNULLASMechConfig();
        } else {
            result = new TSSGSSUPMechConfig(context);
        }

        return result;
    }

    public abstract Subject check(EstablishContext msg) throws SASException;

    public abstract void toString(String spaces, StringBuffer buf);

}
