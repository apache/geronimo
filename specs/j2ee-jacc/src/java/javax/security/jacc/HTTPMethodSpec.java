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
class HTTPMethodSpec {

    private final static String[] HTTP_METHODS = {"GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE"};
    private final static int[] HTTP_MASKS = {0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40};

    private final static int INTEGRAL = 0x01;
    private final static int CONFIDENTIAL = 0x02;
    private final static int NONE = INTEGRAL | CONFIDENTIAL;

    private int mask = 0;
    private int transport = 0;
    private String actions;

    public HTTPMethodSpec(String[] HTTPMethods) {
        this(HTTPMethods, null);
    }

    public HTTPMethodSpec(String name, boolean parseTransportType) {
        if (name == null || name.length() == 0) {
            mask = 0x7F;
            transport = NONE;
        } else {
            String[] tokens = name.split(":", 2);

            if (tokens[0].length() == 0) {
                mask = 0x7F;
            } else {
                String[] methods = tokens[0].split(",", -1);

                for (int i = 0; i < methods.length; i++) {
                    boolean found = false;

                    for (int j = 0; j < HTTP_METHODS.length; j++) {
                        if (methods[i].equals(HTTP_METHODS[j])) {
                            mask |= HTTP_MASKS[j];
                            found = true;

                            break;
                        }
                    }
                    if (!found) throw new IllegalArgumentException("Invalid HTTPMethodSpec");
                }
            }

            if (tokens.length == 2) {
                if (!parseTransportType) throw new IllegalArgumentException("Invalid HTTPMethodSpec");

                if (tokens[1].equals("NONE")) {
                    transport = NONE;
                } else if (tokens[1].equals("INTEGRAL")) {
                    transport = INTEGRAL;
                } else if (tokens[1].equals("CONFIDENTIAL")) {
                    transport = CONFIDENTIAL;
                } else {
                    throw new IllegalArgumentException("Invalid transportType: " + tokens[1]);
                }
            } else {
                transport = NONE;
            }
        }
    }

    public HTTPMethodSpec(String[] HTTPMethods, String transportType) {
        boolean parseTransportType = transportType != null;

        if (HTTPMethods == null || HTTPMethods.length == 0) {
            mask = 0x7F;
        } else {
            for (int i = 0; i < HTTPMethods.length; i++) {
                boolean found = false;

                for (int j = 0; j < HTTP_METHODS.length; j++) {
                    if (HTTPMethods[i].equals(HTTP_METHODS[j])) {
                        mask |= HTTP_MASKS[j];
                        found = true;

                        break;
                    }
                }
                if (!found) throw new IllegalArgumentException("Invalid HTTPMethodSpec");
            }
        }

        if (parseTransportType) {
            if (transportType.length() == 0 || transportType.equals("NONE")) {
                transport = NONE;
            } else if (transportType.equals("INTEGRAL")) {
                transport = INTEGRAL;
            } else if (transportType.equals("CONFIDENTIAL")) {
                transport = CONFIDENTIAL;
            } else {
                throw new IllegalArgumentException("Invalid transportType");
            }
        } else {
            transport = NONE;
        }
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

            if (transport != NONE) {
                buffer.append(":");
                if (transport == INTEGRAL) {
                    buffer.append("INTEGRAL");
                } else {
                    buffer.append("CONFIDENTIAL");
                }
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
