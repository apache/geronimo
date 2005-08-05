       <%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
       <%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
       <portlet:defineObjects/>
		<script language="JavaScript">
		var <portlet:namespace/>formName = "<portlet:namespace/>createDestinationForm";
		var <portlet:namespace/>requiredFields = new Array("destinationMessageDestinationName","destinationPhysicalName");
		function <portlet:namespace/>validateForm(){
		    return (textElementsNotEmpty(<portlet:namespace/>formName,<portlet:namespace/>requiredFields));
		}
		</script>

       <script>

       function <portlet:namespace/>backToDestinationList(){

              document.<portlet:namespace/>createDestinationForm.action = "<portlet:renderURL portletMode="view"><portlet:param name="processAction" value="viewDestinations"/></portlet:renderURL>";
              document.<portlet:namespace/>createDestinationForm.submit();

           return true;
       }
       </script>

       <br>

        <form name="<portlet:namespace/>createDestinationForm" action="<portlet:actionURL portletMode="view"/>" >
        <input type=hidden name="processaction" value="createDestination"  >
       <table>

       <tr>
         <th colspan="2" align=LEFT>
            Add Queue/Topic
         </th>

       </tr>
         <tr>
             <td  align=LEFT>
                  Message Destination Name
             </td>
             <td  align=LEFT>
                   <input type="text" name="destinationMessageDestinationName" value=""/>
             </td>
         </tr>
         <tr>
             <td  align=LEFT>
                   Destination Physical Name
             </td>
             <td  align=LEFT>
                   <input type="text" name="destinationPhysicalName" value=""/>
             </td>
         </tr>
         <tr>
             <td  align=LEFT>
                   Type
             </td>
             <td  align=LEFT>
               <select name="destinationType">
                  <option value="javax.jms.Queue" selected="true" >QUEUE</option>
                  <option value="javax.jms.Topic" >TOPIC</option>
               </select>
             </td>
         </tr>
         <tr>
             <td  align=LEFT>
                   Application Name
             </td>
             <td  align=LEFT>
                   <input type="text" name="destinationApplicationName" value="null"/>
               </select>
             </td>
         </tr>
         <tr>
             <td  align=LEFT>
                    Module Name
             </td>
             <td  align=LEFT>
                   <input type="text" name="destinationModuleName" value="defaultJMS"/>
               </select>
             </td>
         </tr>
         <tr>
                <td colspan="2" align="center" class="formElement">
    			<input type="submit" value="Submit" 
	                onclick="return <portlet:namespace/>validateForm()">
 				<input type="reset" value="Clear"> 
				<input type="submit" value="Back"  onClick="<portlet:namespace/>backToDestinationList();">
                </td>
         </tr>
        </table>

        </form>

