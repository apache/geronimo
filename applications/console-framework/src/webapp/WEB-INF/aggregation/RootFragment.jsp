<%--
Copyright 2004 The Apache Software Foundation
Licensed  under the  Apache License,  Version 2.0  (the "License");
you may not use  this file  except in  compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed  under the  License is distributed on an "AS IS" BASIS,
WITHOUT  WARRANTIES OR CONDITIONS  OF ANY KIND, either  express  or
implied.

See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page session="true" buffer="none" %>
<%@ page import="java.util.Iterator" %>
<%@ page import="org.apache.pluto.portalImpl.aggregation.Fragment" %>
<%@ page import="org.apache.pluto.portalImpl.aggregation.navigation.AbstractNavigationFragment" %>
<jsp:useBean id="fragment" type="org.apache.pluto.portalImpl.aggregation.Fragment" scope="request" />
<html>
<%@ include file="./Head.jsp" %>
<body marginwidth="0" marginheight="0" leftmargin="0" topmargin="0" rightmargin="0">
<table width="100%" cellpadding="0" cellspacing="0" border="0" id="rootfragment">
  <!-- Header -->
  <%@ include file="./Banner.jsp" %>
	<!-- Header -->	
	<tr>
	  <td>
		  <table width="100%"  border="0" cellpadding="0" cellspacing="0">
			  <!-- Spacer -->
        <tr> 
          <td class="Gutter">&nbsp;</td> 
          <td>&nbsp;</td> 
          <td class="Gutter">&nbsp;</td> 
          <td>&nbsp;</td> 
          <td class="Gutter">&nbsp;</td> 
        </tr> 
			  <!-- Spacer -->
			  <!-- Start of Body -->
			  <tr>
				  <!-- Navigation Column -->
					<!-- Spacer -->
          <td class="Gutter">&nbsp;</td> 
					<!-- Spacer -->
          <td width="200px" valign="top"> 
		        <div class="Menu"> 
              <table width="100%"  border="0" cellpadding="0" cellspacing="0"> 
                <tr> 
                  <td>
<%
        Iterator childIterator = fragment.getChildFragments().iterator();

        while (childIterator.hasNext()) {
            Fragment subfragment = (Fragment)childIterator.next();

            if (subfragment instanceof AbstractNavigationFragment)
            {
                subfragment.service(request, response);
                break;
            }

        }
%>
									</td>
                </tr>
              </table>
	          </div>
				  </td>
				  <!-- Navigation Column -->
					<!-- Spacer -->
          <td class="Gutter">&nbsp;</td> 
					<!-- Spacer -->
					<!-- Portlet Section -->
          <td valign="top"> 
					    <table width="100%" border="0" cellpadding="0" cellspacing="0"> 
<%
        childIterator = fragment.getChildFragments().iterator();

        while (childIterator.hasNext()) {
            Fragment subfragment = (Fragment)childIterator.next();

            if (!(subfragment instanceof AbstractNavigationFragment))
            {
                subfragment.service(request, response);
            }
        }
%>
              </table>
          </td>
					<!-- Spacer -->
          <td class="Gutter">&nbsp;</td> 
					<!-- Spacer -->
          <td class="Gutter">&nbsp;</td> 
				</tr>
			  <!-- End of Body -->
			</table>
		</td>
	</tr>
</table>
</body>
</html>