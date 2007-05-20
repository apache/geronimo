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

package org.apache.geronimo.connector.outbound;

import javax.resource.ResourceException;

/**
 * @version $Rev$ $Date$
 */
public interface ConnectionFactorySource {

    //
    // This is implemented by "dynamic gbeans" that are swizzled to expose the
    // getters and setters on the javabean that they wrap.
    //
    // The $ is here  so this method couldn't have a name conflict with a javabean property and so it would
    // not be likely to be called by the casual observer.
    //

    Object $getResource() throws ResourceException;

}
