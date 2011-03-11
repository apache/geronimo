package org.apache.geronimo.testsuite.javaee6.helloworld.web;

import java.io.*;

import javax.annotation.Resource;
import javax.servlet.ServletException;

import javax.servlet.http.*;

import javax.servlet.annotation.WebServlet;

@WebServlet("/hello")
public class HelloWorld extends HttpServlet {
    private static final long serialVersionUID = 1L;
    @Resource(name="welcomeMessage")
    private  String welcomeMessage;

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        performTask(request, response);
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        performTask(request, response);
    }
    
    protected void  performTask(HttpServletRequest request, HttpServletResponse response){
        PrintWriter out;
        try {
            out = response.getWriter();
            out.println(welcomeMessage);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
       
    }
}
