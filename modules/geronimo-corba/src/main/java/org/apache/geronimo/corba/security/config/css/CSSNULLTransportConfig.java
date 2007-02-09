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
package org.apache.geronimo.corba.security.config.css;

import org.apache.geronimo.corba.security.config.tss.TSSTransportMechConfig;


/**
 * @version $Revision: 503274 $ $Date: 2007-02-03 10:19:18 -0800 (Sat, 03 Feb 2007) $
 */
public class CSSNULLTransportConfig implements CSSTransportMechConfig {

    private short supports;
    private short requires;

    public short getSupports() {
        return supports;
    }

    public short getRequires() {
        return requires;
    }

    public boolean canHandle(TSSTransportMechConfig transMech) {
        if ((supports & transMech.getRequires()) != transMech.getRequires()) return false;
        if ((requires & transMech.getSupports()) != requires) return false;

        return true;
    }

    public void toString(String spaces, StringBuffer buf) {
        buf.append(spaces).append("CSSNULLTransportConfig\n");
    }
}
