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

package org.apache.geronimo.validator;

/**
 * Used to enumerate validation results.
 *
 * @version $Revision: 1.3 $ $Date: 2004/03/10 09:58:44 $
 */
public class ValidationResult {
    /**
     * Indicates that the validation process was completely successful.  The
     * application module can be deployed and run.
     */
    public static final ValidationResult PASSED = new ValidationResult("Passed");

    /**
     * Indicated that there are some issues the user should be aware of, but
     * in general the application module can be deployed and run.
     */
    public static final ValidationResult PASSED_WITH_WARNINGS = new ValidationResult("Passed with warnings");

    /**
     * Indicates the presence of errors that will likely prevent the
     * application module from being deployed and/or run successfully.
     */
    public static final ValidationResult FAILED = new ValidationResult("Failed");

    /**
     * Not only were there errors, they were so severe that we couldn't even
     * finish running the validation tests.
     */
    public static final ValidationResult ABORTED = new ValidationResult("Aborted");

    private String description;

    private ValidationResult(String description) {
        this.description = description;
    }

    public String toString() {
        return description;
    }
}
