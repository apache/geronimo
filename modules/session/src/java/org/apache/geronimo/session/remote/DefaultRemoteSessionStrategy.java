/*
 * Copyright 2005-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.session.remote;

import org.apache.geronimo.session.SessionLocation;

/**
 * 
 * @version $Revision: $
 */
public class DefaultRemoteSessionStrategy {

    public static final int REDIRECT = 1;
    public static final int PROXY = 2;
    public static final int MOVE = 3;

    private boolean redirectSupported;
    private int redirectWatermark = 5;
    private int moveWatermark = 10;

    public int decide(SessionLocation location) {
        int count = getRecentRequestCount(location.getSessionId());
        if (redirectSupported) {
            if (count < redirectWatermark) {
                return REDIRECT;
            }
        }
        if (count < moveWatermark) {
            return PROXY;
        }
        return MOVE;
    }

    private int getRecentRequestCount(String sessionId) {
        return 0;
    }
}
