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

package org.apache.geronimo.core.service;


/**
 *
 *
 *
 * @version $Revision: 1.4 $ $Date: 2004/03/10 09:58:42 $
 */
public interface Interceptor {

    /**
     * Interceptor does a little work for the invocation and then invokes the next interceptor
     * in the chain.
     *
     * @param invocation the invocation for which work will be done
     * @return the result of the invocation (includes return or application Exception)
     * @throws java.lang.Throwable if a system exception occures while doing the work
     */
    InvocationResult invoke(Invocation invocation) throws Throwable;
}
