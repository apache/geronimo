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

package org.apache.geronimo.common;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintStream;
import java.io.PrintWriter;


/**
 * @version $Rev$ $Date$
 */
public class DeploymentException extends Exception {

    private final List<? extends Throwable> causes;

    public DeploymentException() {
        causes = null;
    }

    public DeploymentException(Throwable cause) {
        super(cause);
        causes = null;
    }

    public DeploymentException(String message) {
        super(message);
        causes = null;
    }

    public DeploymentException(String message, Throwable cause) {
        super(message, cause);
        causes = null;
    }

    public DeploymentException(String message, List<? extends Throwable> causes) {
        super(message);
        this.causes = causes;
    }

    public void printStackTrace(PrintStream ps) {
        super.printStackTrace(ps);
        if (causes != null) {
            for (Throwable cause: causes) {
                //TODO trim duplicate stack trace elements
                cause.printStackTrace(ps);
            }
        }
    }

    public void printStackTrace(PrintWriter pw) {
        super.printStackTrace(pw);
        if (causes != null) {
            for (Throwable cause: causes) {
                //TODO trim duplicate stack trace elements
                cause.printStackTrace(pw);
            }
        }
    }
    
    public DeploymentException cleanse() {
        if(null != getCause()) {
            return cleanse(this);
        }
        if (causes != null) {
            List<CleanseException> cleansedCauses = new ArrayList<CleanseException>(causes.size());
            for (Throwable cause: causes) {
                CleanseException cleansed = cleanse(cause);
                cleansedCauses.add(cleansed);
            }
            return new DeploymentException(getMessage(), cleansedCauses);
        }
        return this;
    }

    protected static CleanseException cleanse(Throwable root) {
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
