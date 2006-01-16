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

import java.util.Map;

/**
 * A listener to be notified of changes in a session state so that it can be
 * stored in some backup such as a file, a database or a buddy group.
 * 
 * @version $Revision: $
 */
public interface SessionStateListener {

    /**
     * Notifies a complete change of the state; typically when a session moves
     */
    void onCompleteChange(String sessionId, Map state);

    /**
     * Notifies a set of deltas have changed, only a fraction of the complete
     * session state
     */
    void onDeltaChange(String sessionId, Map deltas);

    /**
     * The session has been destroyed so remove all state for this session Id
     * 
     * @param sessionId
     */
    void onDestroy(String sessionId);

}
