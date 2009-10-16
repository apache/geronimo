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

package org.apache.geronimo.interceptor;

/**
 * The result of an Invocation.
 * There are two types of result:
 * <ul>
 * <li>normal - indicating the operation completed normally (e.g. the method returned)</li>
 * <li>exception - indicating the operation completed abnormally (e.g. the method threw a checked exception)</li>
 * </ul>
 * <p>Note that these should both be considered a normal completion of the operation by the container. Abnormal
 * completions, such as a RuntimeException or Error from the invocation, or any problem in the interceptor
 * chain itself, should result in a Throwable being thrown up the chain rather than being contained in this
 * result.</p>
 * <p>This distinction mirrors the semantics for EJB invocations, where a business method is considered to have
 * completed successfuly even if it throws declared Exception - the Exception there is indicating a business level
 * issue and not a system problem.</p>
 *
 * @version $Rev$ $Date$
 */
public interface InvocationResult {
    /**
     * Was this a normal completion (return)?
     * @return true if the invocation returned; false if a declared exception was thrown
     */
    boolean isNormal();

    /**
     * Get the return value from the invocation.
     * It is an error to call this method if the invocation is not complete normally.
     * @return the return value from the invocation; null if the operation was void
     */
    Object getResult();

    /**
     * Was an application exception raised by the invocation?
     * Note, this indicates a checked application exception was thrown; this will never contain
     * a system exception
     * @return true if a declared exception was thrown; false if the invocation returned
     */
    boolean isException();

    /**
     * Get the application exception raised by the invocation.
     * It is an error to call this method if the invocation did not raise an exception
     * @return the checked Exception raised by the application
     */
    Exception getException();
}
