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

import java.io.IOException;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CommonMessageTag extends TagSupport {

    private static final long serialVersionUID = 0L;
    private static final Logger log = LoggerFactory.getLogger(CommonMessageTag.class);
    private static final String COMMON_MESSAGES = "commonMessages";

    @Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        @SuppressWarnings("unchecked")
        List<CommonMessage> messages = (List<CommonMessage>) pageContext.getRequest().getAttribute(COMMON_MESSAGES);
        if (null != messages && 0 != messages.size()) {
            try {
                out.println("<table cellspacing=\"0\" width=\"100%\" summary=\"Inline Messages\" class=\"messagePortlet\"><tbody>");
                for (CommonMessage message : messages) {
                    out.println(message.renderMessage());
                }
                out.println("</tbody></table>");
            } catch (IOException e) {
                log.error("Unable to display common messages");
            }
        }
        return SKIP_BODY;
    }

}
