/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.naming;

public class NameServiceLog {
    public static NameServiceLog getInstance() {
        return new NameServiceLog();
    }

    public String infoBind(String context, String name, String value) {
        String msg;
        msg = "NSL.infoBind: context: " + context + ", name: + " + name + ", value: " + value;
        return msg;
    }

    public String warnAmbiguousBinding(String context, String name, String interfaceName) {
        String msg;
        msg = "NSL.warnAmbiguousBinding: context: " + context + ", name: + " + name + ", interfaceName: " + interfaceName;
        return msg;
    }

    public String warnAmbiguousPattern(String context, String name, String pattern) {
        String msg;
        msg = "NSL.warnAmbiguousPattern: context: " + context + ", name: + " + name + ", pattern: " + pattern;
        return msg;
    }

    public String warnBindFailed(String context, String name, String value, Exception ex) {
        String msg;
        msg = "NSL.warnBindFailed: context: " + context + ", name: + " + name + ", ex: " + ex;
        return msg;
    }

    public String warnIllegalBindValue(String context, Class type, String name, String value) {
        String msg;
        msg = "NSL.warnBindValue: context: " + context + ", type: + " + type + ", name: " + name + ", value: " + value;
        return msg;
    }

    public String warnNameNotFound(String context, Exception notFound) {
        String msg;
        msg = "NSL.warnNameNotFound: context: " + context + ", notFound: " + notFound;
        return msg;
    }

    public String warnNoComponentsForInterface(String context, String name, String interfaceName) {
        String msg;
        msg = "NSL.warnNoComponentForInterface: context: " + context + ", name: + " + name + ", interfaceName: " + interfaceName;
        return msg;
    }

    public String warnNoComponentsMatchPattern(String context, String name, String pattern) {
        String msg;
        msg = "NSL.warnNoComponentsMatchPattern: context: " + context + ", name: + " + name + ", pattern: " + pattern;
        return msg;
    }

    public String warnNoCurrentContext(String exception) {
        String msg;
        msg = "NSL.infoNoCurrentContext: exception: " + exception;
        return msg;
    }

    public String warnObjectHasNoRemoteInterface(String lookupName, String className) {
        String msg;
        msg = "NSL.warnObjectHasNoRemoteInterface: lookupName: " + lookupName + ", className: + " + className;
        return msg;
    }
}
