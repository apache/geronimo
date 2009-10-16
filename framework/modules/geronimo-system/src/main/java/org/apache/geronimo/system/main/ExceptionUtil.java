/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.system.main;

import java.util.ArrayList;

/**
 * @version $Rev$ $Date$
 */
public class ExceptionUtil {

    private static final String[] excludedPackages = {
        "org.apache.geronimo.gbean.jmx.", "net.sf.cglib.reflect.", "sun.reflect."
    };

    private static final String[] excludedStrings = {
        "$$EnhancerByCGLIB$$","$$FastClassByCGLIB$$"
    };

    public static void trimStackTrace(Throwable t) {
        if (t == null) {
            return;
        }

        StackTraceElement[] trace = t.getStackTrace();
        ArrayList list = new ArrayList();

        boolean skip = true;

        int i = 0;

        // If the start of the stack trace is something
        // on the exclude list, don't exclude it.
        for (; i < trace.length && skip; i++) {
            skip = skip && isExcluded(trace[i].getClassName());
        }
        list.add(trace[i-1]);


        for (; i < trace.length; i++) {
            if ( !isExcluded(trace[i].getClassName()) ){
                list.add(trace[i]);
            }
        }

        t.setStackTrace((StackTraceElement[]) list.toArray(new StackTraceElement[0]));
        trimStackTrace(t.getCause());
    }

    private static boolean isExcluded(String className) {
        for (int j = 0; j < excludedPackages.length; j++) {
            if (className.startsWith(excludedPackages[j])) {
                return true;
            }
        }
        for (int j = 0; j < excludedStrings.length; j++) {
            if (className.indexOf(excludedStrings[j]) != -1) {
                return true;
            }
        }
        return false;
    }
}
