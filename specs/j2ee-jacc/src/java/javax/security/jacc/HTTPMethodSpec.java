/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */

package javax.security.jacc;

/**
 *
 * @version $Revision: 1.1 $ $Date: 2003/08/30 01:55:12 $
 */
class HTTPMethodSpec {

    private final static String[] HTTP_METHODS = { "GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS", "TRACE" };
    private final static int[] HTTP_MASKS      = {  0x01,   0x02,  0x04,     0x08,   0x10,      0x20,    0x40 };

    private final static int NA           =0x00;
    private final static int INTEGRAL     =0x01;
    private final static int CONFIDENTIAL =0x02;

    private int mask =0;
    private int transport =0;
    private String actions;

    public HTTPMethodSpec(String name) {
        this(name, false);
    }

    public HTTPMethodSpec(String[] HTTPMethods) {
        this(HTTPMethods, null);
    }

    public HTTPMethodSpec(String name, boolean parseTransportType) {
        if (name == null || name.length() == 0) {
            mask      = 0x7F;
            transport = (parseTransportType? 0x03:NA);
        } else {
            String[] tokens = name.split(":", 2);

            if (tokens[0].length() == 0) {
                mask = 0x7F;
            } else {
                String[] methods = tokens[0].split(",", -1);

                for (int i=0; i<methods.length; i++) {
                    boolean found =false;

                    for (int j=0; j<HTTP_METHODS.length; j++) {
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

                if (tokens[1].length() == 0) {
                    throw new IllegalArgumentException("Missing transport type");
                } else if (tokens[1].equals("INTEGRAL")) {
                    transport = INTEGRAL;
                } else if (tokens[1].equals("CONFIDENTIAL")) {
                    transport = CONFIDENTIAL;
                } else {
                    throw new IllegalArgumentException("Invalid transportType");
                }
            } else {
                transport = (parseTransportType? 0x03:NA);
            }
        }
    }

    public HTTPMethodSpec(String[] HTTPMethods, String transportType) {
        boolean parseTransportType = transportType != null;

        if (HTTPMethods == null || HTTPMethods.length == 0) {
            mask = 0x7F;
        } else {
            for (int i=0; i<HTTPMethods.length; i++) {
                boolean found =false;

                for (int j=0; j<HTTP_METHODS.length; j++) {
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
            if (transportType.length() == 0) {
                transport = 0x03;
            } else if (transportType.equals("INTEGRAL")) {
                transport = INTEGRAL;
            } else if (transportType.equals("CONFIDENTIAL")) {
                transport = CONFIDENTIAL;
            } else {
                throw new IllegalArgumentException("Invalid transportType");
            }
        } else {
            transport = NA;
        }
    }

    public boolean equals(HTTPMethodSpec o) {
        return mask == o.mask && transport == o.transport;
    }

    public String getActions() {
        if (actions == null) {
            boolean first = true;
            StringBuffer buffer = new StringBuffer();

            for (int i=0; i<HTTP_MASKS.length; i++) {
                if ((mask & HTTP_MASKS[i]) > 0) {
                    if (first) {
                        first = false;
                    } else {
                        buffer.append(",");
                    }
                    buffer.append(HTTP_METHODS[i]);
                }
            }

            if (transport != NA) {
                buffer.append(":");

                if (transport != 0x03) {
                    if (transport == INTEGRAL) {
                        buffer.append("INTEGRAL");
                    } else {
                        buffer.append("CONFIDENTIAL");
                    }
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
