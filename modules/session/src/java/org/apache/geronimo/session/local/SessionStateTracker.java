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
package org.apache.geronimo.session.local;

import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;

import java.util.Map;
import java.util.Set;

/**
 * A strategy to decide how to aggregate changes to the sessions together into
 * complete units of work so that when checkpointed, the changes are replicated
 * to some backup.
 * 
 * This class by default uses deltas where possible, though it would be possible
 * to change this class to never use deltas if required; or to suspend
 * checkpoints until later on etc.
 * 
 * @version $Revision: $
 */
public class SessionStateTracker {

    private final LocalSessionLocation location;
    private final SessionStateListener stateListener;
    private final SessionImpl session;
    private Set deltaKeys = new CopyOnWriteArraySet();
    private boolean change;
    private boolean destroy;

    public SessionStateTracker(LocalSessionLocation location, SessionImpl session, SessionStateListener listener) {
        this.location = location;
        this.session = session;
        this.stateListener = listener;
    }

    /**
     * When the session decides to broadcast the entire state; often done after
     * a move
     */
    public void onChange() {
        change = true;
        deltaKeys.clear();
    }

    /**
     * Notifies only the changed entry in the Session
     */
    public void onDeltaChange(String key) {
        deltaKeys.add(key);
    }

    /**
     * Broadcasts that the session has been removed
     */
    public void onDestroy() {
        destroy = true;
        deltaKeys.clear();
    }

    /**
     * Checkpoints any pending state changes
     */
    public void checkpoint() {
        if (stateListener != null) {
            String sessionId = location.getSessionId();
            if (change) {
                Map state = session.getStateMap();
                stateListener.onCompleteChange(sessionId, state);
                change = false;
            }
            else if (destroy) {
                stateListener.onDestroy(sessionId);
                destroy = false;
            }
            else {
                Map deltaState = session.createDeltas(deltaKeys);
                stateListener.onDeltaChange(sessionId, deltaState);
                deltaKeys.clear();
            }
        }
    }

}
