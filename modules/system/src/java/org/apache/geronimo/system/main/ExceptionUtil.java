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
package org.apache.geronimo.system.main;

import java.util.ArrayList;

/**
 * @version $Rev: 46019 $ $Date: 2004-09-14 02:56:06 -0700 (Tue, 14 Sep 2004) $
 */
public class ExceptionUtil {

    private static final String[] excludedPackages = {
        "org.apache.geronimo.gbean.jmx.", "mx4j.", "net.sf.cglib.reflect"
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

        TRIM: for (int i = 0; i < trace.length; i++) {
            String className = trace[i].getClassName();
            for (int j = 0; j < excludedPackages.length; j++) {
                if (className.startsWith(excludedPackages[j])) {
                    continue TRIM;
                }
            }
            for (int j = 0; j < excludedStrings.length; j++) {
                if (className.indexOf(excludedStrings[j]) != -1) {
                    continue TRIM;
                }
            }
            list.add(trace[i]);
        }

        t.setStackTrace((StackTraceElement[]) list.toArray(new StackTraceElement[0]));
        trimStackTrace(t.getCause());
    }
}
