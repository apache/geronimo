/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.console.message;

import org.directwebremoting.annotations.DataTransferObject;
import org.directwebremoting.annotations.RemoteProperty;

@DataTransferObject
public final class JSCommonMessage {

    public static final String ERROR = "error";
    public static final String WARN = "warn";
    public static final String INFO = "info";

    private static final long serialVersionUID = 0L;

    private final CommonMessage.Type type;
    private final String abbr;
    private final String detail;

    public JSCommonMessage(CommonMessage.Type type, String abbr, String detail) {
        this.type = type;
        this.abbr = abbr;
        this.detail = detail;
    }

    @RemoteProperty
    public String getType() {
        String ret = "info";
        switch (type) {
        case Error:
            ret = ERROR;
            break;
        case Warn:
            ret = WARN;
            break;
        case Info:
            ret = INFO;
            break;
        }
        return ret;
    }

    @RemoteProperty
    public String getAbbr() {
        return this.abbr;
    }

    @RemoteProperty
    public String getDetail() {
        return this.detail;
    }

}
