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
 * ====================================================================
 */
package org.apache.geronimo.kernel.log;

import org.apache.commons.logging.LogFactory;

/**
 * @version $Revision: 1.1 $ $Date: 2004/02/13 07:22:22 $
 */
public class GeronimoLogging {
    public static final GeronimoLogging TRACE = new GeronimoLogging("TRACE");
    public static final GeronimoLogging DEBUG = new GeronimoLogging("DEBUG");
    public static final GeronimoLogging INFO = new GeronimoLogging("INFO");
    public static final GeronimoLogging WARN = new GeronimoLogging("WARN");
    public static final GeronimoLogging ERROR = new GeronimoLogging("ERROR");
    public static final GeronimoLogging FATAL = new GeronimoLogging("FATAL");

    private static boolean initialized = false;
    private static GeronimoLogging defaultLevel;

    /**
     * Initializes the logging system used by Geronimo.  This MUST be called in
     * in the main class used to start the geronimo server.  This method forces
     * commons logging to use GeronimoLogFacotry, starts the initial commons-logging
     * logging system, and forces mx4j to use commons logging.
     *
     * @param level
     */
    public static void initialize(GeronimoLogging level) {
        if (!initialized) {
            defaultLevel = level;

            // force commons-logging to use our log factory
            System.setProperty(LogFactory.FACTORY_PROPERTY, GeronimoLogFactory.class.getName());

            // force the log factory to initialize
            LogFactory.getLog(GeronimoLogging.class);

            // force mx4j to use commons logging
            // todo do this with reflection so mx4j is not required (this is important in JDK 1.5)
            mx4j.log.Log.redirectTo(new mx4j.log.CommonsLogger());

            initialized = true;
        }

    }

    public static GeronimoLogging getDefaultLevel() {
        return defaultLevel;
    }

    private final String level;

    private GeronimoLogging(String level) {
        this.level = level;
    }

    public String toString() {
        return level;
    }

    public boolean equals(Object object) {
        return object == this;
    }
}
