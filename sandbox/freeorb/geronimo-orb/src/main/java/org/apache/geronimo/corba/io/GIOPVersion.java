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

import org.omg.IIOP.Version;


public final class GIOPVersion {

    private GIOPVersion(int i, int j) {
        this.major = i;
        this.minor = j;
    }

    public final int major;
    public final int minor;

    public static final GIOPVersion V1_0 = new GIOPVersion(1, 0);
    public static final GIOPVersion V1_1 = new GIOPVersion(1, 1);
    public static final GIOPVersion V1_2 = new GIOPVersion(1, 2);

    public int hashCode() {
        return major * 10 + minor;
    }

    public boolean equals(Object other) {
        if (other instanceof GIOPVersion) {
            GIOPVersion o = (GIOPVersion) other;
            return major == o.major && minor == o.minor;
        }

        return false;
    }

    public static GIOPVersion get(Version version) {
        return get(version.major, version.minor);
    }

    public static GIOPVersion get(int major2, int minor2) {
        if (major2 == 1) {
            if (minor2 == 0) return V1_0;
            if (minor2 == 1) return V1_1;
            if (minor2 == 2) return V1_2;
        }

        return new GIOPVersion(major2, minor2);
    }
}
