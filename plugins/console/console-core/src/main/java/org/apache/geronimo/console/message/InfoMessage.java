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

public final class InfoMessage extends CommonMessage {

    private static final long serialVersionUID = 0L;

    public InfoMessage(String abbr) {
        super(abbr);
    }

    public InfoMessage(String abbr, String detail) {
        super(abbr, detail);
    }

    @Override
    public String renderMessage() {
        String timestamp = String.valueOf(System.nanoTime());
        StringBuilder sb = new StringBuilder();
        sb.append("<tr valign=\"top\">");
        sb.append("<td style=\"width: 20px;\">");
        if (null != detail) {
            sb.append("<a class=\"expand-task\" href=\"javascript:showHideSection('"
                            + timestamp
                            + "org_apache_geronimo_abbreviateMessages');showHideSection('"
                            + timestamp
                            + "org_apache_geronimo_detailedMessages');\" tabindex=\"1\"><img border=\"0\" align=\"absmiddle\" alt=\"show/hide\" src=\"/console/images/arrow_collapsed.gif\" id=\""
                            + timestamp + "org_apache_geronimo_abbreviateMessagesImg\" title=\"show/hide\"/></a>");
        }
        sb.append("</td>");
        sb.append("<td style=\"width: 20px;\"><img height=\"16\" width=\"16\" align=\"baseline\" src=\"/console/images/msg_info.gif\" alt=\"Info\" title=\"Information\"/></td>");
        sb.append("<td><span id=\"" + timestamp
                + "org_apache_geronimo_abbreviateMessages\" style=\"display: inline;\" class=\"validation-info\">"
                + abbr + "</span>");
        if (null != detail) {
            sb.append("<span id=\""
                            + timestamp
                            + "org_apache_geronimo_detailedMessages\" style=\"display: none;\" class=\"validation-info\">"
                            + convertLineBreakToBR(detail) + "</span>");
        }
        sb.append("</td></tr>");
        return sb.toString();
    }
}
