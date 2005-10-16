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

package org.apache.geronimo.common;


/**
 * @version $Rev$ $Date$
 */
public class DeploymentException extends Exception {

    public DeploymentException() {
    }

    public DeploymentException(Throwable cause) {
        super(cause);
    }

    public DeploymentException(String message) {
        super(message);
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public DeploymentException cleanse() {
        if(null != getCause()) {
            Throwable root = this;
            CleanseException previousEx = null;
            CleanseException rootEx = null;
            while (null != root) {
                Throwable e = root.getCause();
                CleanseException exception = new CleanseException(root.getMessage(), root.toString());
                if (null == rootEx) {
                    rootEx = exception;
                }
                exception.setStackTrace(root.getStackTrace());
                if (null != previousEx) {
                    previousEx.initCause(exception);
                }
                previousEx = exception;
                root = e;
            }
            return rootEx;
        }

        return this;
    }
    
    private static class CleanseException extends DeploymentException {
        private final String toString;
        
        public CleanseException(String message, String toString) {
            super(message);
            this.toString = toString;
        }
        
        public String toString() {
            return toString;
        }
    }
}
