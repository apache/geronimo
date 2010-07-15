/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
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
package org.apache.geronimo.tomcat.interceptor;

import org.apache.catalina.tribes.group.ChannelInterceptorBase; 
import org.apache.catalina.tribes.ChannelException; 
import org.apache.catalina.tribes.Channel;

/*
This class disables multicast in a tomcat cluster configuration.
It's used in conjunction with a unicast configuration involving
static members.  Once the ability to disable multicast is exposed
in tomcat, this class be dropped.
*/

/**
 * @version $Rev$ $Date$
 */
public class DisableMcastInterceptor extends ChannelInterceptorBase { 
    
    public DisableMcastInterceptor() { 
        super(); 
    }

public void start(int svc) throws ChannelException { 
    svc = (svc & (~Channel.MBR_TX_SEQ) & (~Channel.MBR_RX_SEQ));
    super.start(svc);
    } 
} 