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
package org.apache.geronimo.kernel.repository;

/**
 * An interface for getting notifications on the progress of file writes.
 *
 * @version $Rev$ $Date$
 */
public interface FileWriteMonitor {
    /**
     * @param fileSize If it's known ahead of time, this is the total size of
     *                 the file to be written.  This would typically be the
     *                 case for a copy operation, for example, but not
     *                 necessarily for a download.  If the file size is not
     *                 known, this will be set to a negative number.
     */
    public void writeStarted(String fileDescription, int fileSize);

    /**
     * The running count of bytes written.
     */
    public void writeProgress(int bytes);

    /**
     * Indicates that the write completed with the specified number
     * of total bytes.
     */
    public void writeComplete(int bytes);
}
