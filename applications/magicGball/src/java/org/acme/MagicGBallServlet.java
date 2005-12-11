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
package org.acme;

import java.io.IOException;
import java.io.PrintWriter;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Run this servlet
 *
 * @version $Rev$ $Date$
 */
public class MagicGBallServlet extends HttpServlet {

	public MagicGBallServlet() {
		super();
	}

	protected void service(HttpServletRequest req, HttpServletResponse res)
		throws ServletException, IOException {
		try {
			Context ctx = new InitialContext();
			MagicGBallLocalHome ejbHome = (MagicGBallLocalHome) ctx.lookup("java:comp/env/mGball");
			MagicGBallLocal m8ball = ejbHome.create();
			String question = req.getParameter("question");
			String answer = m8ball.ask(question);
			
			PrintWriter out = res.getWriter();
			out.print(answer);
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (CreateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
