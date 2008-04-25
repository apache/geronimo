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
package org.apache.geronimo.corba.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.corba.CORBABean;


/**
 * OpenORB specific startup GBean
 *
 * @version $Revision: 451417 $ $Date: 2006-09-29 13:13:22 -0700 (Fri, 29 Sep 2006) $
 */
public class OpenORBUtil implements GBeanLifecycle {

    private final Logger log = LoggerFactory.getLogger(OpenORBUtil.class);

    private final CORBABean server;

    public OpenORBUtil() {
        server = null;
    }

    public OpenORBUtil(CORBABean server) {
        this.server = server;
    }

    public CORBABean getServer() {
        return server;
    }

    public void doStart() throws Exception {

//        DefaultORB.setORB(server.getORB());

        log.debug("Started OpenORBUtil");
    }

    public void doStop() throws Exception {
        log.debug("Stopped OpenORBUtil");
    }

    public void doFail() {
        log.warn("Failed OpenORBUtil");
    }

}
