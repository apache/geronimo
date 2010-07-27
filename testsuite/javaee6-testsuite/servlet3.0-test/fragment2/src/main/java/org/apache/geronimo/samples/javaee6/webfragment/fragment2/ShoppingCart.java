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
**/
package org.apache.geronimo.samples.javaee6.webfragment.fragment2;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Administrator
 */
public class ShoppingCart extends HttpServlet {

    ArrayList<BuyRecordItem> shoppingList;

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
        PrintWriter out = response.getWriter();

        if (null == request.getSession().getAttribute("shoppingList")) {
            shoppingList = new ArrayList<BuyRecordItem>();
        } else {
            shoppingList = (ArrayList) request.getSession().getAttribute("shoppingList");
        }
        BuyRecordItem recordItem = new BuyRecordItem(Integer.parseInt(request.getParameter("ID")), Integer.parseInt(request.getParameter("quantity")));
        addToCart(recordItem, shoppingList);
        request.getSession().setAttribute("shoppingList", shoppingList);
        try {
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet ShoppingCart</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>You have already bought:</h1>");
            out.println("<table border=1>");
            out.println("<tr><th>ID</th><th>Name</th><th>Price</th><th>Quantity</th><th>Cost</th></tr>");
            int totalCost = 0;
            for (BuyRecordItem record : shoppingList) {
                int ID = record.getID();
                int quantity = record.getQuantity();
                int cost = ID * 10 * quantity;
                totalCost += cost;
                out.println("<tr><td>" + ID + "</td><td>Item" + ID + "</td><td>" + (ID * 10) + "</td><td>" + quantity + "</td><td>" + cost + "</td></tr>");
            }
            out.println("<tr><h2>The total cost is:<font color=green>" + totalCost + "</font><h2></tr>");
            out.println("</table>");
            out.println("<a href=\"QueryAll\"><h2>>>Continue shopping!<h2></a><br>");
            out.println("<a href=\"Payment?totalCost="+totalCost+"\">>>Go to pay!</a>");
            out.println("</body>");
            out.println("</html>");
        } finally {
            out.close();
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
    }

    private void addToCart(BuyRecordItem currentRecordItem, ArrayList<BuyRecordItem> shoppingList) {
        int currentID=currentRecordItem.getID();
        int currentQuantity=currentRecordItem.getQuantity();
        boolean exist=false;
        for(BuyRecordItem record:shoppingList)
        {
        if(currentID==record.getID())
        {
              record.setQuantity(record.getQuantity()+currentQuantity);
              exist=true;
              break;
        }
          
        }
        if(!exist)
        this.shoppingList.add(currentRecordItem);
    }
}
