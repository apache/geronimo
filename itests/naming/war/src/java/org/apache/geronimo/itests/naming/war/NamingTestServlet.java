package org.apache.geronimo.itests.naming.war;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.notgeronimo.itests.naming.common.Test;

/**
 *
 */
public class NamingTestServlet extends HttpServlet {
    /**
     *
     * @param request
     *                   the HTTP request object
     * @param response
     *                   the HTTP response object
     * @throws IOException
     *                    thrown when there is a problem getting the writer
     */
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {

        Test test = new Test();
        try {
            test.testWebServiceLookup();
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }
}
