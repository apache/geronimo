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

package org.apache.geronimo.console.ajax;

import javax.servlet.http.HttpSession;

import org.directwebremoting.WebContextFactory;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;

/**
 * Provides information to an AJAX client during server side
 * activities.
 */
@RemoteProxy
public class ProgressMonitor
{
    @RemoteMethod
    public ProgressInfo getProgressInfo()
    {
        HttpSession session = WebContextFactory.get().getSession(false);

        if (session.getAttribute(ProgressInfo.PROGRESS_INFO_KEY) != null) {
            return (ProgressInfo) session.getAttribute(ProgressInfo.PROGRESS_INFO_KEY);
        } else {
            return new ProgressInfo();
        }
    }
}
