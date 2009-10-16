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
package org.apache.geronimo.deployment.plugin.remote;

import java.io.File;

import org.apache.geronimo.deployment.plugin.local.AbstractDeployCommand;

/**
 * Knows how to upload files to a server
 *
 * @version $Rev$ $Date$
 */
public class RemoteDeployUtil {

    public static void uploadFilesToServer(File[] files, AbstractDeployCommand command) {
        FileUploadClient uploadServletClient = new FileUploadServletClient();
        try {
            uploadServletClient.uploadFilesToServer(command.getRemoteDeployUploadURL(), 
                command.getCommandContext().getUsername(),
                command.getCommandContext().getPassword(),
                files,
                new FileUploadServletProgressAdapter(command));
        } catch (Exception e) {
            command.doFail(e);
        }
    }

    protected static class FileUploadServletProgressAdapter implements FileUploadProgress {
        private final AbstractDeployCommand command;
        
        public FileUploadServletProgressAdapter(AbstractDeployCommand command) {
            this.command = command;
        }

        public void fail(String message) {
            command.fail(message);
        }

        public void fail(Exception exception) {
            command.doFail(exception);
        }

        public void updateStatus(String message) {
            command.updateStatus(message);
        }

    }
}
