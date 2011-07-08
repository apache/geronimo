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
package org.apache.geronimo.console.message;

import java.io.Serializable;

public abstract class CommonMessage implements Serializable {

    public enum Type {
        Info, Warn, Error;
    }

    private static final long serialVersionUID = 0L;

    protected String abbr;

    protected String detail;

    public CommonMessage() {
    }

    public CommonMessage(String abbr) {
        this.abbr = abbr;
    }

    public CommonMessage(String abbr, String detail) {
        this.abbr = abbr;
        this.detail = detail;
    }

    public String getAbbr() {
        return abbr;
    }

    public String getDetail() {
        return detail;
    }

    public abstract String renderMessage();

    protected String convertLineBreakToBR(String str){
        return str.replaceAll("\r\n|[\r\n]", "<br />");
    }
}
