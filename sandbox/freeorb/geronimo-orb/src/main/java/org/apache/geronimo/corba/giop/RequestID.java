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
/**
 * 
 */
package org.apache.geronimo.corba.giop;

class RequestID {

    final int id;

    RequestID(int id) {
        this.id = id;
    }

    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (other instanceof RequestID) {
            RequestID req = (RequestID) other;
            return req.id == id;
        }
        return false;
    }

    public int hashCode() {
        return id;
    }

    public int value() {
        return id;
    }

    boolean isAssignedHere(boolean hereIsClient) {
        boolean is_even = ((id & 1) == 0);
        return hereIsClient & is_even;
    }
}