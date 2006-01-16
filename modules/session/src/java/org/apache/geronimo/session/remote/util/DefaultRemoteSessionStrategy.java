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
package org.apache.geronimo.session.remote.util;

import org.apache.geronimo.session.SessionLocation;

import java.util.Map;

/**
 * A default strategy that uses simple watermarks for deciding when to redirect,
 * then proxy, then move. Requests will be redirected by default up to a low
 * watermark, then they will be proxied until they reach the high watermark and
 * then the session will be moved.
 * 
 * @version $Revision: $
 */
public class DefaultRemoteSessionStrategy implements RemoteSessionStrategy {

    private final Map cache;
    private boolean redirectSupported = true;
    private int proxyWatermark = 5;
    private int moveWatermark = 10;

    public DefaultRemoteSessionStrategy() {
        this(new LRUCache(4000));
    }

    public DefaultRemoteSessionStrategy(Map cache) {
        this.cache = cache;
    }

    public int decide(SessionLocation location) {
        int count = getRecentRequestCount(location.getSessionId());
        if (redirectSupported) {
            if (count < proxyWatermark) {
                return REDIRECT;
            }
        }
        if (count < moveWatermark) {
            return PROXY;
        }
        return MOVE;
    }

    // Properties
    // -------------------------------------------------------------------------
    public boolean isRedirectSupported() {
        return redirectSupported;
    }

    /**
     * Sets whether or not redirection should be supported at all. It is by
     * default but you may wish to disable this if you have a non-smart client
     * which is not capable of redirection.
     */
    public void setRedirectSupported(boolean redirectSupported) {
        this.redirectSupported = redirectSupported;
    }

    public int getMoveWatermark() {
        return moveWatermark;
    }

    /**
     * Sets the low watermark of requests in the recent history of requests for
     * the same session Id which will cause the session to be moved locally.
     */
    public void setMoveWatermark(int moveWatermark) {
        this.moveWatermark = moveWatermark;
    }

    public int getProxyWatermark() {
        return proxyWatermark;
    }

    /**
     * Sets the low watermark of requests in the recent history of requests for
     * the same session Id which will cause the session to be proxied to the
     * remote session.
     */
    public void setProxyWatermark(int redirectWatermark) {
        this.proxyWatermark = redirectWatermark;
    }

    // Implementation methods
    // -------------------------------------------------------------------------
    protected synchronized int getRecentRequestCount(String sessionId) {
        Counter counter = (Counter) cache.get(sessionId);
        if (counter == null) {
            counter = new Counter();
            cache.put(sessionId, counter);
        }
        return ++counter.value;
    }

    protected static class Counter {
        public int value;
    }
}
