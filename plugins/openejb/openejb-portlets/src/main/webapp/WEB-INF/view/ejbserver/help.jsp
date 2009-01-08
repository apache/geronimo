<%--
   Licensed to the Apache Software Foundation (ASF) under one or more
   contributor license agreements.  See the NOTICE file distributed with
   this work for additional information regarding copyright ownership.
   The ASF licenses this file to You under the Apache License, Version 2.0
   (the "License"); you may not use this file except in compliance with
   the License.  You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
--%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<fmt:setBundle basename="openejb-portlet"/>
<p><fmt:message key="portlet.openejb.help.title" /></p>
<h3><fmt:message key="portlet.openejb.help.ejbInfo" /></h3>
<p><fmt:message key="portlet.openejb.help.ejbInfoDetail" />
<ul>
<li><b><fmt:message key="portlet.openejb.help.beanclassname" />:</b> <fmt:message key="portlet.openejb.help.beanclassnamedesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.businesslocal" />:</b> <fmt:message key="portlet.openejb.help.businesslocaldesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.businessremote" />:</b> <fmt:message key="portlet.openejb.help.businessremotedesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.deploymentId" />:</b> <fmt:message key="portlet.openejb.help.deploymentIddesc"/> </li>
<li><b><fmt:message key="portlet.openejb.help.ejbname" />:</b> <fmt:message key="portlet.openejb.help.ejbnamedesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.ejbhome"/>:</b> <fmt:message key="portlet.openejb.help.ejbhomedesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.jndiname"/>:</b> <fmt:message key="portlet.openejb.help.jndinamedesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.localhome"/>:</b> <fmt:message key="portlet.openejb.help.localhomedesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.local"/>:</b> 
<fmt:message key="portlet.openejb.help.localdesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.remote"/>:</b> <fmt:message key="portlet.openejb.help.remotedesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.primarykeyclass"/>:</b> <fmt:message key="portlet.openejb.help.primarykeyclassdesc"/></li>	
<li><b><fmt:message key="portlet.openejb.help.primarykeyfield"/>:</b> <fmt:message key="portlet.openejb.help.primarykeyfielddesc"/></li>
<li><b><fmt:message key="portlet.openejb.help.sei"/>:</b> <fmt:message key="portlet.openejb.help.seidesc"/></li>
</ul>
</p>
<h3><fmt:message key="portlet.openejb.help.containerinfo"/></h3>
<p><fmt:message key="portlet.openejb.help.containerinfodesc"/>
<ul>
  <li><b><fmt:message key="portlet.openejb.help.statelesscontainer"/></b>
    <ul>
      <li><b><fmt:message key="portlet.openejb.help.sltimeout"/>:</b> <fmt:message key="portlet.openejb.help.sltimeoutdesc"/></li>
      <li><b><fmt:message key="portlet.openejb.help.slpoolsize"/>:</b> <fmt:message key="portlet.openejb.help.slpoolsizedesc"/></li>
      <li><b><fmt:message key="portlet.openejb.help.strictpooling"/>:</b> <fmt:message key="portlet.openejb.help.strictpoolingdesc"/></li>
    </ul>
  </li>	
  <li><b><fmt:message key="portlet.openejb.help.statefulcontainer"/></b>    <ul>
      <li><b><fmt:message key="portlet.openejb.help.sftimeout"/>:</b> <fmt:message key="portlet.openejb.help.sftimeoutdesc"/></li>
      <li><b><fmt:message key="portlet.openejb.help.sfpoolsize"/>:</b> <fmt:message key="portlet.openejb.help.sfpoolsizedesc"/></li>
      <li><b><fmt:message key="portlet.openejb.help.passivator"/>:</b> <fmt:message key="portlet.openejb.help.passivatordesc"/>
<fmt:message key="portlet.openejb.help.ki"/>:
<fmt:message key="portlet.openejb.help.ki1"/>
<fmt:message key="portlet.openejb.help.ki2"/>
<fmt:message key="portlet.openejb.help.kid"/></li>
      <li><b><fmt:message key="portlet.openejb.help.bulkpassivate"/>:</b> <fmt:message key="portlet.openejb.help.bulkpassivatedesc"/></li>   
    </ul>
  </li>	
  <li><b><fmt:message key="portlet.openejb.help.mdbcontainer"/></b>
    <ul>
      <li><b><fmt:message key="portlet.openejb.help.instancelimit"/>:</b><fmt:message key="portlet.openejb.help.instancelimitdesc"/></li>
      <li><b><fmt:message key="portlet.openejb.help.activation"/>:</b><fmt:message key="portlet.openejb.help.activationdesc"/></li>
      <li><b><fmt:message key="portlet.openejb.help.mli"/>:</b><fmt:message key="portlet.openejb.help.mlidesc"/></li>
      <li><b><fmt:message key="portlet.openejb.help.ra"/>:</b><fmt:message key="portlet.openejb.help.radesc"/></li>
    </ul>
  </li>	
  <li><b><fmt:message key="portlet.openejb.help.cmpcontainer"/></b><ul>
      <li><b><fmt:message key="portlet.openejb.help.cef"/>:</b> <fmt:message key="portlet.openejb.help.cefdesc"/></li>      
    </ul>
  </li>	
  <li><b><fmt:message key="portlet.openejb.help.bmpcontainer"/></b>
   <ul>
      <li><b><fmt:message key="portlet.openejb.help.bmppoolsize"/>:</b><fmt:message key="portlet.openejb.help.bmppoolsizedesc"/></li>
    </ul>
  </li>
