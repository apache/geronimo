/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
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

package org.apache.geronimo.deployment.cli;

import org.apache.geronimo.common.DeploymentException;

/**
 * Something was wrong with the user's syntax (as opposed to the command
 * failed for some other reason).
 * 
 * @version $Rev: 53762 $ $Date: 2004-10-04 18:54:53 -0400 (Mon, 04 Oct 2004) $
 */
public class DeploymentSyntaxException extends DeploymentException {
    public DeploymentSyntaxException() {
    }

    public DeploymentSyntaxException(Throwable cause) {
        super(cause);
    }

    public DeploymentSyntaxException(String message) {
        super(message);
    }

    public DeploymentSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }
}
