/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.rmi.iiop.client;

import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.geronimo.interop.SystemException;
import org.apache.geronimo.interop.util.ListUtil;


public class HostList {
    private static ArrayList EMPTY_LIST = new ArrayList(0);

    private int _connectCount;

    private boolean _firstConnectAll = true;

    private int _cacheTimeout = 600; // 600 seconds = 10 minutes

    private int _preferredIndex;

    private int _alternateIndex;

    private ArrayList _preferredServers = EMPTY_LIST;

    private ArrayList _alternateServers = EMPTY_LIST;

    public HostList(String hostList) {
        try {
            int cycle = -1;
            for (StringTokenizer st = new StringTokenizer(hostList, ";"); st.hasMoreTokens();) {
                String token = st.nextToken().trim();
                if (token.startsWith("cycle=")) {
                    String value = token.substring(6);
                    cycle = Integer.parseInt(value);
                } else if (token.startsWith("cacheTimeout=")) {
                    String value = token.substring(6);
                    _cacheTimeout = Integer.parseInt(value);
                } else if (token.startsWith("preferredServers=")) {
                    String value = token.substring(17);
                    _preferredServers = ListUtil.getCommaSeparatedList(value);
                } else if (token.startsWith("alternateServers=")) {
                    String value = token.substring(17);
                    _alternateServers = ListUtil.getCommaSeparatedList(value);
                }
                // Otherwise ignore for forwards compabitility
            }
            if (cycle < 0) {
                cycle = 0; //FastRandom.getSharedInstance().nextInt(0, 999999999);
            }
            if (_preferredServers.size() > 0) {
                _preferredIndex = cycle % _preferredServers.size();
            }
            if (_alternateServers.size() > 0) {
                _alternateIndex = cycle % _alternateServers.size();
            }
        } catch (Exception ex) {
            throw new SystemException("hostList = " + hostList, ex);
        }
    }

    public boolean connectAll() {
        synchronized (this) {
            return _connectCount >= 5; // TODO: make this configurable
        }
    }

    public void countConnect() {
        synchronized (this) {
            int n = _connectCount;
            if (n < Integer.MAX_VALUE) {
                _connectCount = ++n;
            }
        }
    }

    public int getCacheTimeout() {
        return _cacheTimeout;
    }

    public int getPreferredIndex() {
        synchronized (this) {
            int nextIndex = _preferredIndex;
            if (connectAll()) {
                int n = _preferredServers.size();
                if (n > 0) {
                    _preferredIndex = (nextIndex + 1) % n;
                }
            }
            return nextIndex;
        }
    }

    public int getAlternateIndex() {
        synchronized (this) {
            int nextIndex = _alternateIndex;
            if (connectAll()) {
                int n = _alternateServers.size();
                if (n > 0) {
                    _alternateIndex = (nextIndex + 1) % n;
                }
            }
            return nextIndex;
        }
    }

    public ArrayList getPreferredServers() {
        return _preferredServers;
    }

    public ArrayList getAlternateServers() {
        return _alternateServers;
    }

    public String toString() {
        return "HostList:cacheTimeout=" + _cacheTimeout
               + ";preferredServers=" + ListUtil.formatCommaSeparatedList(_preferredServers)
               + ";alternateServers=" + ListUtil.formatCommaSeparatedList(_alternateServers);
    }
}
