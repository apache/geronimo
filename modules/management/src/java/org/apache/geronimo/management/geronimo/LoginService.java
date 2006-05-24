/**
 *
 * Copyright 2005 The Apache Software Foundation
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
package org.apache.geronimo.management.geronimo;

import java.util.Collection;

/**
 * A very basic sketch of a login service.
 *
 * The meat of this is in a Geronimo-specific extension, but it's not yet clear
 * how much of that should be moved here to be "portable".
 *
 * @version $Rev: 46019 $ $Date: 2004-09-14 05:56:06 -0400 (Tue, 14 Sep 2004) $
 */
public interface LoginService {
    /**
     * Return the object name of this login service.
     *
     * @return the object name of this service
     */
    public String getObjectName();

    public Collection getRealms();

    public void setRealms(Collection realms);

    public int getMaxLoginDurationMillis();

    public void setMaxLoginDurationMillis(int maxLoginDurationMillis);

    public int getExpiredLoginScanIntervalMillis();

    public void setExpiredLoginScanIntervalMillis(int expiredLoginScanIntervalMillis);
}
