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
package org.apache.geronimo.corba.codeset;

import org.apache.geronimo.corba.io.InputStreamBase;
import org.apache.geronimo.corba.io.OutputStreamBase;


public interface CharConverter {

    /**
     * write a char with this converter
     */
    void write_char(OutputStreamBase out, char value);

    /**
     * write a string with this converter
     */
    void write_string(OutputStreamBase out, String value);

    /**
     * read a single char
     */
    char read_char(InputStreamBase base);

    /**
     * read a string.  Parameter first_long is the first 4bytes of the read representation.
     */
    String read_string(InputStreamBase stream, int first_long);


}
