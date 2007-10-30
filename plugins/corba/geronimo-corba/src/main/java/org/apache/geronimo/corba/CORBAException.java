/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.corba;


/**
 * @version $Revision: 465172 $ $Date: 2006-10-18 01:16:14 -0700 (Wed, 18 Oct 2006) $
 */
public class CORBAException extends Exception {
    public CORBAException() {
        super();
    }

    public CORBAException(String message) {
        super(message);
    }

    public CORBAException(String message, Throwable cause) {
        super(message, cause);
    }

    public CORBAException(Throwable cause) {
        super(cause);
    }
}
