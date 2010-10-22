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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.sample.RegisterValidation.Action;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import javax.annotation.Resource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.geronimo.sample.RegisterValidation.Model.Address;
import org.apache.geronimo.sample.RegisterValidation.Model.Information;
import org.apache.geronimo.sample.RegisterValidation.Model.OrdinaryPeople;
import org.apache.geronimo.sample.RegisterValidation.Model.VIP;

@WebServlet(name = "submitInfo", urlPatterns = {"/submitInfo"})
public class submitInfo extends HttpServlet {

    // we'll also test that these get injected properly, as well as the jndi lookup version 
    @Resource ValidatorFactory injectedValidatorFactory; 
    @Resource Validator injectedValidator; 
    
    /** 
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code> methods.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        Information infor = new Information();
        String message = "";
        String name = request.getParameter("name");
        if(""==name)name=null; 
        String age = request.getParameter("age");
        if(""==age)age=null;
        String mail = request.getParameter("mail");
        if(""==mail)mail=null;
        String country = request.getParameter("country");
        if(""==country)country=null;
        String state = request.getParameter("state");
        if(""==state)state=null;
        String city = request.getParameter("city");
        if(""==city)city=null;
        String salary = request.getParameter("salary");
        if(""==salary)salary=null;
        String birthday=request.getParameter("birthday");
        if(""==birthday)birthday=null;
        infor.setName(name);  
        if(null!=age)
        infor.setAge(Integer.parseInt(age));
        infor.setMail(mail);       
        infor.setAddress(new Address(country, state, city));
        if(null!=salary)
        infor.setSalary(Integer.parseInt(salary));
        infor.setBirthday(birthday);
        
        int violationSize = 0;                
        ValidatorFactory vf = null; 
        Validator validator = null; 
        
        if (injectedValidatorFactory == null) {
            message += "<b>Unable to ValidatorFactory injected as @Resource<br>";
            violationSize += 1; 
        }
        if (injectedValidator == null) {
            message += "<b>Unable to Validator injected as @Resource<br>";
            violationSize += 1; 
        }
        
        // obtain the validator factory and validator instance from JNDI 
        try {
            InitialContext ctx = new InitialContext();
            vf = (ValidatorFactory)ctx.lookup("java:comp/ValidatorFactory");
            validator = (Validator)ctx.lookup("java:comp/Validator");
            Validator validator2 = (Validator)ctx.lookup("java:comp/Validator");
            if (validator == validator2) {
                message += "<b>JNDI Validator is a singleton<br>";
                violationSize += 1; 
            }
        } catch (NamingException e) {
            // log this as a violation 
            message += "<b>Unable to obtain validator from JNDI:</b>" + e.getMessage() + "<br>";
            violationSize += 1; 
            // this is an error condition, but just mark the error and 
            // create validators directly  
            vf = Validation.buildDefaultValidatorFactory();
            validator = (Validator)vf.getValidator();
        }
        
        Object contextValidator = getServletContext().getAttribute("javax.faces.validator.beanValidator.ValidatorFactory"); 
        if (contextValidator == null || !(contextValidator instanceof ValidatorFactory)) {
            message += "<b>Unable to obtain validator from ServletContext: " + contextValidator + "</b><br>";
            violationSize += 1; 
        }
        
        Set<ConstraintViolation<Information>> generalSet = validator.validate(infor);
        int generalSize = generalSet.size();
        violationSize += generalSize;
        if (generalSize > 0) {
            for (ConstraintViolation<Information> constraintViolation : generalSet) {
                message += "<b>Invalid value:</b><font color=red>" + constraintViolation.getInvalidValue() + "</font>," + constraintViolation.getMessage() + "<br>";
            }
        }

        if (null != request.getParameter("submit")) {
            Set<ConstraintViolation<Information>> salarySet = validator.validate(infor, OrdinaryPeople.class);
            int salarySize = salarySet.size();
            violationSize += salarySize;
            if (salarySize > 0) {
                for (ConstraintViolation<Information> constraintViolation : salarySet) {
                    message += "<b>Invalid value:</b><font color=red>" + constraintViolation.getInvalidValue() + "</font>," + constraintViolation.getMessage() + "<br>";
                }
            }
        }
        if (null != request.getParameter("submitAsVIP")) {
            Set<ConstraintViolation<Information>> VIPSalarySet = validator.validate(infor, VIP.class);
            int VIPSalarySize = VIPSalarySet.size();
            violationSize += VIPSalarySize;
            if (VIPSalarySize > 0) {
                for (ConstraintViolation<Information> constraintViolation : VIPSalarySet) {
                    message += "<b>Invalid value:</b><font color=red>" + constraintViolation.getInvalidValue() + "</font>," + constraintViolation.getMessage() + "<br>";
                }
            }
        }
        if (violationSize <= 0) {
            message += "Congratulations,all your information passed validation!";
            request.setAttribute("message", message);
            RequestDispatcher dispatcher = request.getRequestDispatcher("successful.jsp");
            dispatcher.forward(request, response);
        } else {
            request.getSession().setAttribute("message", message);
            RequestDispatcher dispatcher = request.getRequestDispatcher("register.jsp");
            dispatcher.forward(request, response);
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /** 
     * Handles the HTTP <code>GET</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Handles the HTTP <code>POST</code> method.
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /** 
     * Returns a short description of the servlet.
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
