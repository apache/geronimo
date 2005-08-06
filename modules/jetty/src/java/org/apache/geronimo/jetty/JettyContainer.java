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

package org.apache.geronimo.jetty;

import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpListener;
import org.mortbay.http.RequestLog;
import org.mortbay.http.UserRealm;
import org.apache.geronimo.j2ee.management.geronimo.WebContainer;

/**
 * @version $Rev$ $Date$
 */
public interface JettyContainer extends WebContainer {
    void addListener(HttpListener listener);

    void removeListener(HttpListener listener);

    void addContext(HttpContext context);

    void removeContext(HttpContext context);

    void addRealm(UserRealm realm);

    void removeRealm(UserRealm realm);

    void resetStatistics();

    void setCollectStatistics(boolean on);

    boolean getCollectStatistics();

    long getCollectStatisticsStarted();

    int getConnections();

    int getConnectionsOpen();

    int getConnectionsOpenMax();

    long getConnectionsDurationAve();

    long getConnectionsDurationMax();

    int getConnectionsRequestsAve();

    int getConnectionsRequestsMax();

    int getErrors();

    int getRequests();

    int getRequestsActive();

    int getRequestsActiveMax();

    long getRequestsDurationAve();

    long getRequestsDurationMax();

    void setRequestLog(RequestLog log);

    /* ------------------------------------------------------------ */
    RequestLog getRequestLog();
}
