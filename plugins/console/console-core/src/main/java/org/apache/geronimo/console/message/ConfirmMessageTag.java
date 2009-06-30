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

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ConfirmMessageTag extends TagSupport {

    private static final long serialVersionUID = 0L;
    private static final Logger log = LoggerFactory.getLogger(ConfirmMessageTag.class);

    private static final String DOJO_BASE = "/dojo/dojo";

    @Override
    public int doStartTag() throws JspException {
        JspWriter out = pageContext.getOut();
        try {
            out.println("<style type=\"text/css\">");
            out.println("@import \"" + DOJO_BASE + "/dijit/themes/soria/soria.css\";");
            out.println("@import \"" + DOJO_BASE + "/dojo/resources/dojo.css\";");
            out.println(".soria .dijitDialog {background:#F7F7F7; border:3px solid #88A4D7; width:450px; padding:10px; font-family:Verdana,Helvetica,sans-serif;}");
            out.println(".soria .dijitDialog .dijitDialogPaneContent {background:#F7F7F7; border:0; padding:0;}");
            out.println(".soria .dijitDialogUnderlay {background:#000000; opacity:0.5;}");
            out.println(".soria .dijitDialogTitleBar {display:none;}");
            out.println(".soria .dijitButtonNode {background:#F7F7F7; border:2px solid #88A4D7; padding:0.2em; font-family:Verdana,Helvetica,sans-serif;}");
            out.println("</style>");
            out.println(" <script type=\"text/javascript\" src=\"" + DOJO_BASE + "/dojo/dojo.js\" djConfig=\"parseOnLoad:true\"></script>");
        } catch (IOException e) {
            log.error("Unable to display confirm messages");
        }
        return SKIP_BODY;
    }

}
