/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.io;

public abstract class MinorCodes {

    public static final int FREEORB_ID = 0x12345 << 12;
    public static final int BAD_MAGIC = 1 | FREEORB_ID;
    public static final int BAD_MAJOR = 2 | FREEORB_ID;
    public static final int BAD_MINOR = 3 | FREEORB_ID;
    public static final int END_OF_ENCAPSULATION = 4 | FREEORB_ID;
	public static final int REQUEST_START_FAILED = 5 | FREEORB_ID;


}
