/**
 *
 * Copyright 2004 The Apache Software Foundation
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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.resource.spi.work;

/**
 *
 *
 *
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:58:29 $
 */
public interface WorkManager {
    public static final long IMMEDIATE = 0L;
    public static final long INDEFINITE = Long.MAX_VALUE;
    public static final long UNKNOWN = -1;

    public void doWork(Work work) throws WorkException;

    public void doWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener) throws WorkException;

    public long startWork(Work work) throws WorkException;

    public long startWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener) throws WorkException;

    public void scheduleWork(Work work) throws WorkException;

    public void scheduleWork(Work work, long startTimeout, ExecutionContext execContext, WorkListener workListener) throws WorkException;
}