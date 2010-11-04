/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.geronimo.testsuite.servlet3.app;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.AsyncContext;

public class TaskExecutor implements Runnable {
    private AsyncContext ctx = null;

    public TaskExecutor(AsyncContext ctx) {
        this.ctx = ctx;
    }


    public void run(){
        PrintWriter out = null;
        try {
            out = ctx.getResponse().getWriter();

        } catch (IOException e) {

            e.printStackTrace();
        }
        out.println("<p id='c'>");
        out.println("TaskExecutor starts at: " + "<font color='red'>" + new Date() + " -> <b id='tst'>" + System.currentTimeMillis() + "</b></font>" + "." + "Task starts executing" + ".");
        out.println("</p>");
        out.println("<br><br>");
        out.flush();
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {

            e.printStackTrace();
        }
        out.println("<p id='d'>");
        out.println("Task finishes.");
        out.println("</p>");
        out.println("<p>");
        out.println("TaskExecutor finishes at: " + "<font color='red'>" + new Date() + " -> <b id='tft'>" + System.currentTimeMillis() + "</b></font>" + ".");
        out.println("</p>");
        out.println("<br><br>");
        out.println("</body>");
        out.println("</html>");
        out.flush();
        out.close();
        ctx.complete();
    }

}
