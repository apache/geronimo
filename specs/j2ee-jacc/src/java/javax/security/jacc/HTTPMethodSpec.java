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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.security.jacc;


/**
 * @version $Rev$ $Date$
 */
final class HTTPMethodSpec {

    private final static String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE"};
    private final static int[] HTTP_MASKS = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40};

    final static int NA = 0x00;
    final static int INTEGRAL = 0x01;
    final static int CONFIDENTIAL = 0x02;
    final static int NONE = INTEGRAL | CONFIDENTIAL;

    private final int mask;
    private final int transport;
    private String actions;

    public HTTPMethodSpec(String[] HTTPMethods) {
        this(HTTPMethods, null);
    }

    public HTTPMethodSpec(String name, boolean parseTransportType) {
        if (parseTransportType) {
            if (name == null || name.length() == 0) {
                this.transport = NONE;
            } else {
                String[] tokens = name.split(":", 2);

                if (tokens.length == 2) {
                    if (tokens[1].equals("NONE")) {
                        this.transport = NONE;
                    } else if (tokens[1].equals("INTEGRAL")) {
                        this.transport = INTEGRAL;
                    } else if (tokens[1].equals("CONFIDENTIAL")) {
                        this.transport = CONFIDENTIAL;
                    } else {
                        throw new IllegalArgumentException("Invalid transportType: " + tokens[1]);
                    }
                } else {
                    this.transport = NONE;
                }
                name = tokens[0];
            }
        } else {
            this.transport = NA;
        }

        if (name == null || name.length() == 0) {
            this.mask = 0x7F;
        } else {
            String[] methods = name.split(",", -1);
            int tmpMask = 0;

            for (int i = 0; i < methods.length; i++) {
                boolean found = false;

                for (int j = 0; j < HTTP_METHODS.length; j++) {
                    if (methods[i].equals(HTTP_METHODS[j])) {
                        tmpMask |= HTTP_MASKS[j];
                        found = true;

                        break;
                    }
                }
                if (!found) throw new IllegalArgumentException("Invalid HTTPMethodSpec");
            }
            this.mask = tmpMask;
        }
    }

    public HTTPMethodSpec(String[] HTTPMethods, String transport) {
        boolean parseTransportType = transport != null;

        if (HTTPMethods == null || HTTPMethods.length == 0) {
            this.mask = 0x7F;
        } else {
            int tmpMask = 0;

            for (int i = 0; i < HTTPMethods.length; i++) {

                for (int j = 0; j < HTTP_METHODS.length; j++) {
                    if (HTTPMethods[i].equals(HTTP_METHODS[j])) {
                        tmpMask |= HTTP_MASKS[j];

                        break;
                    }
                }
                if (tmpMask == 0) throw new IllegalArgumentException("Invalid HTTPMethodSpec");
            }
            this.mask = tmpMask;
        }

        if (parseTransportType) {
            if (transport.length() == 0 || transport.equals("NONE")) {
                this.transport = NONE;
            } else if (transport.equals("INTEGRAL")) {
                this.transport = INTEGRAL;
            } else if (transport.equals("CONFIDENTIAL")) {
                this.transport = CONFIDENTIAL;
            } else {
                throw new IllegalArgumentException("Invalid transport");
            }
        } else {
            this.transport = NONE;
        }
    }

    public HTTPMethodSpec(String singleMethod, int transport) {
        int tmpMask = 0;

        for (int j = 0; j < HTTP_METHODS.length; j++) {
            if (HTTP_METHODS[j].equals(singleMethod)) {
                tmpMask = HTTP_MASKS[j];

                break;
            }
        }
        if (tmpMask == 0) throw new IllegalArgumentException("Invalid HTTPMethodSpec");
        this.mask = tmpMask;
        this.transport = transport;
    }

    public boolean equals(HTTPMethodSpec o) {
        return mask == o.mask && transport == o.transport;
    }

    public String getActions() {
        if (actions == null) {
            boolean first = true;
            StringBuffer buffer = new StringBuffer();

            for (int i = 0; i < HTTP_MASKS.length; i++) {
                if ((mask & HTTP_MASKS[i]) > 0) {
                    if (first) {
                        first = false;
                    } else {
                        buffer.append(",");
                    }
                    buffer.append(HTTP_METHODS[i]);
                }
            }

            if (transport == INTEGRAL) {
                buffer.append(":INTEGRAL");
            } else if (transport == CONFIDENTIAL) {
                buffer.append(":CONFIDENTIAL");
            }

            actions = buffer.toString();
        }
        return actions;
    }

    public int hashCode() {
        return mask ^ transport;
    }

    public boolean implies(HTTPMethodSpec p) {
        return ((mask & p.mask) == p.mask) && ((transport & p.transport) == p.transport);
    }
}
