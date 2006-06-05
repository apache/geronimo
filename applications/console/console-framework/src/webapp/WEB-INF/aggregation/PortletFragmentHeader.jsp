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
<jsp:useBean id="portletInfo" type="org.apache.pluto.portalImpl.aggregation.PortletFragment.PortletInfo" scope="request" />

<!-- inside PortletFragmentHeader -->
                <tr>
			            <td>
								  <div class="Content"> 
                    <table width="100%"  border="0" cellspacing="0" cellpadding="0"> 
                      <tr> 
                        <td class="TopLeft"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td> 
                        <td>
												  <table width="100%"  border="0" cellspacing="0" cellpadding="0"> 
                            <tr> 
															<!-- portlet header -->
															<td class="Title">
																<strong>
																	<%= portletInfo.getTitle() %>
																</strong>
															</td> 
															<!-- portlet header -->
															<!-- portlet header links -->
															<td class="Title" align='right'>
																<%
																	java.util.List modeList = portletInfo.getAvailablePortletModes();
																	java.util.Collections.sort(modeList);
																	for (java.util.Iterator iter = modeList.iterator(); iter.hasNext();) 
																	{
																		org.apache.pluto.portalImpl.aggregation.PortletFragment.PortletModeInfo modeInfo = (org.apache.pluto.portalImpl.aggregation.PortletFragment.PortletModeInfo) iter.next();
  																	if (!modeInfo.isCurrent()) 
		  															{
						  												%><a href="<%=modeInfo.getUrl() %>"><%=modeInfo.getName()%></a>&nbsp;<%
														  			}
																		else
																		{
																		  %>[<%=modeInfo.getName()%>]&nbsp;<%
																	  }
																	}
																%>
												      </td>
															<!-- portlet header links -->
                            </tr> 
                          </table>
													</td> 
                        <td class="TopRight"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td> 
                      </tr> 
                    </table> 
								    </div> 
									</td>
							  </tr>
								<tr>
								  <td>
									  <div class="Content">
                      <table width="100%"  border="0" cellspacing="0" cellpadding="0"> 
											  <tr>
												  <td class="Left"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="5px"></td>
												  <td class="Body"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td>
													<td class="Right"><img src="<%=request.getContextPath()%>/images/spacer.gif" width="1" height="1"></td>
											  </tr>
                        <tr> 
                          <td class="Left">&nbsp;</td> 
                          <td class="Body">
<!-- inside PortletFragmentHeader -->
							