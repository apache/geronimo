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

public class DownloadInfo
{
    public static final String DOWNLOAD_INFO_KEY = "downloadInfo";
    private boolean downloadStarted = false;
    private boolean downloadFinished = false;
    private long bytesDownloaded = 0;
    private long totalBytes = -1;
    
    public DownloadInfo()
    {
    }
    public boolean isDownloadFinished() {
        return downloadFinished;
    }
    public void setDownloadFinished(boolean downloadFinished) {
        this.downloadFinished = downloadFinished;
    }
    public boolean isDownloadStarted() {
        return downloadStarted;
    }
    public void setDownloadStarted(boolean downloadStarted) {
        this.downloadStarted = downloadStarted;
    }
    public long getBytesDownloaded() {
        return bytesDownloaded;
    }
    public void setBytesDownloaded(long bytesDownloaded) {
        this.bytesDownloaded = bytesDownloaded;
    }
    public long getTotalBytes() {
        return totalBytes;
    }
    public void setTotalBytes(long totalBytes) {
        this.totalBytes = totalBytes;
    }
}

