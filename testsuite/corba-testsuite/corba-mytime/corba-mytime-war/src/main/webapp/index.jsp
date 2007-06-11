<%@ page contentType="text/html" import="org.apache.geronimo.testsuite.corba.mytime.*, javax.naming.* " %>
<html<head><title>Time</title></head><body>
<%
    String s="-"; // Just declare a string
    try {
        // This creates a context, it can be used to lookup EJBs. Using normal RMI you would
        // have to know port number and stuff. The InitialContext holds info like
        // server names, ports and stuff I guess.
        Context context = new InitialContext();
        // MyTimeLocalHome is a rference to the EJB
        MyTimeLocalHome myTimeHomeLocal = (MyTimeLocalHome)context.lookup("java:comp/env/MyTime");
        // This is like a constructor returning a MyTimeLocal, an interface for the EJB on which you
        // can call methods (but not access variables as with any interface)
        MyTimeLocal myTimeLocal = myTimeHomeLocal.create();
        // So, just go ahead and call a method (in this case the only method).
        s =  myTimeLocal.getTime();
    }
    catch (Exception e) {
        s=e.toString();
    }
%>
This is the time returned from the EJB: <%=s%>
</body></html>
