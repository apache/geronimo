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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>
<portlet:defineObjects/>

<input type="hidden" name="mode" value="results-after"/>
<input type="hidden" name="os" value="${model.os}"/>
<input type="hidden" name="addAjpPort" value="${model.addAjpPort}"/>
<input type="hidden" name="workersPath" value="${model.workersPath}"/>
<input type="hidden" name="logFilePath" value="${model.logFilePath}"/>


<p><b>Apache mod_jk</b> -- Configuration Results</p>

<p>This page lists the things you must do to enable <tt>mod_jk</tt> in Apache
    and configure it to talk to Geronimo.</p>

<h2>Step 1: Configure Geronimo for AJP</h2>

<c:choose>
    <c:when test="${empty model.addAjpPort}">
        <p>This Geronimo configuration already has an AJP listener running on port ${ajpPort}.
            Nothing further needs to be done here.</p>
    </c:when>
    <c:otherwise>
        <p>An AJP lisstner was added on port ${ajpPort}.  Nothing further needs to be done here.</p>
    </c:otherwise>
</c:choose>

<h2>Step 2: Install <tt>mod_jk</tt></h2>

<c:choose>
    <c:when test="${model.os != '' && model.os != 'Other'}">
        <p>On ${model.os}, you can install <tt>mod_jk</tt> using an RPM.  This does
        not activate it in Apache, but it at least installs the binary in the right
        location.  To install, find the following RPM on your install media and
        run a command like this:</p>
        <pre>
rpm -Uvh <c:choose><c:when test="${model.os == 'Fedora Core 4'}">mod_jk-1.2.6-3jpp_4fc.i586.rpm</c:when><c:when test="${model.os == 'SuSE Pro 9.0'}">apache2-jakarta-tomcat-connectors-4.1.27-32.i586.rpm</c:when><c:when test="${model.os == 'SuSE Pro 9.1'}">apache2-jakarta-tomcat-connectors-5.0.19-13.i586.rpm</c:when><c:when test="${model.os == 'SuSE Pro 9.2'}">mod_jk-ap20-4.1.30-3.i586.rpm</c:when><c:when test="${model.os == 'SuSE Pro 9.3'}">mod_jk-ap20-4.1.30-3.i586.rpm</c:when><c:when test="${model.os == 'SuSE Linux 10.0'}">mod_jk-ap20-4.1.30-3.i586.rpm</c:when></c:choose>
        </pre>
        <p>Once the <tt>mod_jk</tt> RPM is installed, you can activate it by
   <c:choose>
     <c:when test="${fn:startsWith(model.os, 'SuSE')}">
          editing <tt>/etc/sysconfig/apache2</tt> and adding <tt>jk</tt> to the value configured for
          <tt>APACHE_MODULES</tt> and then running (as root) <tt>SuSEconfig</tt> followed by
          <tt>rcapache2 start</tt> (or <tt>rcapache2 restart</tt>).
     </c:when>
     <c:otherwise>
          adding the following line to <tt>/etc/httpd/conf/httpd.conf</tt>:</p>
<pre>
LoadModule jk_module modules/mod_jk.so
</pre>
        <p>Then you can start Apache by running <tt>service httpd start</tt> (or <tt>service httpd restart</tt>).
     </c:otherwise>
   </c:choose>
        </p>
    </c:when><c:otherwise>
        <p>Based on your operating system selection, I don't have specific instructions
            to install <tt>mod_jk</tt>.  You may be able to get a binary release from
            <a href="http://www.apache.org/dist/tomcat/tomcat-connectors/jk/binaries/">http://www.apache.org/dist/tomcat/tomcat-connectors/jk/binaries/</a>.
            Otherwise, you'll have to build from source.</p>
        <p>To enable <tt>mod_jk</tt> in Apache, you typically need to install the <tt>.so</tt> (Linux/UNIX/Mac OS X)
          or <tt>.dll</tt> (Windows) and then add a line to the <tt>httpd.conf</tt> file like this:</p>
<pre>
LoadModule jk_module modules/mod_jk.so
</pre>
        <p>Finally, start or restart Apache.</p>
    </c:otherwise>
</c:choose>

<h2>Step 3: Create a <tt>workers.properties</tt> file</h2>

<p>You've decided to save this file at <tt>${model.workersPath}</tt>.  Please save the following content
  to that file.</p>
<pre>
worker.list=geronimo_ajp13
worker.geronimo_ajp13.port=${ajpPort}
worker.geronimo_ajp13.host=localhost
worker.geronimo_ajp13.type=ajp13
</pre>

<h2>Step 4: Apache Configuration</h2>

<p>The following information needs to go into your Apache Configuration.</p>

<c:choose>
  <c:when test="${fn:startsWith(model.os, 'SuSE')}">
       <p>For SuSE, this should be saved to a file in the Apache conf.d dir, e.g.
         write this content to <tt>/etc/apache2/conf.d/geronimo-jk.conf</tt></p>
  </c:when>
  <c:otherwise>
      <p>This information should be added to the Apache configuration file.  This is often
      at a location such as <tt>/etc/httpd/conf/httpd.conf</tt></p>
  </c:otherwise>
</c:choose>

<pre>
&lt;IfModule mod_jk.c&gt;
    JkWorkersFile ${model.workersPath}
    JkLogFile ${model.logFilePath}
    JkLogLevel error
<c:forEach var="web" items="${model.webApps}"><c:if test="${web.enabled}">
    JkMount ${web.contextRoot} ajp13<c:if test="${web.serveStaticContent}">
    Alias ${web.contextRoot} "${web.webAppDir}"
    &lt;Directory "${web.webAppDir}"&gt;
        Options Indexes FollowSymLinks
        allow from all
    &lt;/Directory&gt;
    &lt;Location "${web.contextRoot}/WEB-INF/"&gt;
        AllowOverride None
        deny from all
    &lt;/Location&gt;
</c:if></c:if></c:forEach>
&lt;/IfModule&gt;
</pre>

<h2>Step 5: Restart Apache</h2>

<p>With those steps completed, Apache should be ready to go!  Start Geronimo and restart Apache
and try accessing a Geronimo web application through an Apache URL.  If there are any problems,
check the Apache error log, and the mod_jk error log (at <tt>${model.logFilePath}</tt>).</p>

<p><a href="<portlet:actionURL portletMode="view">
              <portlet:param name="mode" value="index-before" />
            </portlet:actionURL>">Done</a></p>
