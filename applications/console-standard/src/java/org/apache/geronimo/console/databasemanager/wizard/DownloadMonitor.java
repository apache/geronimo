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

package org.apache.geronimo.console.databasemanager.wizard;

import javax.servlet.http.HttpSession;

import uk.ltd.getahead.dwr.ExecutionContext;

public class DownloadMonitor
{
    public DownloadInfo getDownloadInfo()
    {
        HttpSession session = ExecutionContext.get().getSession();
        
        if (session.getAttribute(DownloadInfo.DOWNLOAD_INFO_KEY) != null) {
            return (DownloadInfo) session.getAttribute(DownloadInfo.DOWNLOAD_INFO_KEY);
        } else {
            return new DownloadInfo();
        }
    }
}

