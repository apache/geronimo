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

package org.apache.geronimo.tomcat.valve;

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.valves.ValveBase;
import org.apache.geronimo.security.Callers;
import org.apache.geronimo.security.ContextManager;

/**
 *  A hacker valve used to clean up the security association before returning the thread to the pool.
 *  Please refer to GERONIMO-4748 for details
 *  @version $Rev$ $Date$ 
 */
public class ThreadCleanerValve extends ValveBase {

    public void invoke(Request request, Response response) throws IOException, ServletException {
        Callers oldCallers = ContextManager.getCallers();
        try {
            getNext().invoke(request, response);
        } finally {
            ContextManager.popCallers(oldCallers);
        }
    }
}
