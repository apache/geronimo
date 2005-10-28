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
package org.apache.geronimo.corba.cdr;

import org.omg.CORBA_2_3.portable.OutputStream;


public abstract class CDROutputStream extends OutputStream {

    public void __writeEndian() {
        // TODO Auto-generated method stub

    }

    /**
     * Allocate a dynamic in-memory output stream
     */
    public static CDROutputStream create() {
        // TODO Auto-generated method stub
        return null;
    }

    /**  */
    public int __get_output_position() {
        // TODO Auto-generated method stub
        return 0;
    }

    public long __beginEncapsulation() {
        // TODO Auto-generated method stub
        return 0;
    }

    public void __finishEncapsulation(long encap_state) {
        // TODO Auto-generated method stub

    }

}
