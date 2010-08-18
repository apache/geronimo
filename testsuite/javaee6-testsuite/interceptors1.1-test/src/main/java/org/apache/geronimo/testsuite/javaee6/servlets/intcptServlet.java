/**
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package org.apache.geronimo.testsuite.javaee6.servlets;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ejb.EJB;
import org.apache.geronimo.testsuite.javaee6.beans.ValueBean;

public class intcptServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	@EJB
	private ValueBean valuebean;
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException{

		//System.out.println("start doGet");
		double d=Double.valueOf(request.getParameter("NumberValue"));
                String[] arr = new String[6];
		
		//try{
                //  InitialContext ctx = new InitialContext();
                //  ValueBean valuebean = (ValueBean) ctx.lookup("java:global/interceptor/ValueBean");
        //valuebean.SayFound(); 
		arr=valuebean.SayIsValid(d,arr);
                //System.out.println("in servlet,arr[0]:"+arr[0]);
                //System.out.println("in servlet,arr[1]:"+arr[1]);
                //System.out.println("in servlet,arr[2]:"+arr[2]);
                //System.out.println("in servlet,arr[3]:"+arr[3]);
                //System.out.println("in servlet,arr[4]:"+arr[4]);
                //System.out.println("in servlet,arr[5]:"+arr[5]);
	request.setAttribute("Intcpt1IsValid",arr[0]);
        request.setAttribute("Intcpt2IsValid",arr[3]);
        request.setAttribute("date1",arr[1]);
        request.setAttribute("date2",arr[4]);
        request.setAttribute("sysmi1",arr[2]);
        request.setAttribute("sysmi2",arr[5]);
		getServletContext().getRequestDispatcher("/index.jsp").forward(request, response);
               // }
               // catch(NamingException ne){
               // }
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		this.doGet(request, response);
	}
    

}
