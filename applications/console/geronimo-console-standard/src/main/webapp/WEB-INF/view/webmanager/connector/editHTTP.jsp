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
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="portlet" uri="http://java.sun.com/portlet" %>
<portlet:defineObjects/>

<script language="JavaScript">
var <portlet:namespace/>formName = "<portlet:namespace/>HttpForm";
var <portlet:namespace/>requiredFields = new Array("host");
var <portlet:namespace/>numericFields = new Array("port", "maxThreads");
function <portlet:namespace/>validateForm(){
    if(!textElementsNotEmpty(<portlet:namespace/>formName, <portlet:namespace/>requiredFields)) {
        return false;
    }
    for(i in <portlet:namespace/>numericFields) {
        if(!checkIntegral(<portlet:namespace/>formName, <portlet:namespace/>numericFields[i])) {
            return false;
        }
    }
    return true;
}
</script>

<form name="<portlet:namespace/>HttpForm" action="<portlet:actionURL/>">
<input type="hidden" name="mode" value="${mode}">
<input type="hidden" name="protocol" value="${protocol}">
<input type="hidden" name="containerURI" value="${containerURI}">
<input type="hidden" name="managerURI" value="${managerURI}">
<c:if test="${mode eq 'save'}">
  <input type="hidden" name="connectorURI" value="${connectorURI}">
</c:if>
<table width="100%"  border="0">

<!-- Current Task -->
<c:choose>
  <c:when test="${mode eq 'add'}">
    <tr><th colspan="2" align="left">Add new ${protocol} listener for ${containerDisplayName}</th></tr>
  </c:when>
  <c:otherwise>
    <tr><th colspan="2" align="left">Edit connector ${displayName}</th></tr>
  </c:otherwise>
</c:choose>

<!-- Name Field -->
<c:if test="${mode eq 'add'}">
  <tr>
    <td><div align="right">Unique Name: </div></td>
    <td><input name="displayName" type="text" size="30"></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>A name that is different than the name for any other web connectors in the server</td>
  </tr>
  <script language="JavaScript">
    <portlet:namespace/>requiredFields = new Array("displayName").concat(<portlet:namespace/>requiredFields);
  </script>
</c:if>
<!-- Host Field -->
  <tr>
    <td><div align="right">Host: </div></td>
    <td>
      <input name="host" type="text" size="30" value="${host}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The host name or IP to bind to.  The normal values are <tt>0.0.0.0</tt> (all interfaces) or <tt>localhost</tt> (local connections only)</td>
  </tr>
<!-- Port Field -->
  <tr>
    <td><div align="right">Port: </div></td>
    <td>
      <input name="port" type="text" size="5" value="${port}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The network port to bind to.</td>
  </tr>
<!-- Max Threads Field -->
  <tr>
    <td><div align="right">Max Threads: </div></td>
    <td>
      <input name="maxThreads" type="text" size="3" value="${maxThreads}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum number of threads this connector should use to handle incoming requests</td>
  </tr>
  
<%-- TOMCAT CONNECTOR SPECIFIC ATTRIBUTES: START --%>
<c:if test="${server eq 'tomcat'}">
  <tr>
    <th align="right" width="175">Show&nbsp;all&nbsp;fields:</th>
    <td><input type="checkbox" name="showAllFields" onClick="document.getElementById('<portlet:namespace/>propsDiv').style.display=this.checked ? 'block' : 'none';" CHECKED/></td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>Check this box to view and configure all attributes.  Uncheck to hide. Note: Hiding will not remove the field values.</td>
  </tr>
</table>
<div id="<portlet:namespace/>propsDiv" style="display: block">
<table width="100%"  border="0">
<%-- Common Connector Attributes --%>
<!-- AllowTrace Field -->
  <tr>
    <td><div align="right">AllowTrace: </div></td>
    <td>
      <input type="checkbox" name="allowTrace" <c:if test="${!empty allowTrace}">CHECKED </c:if>/>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>Check to enable the TRACE HTTP method.</td>
  </tr>
<!-- EmptySessionPath Field -->
  <tr>
    <td><div align="right">EmptySessionPath: </div></td>
    <td>
      <input type="checkbox" name="emptySessionPath" <c:if test="${!empty emptySessionPath}">CHECKED </c:if>/>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>If checked, all paths for session cookies will be set to /.</td>
  </tr>
<!-- EnableLookups Field -->
  <tr>
    <td><div align="right">EnableLookups: </div></td>
    <td>
      <input type="checkbox" name="enableLookups" <c:if test="${!empty enableLookups}">CHECKED </c:if>/>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>Check if you want calls to request.getRemoteHost() to perform DNS lookups in order to return the actual host name of the remote client.  By default, DNS lookups are enabled.</td>
  </tr>
<!-- MaxPostSize Field -->
  <tr>
    <td><div align="right">MaxPostSize: </div></td>
    <td>
      <input name="maxPostSize" type="text" size="10" value="${maxPostSize}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum size in bytes of the POST which will be handled by the container FORM URL parameter parsing. The limit can be disabled by setting this attribute to a value less than or equal to 0. Default value is 2097152 (2 megabytes)</td>
  </tr>
<!-- MaxSavePostSize Field -->
  <tr>
    <td><div align="right">MaxSavePostSize: </div></td>
    <td>
      <input name="maxSavePostSize" type="text" size="10" value="${maxSavePostSize}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum size in bytes of the POST which will be saved/buffered by the container during FORM or CLIENT-CERT authentication. The limit can be disabled by setting this attribute to -1. Setting the attribute to zero will disable the saving of POST data during authentication . Default value is 4096 (4 kilobytes).</td>
  </tr>
<!-- ProxyName Field -->
  <tr>
    <td><div align="right">ProxyName: </div></td>
    <td>
      <input name="proxyName" type="text" size="30" value="${proxyName}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>If this Connector is being used in a proxy configuration, configure this attribute to specify the server name to be returned for calls to request.getServerName().</td>
  </tr>
<!-- ProxyPort Field -->
  <tr>
    <td><div align="right">ProxyPort: </div></td>
    <td>
      <input name="proxyPort" type="text" size="5" value="${proxyPort}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>If this Connector is being used in a proxy configuration, configure this attribute to specify the server port to be returned for calls to request.getServerPort().</td>
  </tr>
<!-- RedirectPort Field -->
  <tr>
    <td><div align="right">RedirectPort: </div></td>
    <td>
      <input name="redirectPort" type="text" size="5" value="${redirectPort}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>If this Connector is supporting non-SSL requests, and a request is received for which a matching <security-constraint> requires SSL transport, Catalina will automatically redirect the request to the port number specified here.</td>
  </tr>
<!-- URIEncoding Field -->
  <tr>
    <td><div align="right">URIEncoding: </div></td>
    <td>
      <input name="URIEncoding" type="text" size="30" value="${URIEncoding}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>This specifies the character encoding used to decode the URI bytes, after %xx decoding the URL. Default is ISO-8859-1.</td>
  </tr>
<!-- UseBodyEncodingForURI Field -->
  <tr>
    <td><div align="right">UseBodyEncodingForURI: </div></td>
    <td>
      <input type="checkbox" name="useBodyEncodingForURI" <c:if test="${!empty useBodyEncodingForURI}">CHECKED </c:if>/>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>Check this if the encoding specified in contentType should be used for URI query parameters, instead of using the URIEncoding.</td>
  </tr>
<!-- UseIPVHosts Field -->
  <tr>
    <td><div align="right">UseIPVHosts: </div></td>
    <td>
      <input type="checkbox" name="useIPVHosts" <c:if test="${!empty useIPVHosts}">CHECKED </c:if>/>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>Check this to cause Tomcat to use the IP address that the request was recieved on to determine the Host to send the request to.</td>
  </tr>
<!-- XpoweredBy Field -->
  <tr>
    <td><div align="right">XpoweredBy: </div></td>
    <td>
      <input type="checkbox" name="xpoweredBy" <c:if test="${!empty xpoweredBy}">CHECKED </c:if>/>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>Check this to cause Tomcat to advertise support for the Servlet specification using the header recommended in the specification.</td>
  </tr>

<%-- HTTP Attributes --%>  
<!-- AcceptCount Field -->
  <tr>
    <td><div align="right">AcceptCount: </div></td>
    <td>
      <input name="acceptCount" type="text" size="5" value="${acceptCount}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum queue length for incoming connection requests when all possible request processing threads are in use. Any requests received when the queue is full will be refused. The default value is 10.</td>
  </tr>
<!-- BufferSize Field -->
  <tr>
    <td><div align="right">BufferSize: </div></td>
    <td>
      <input name="bufferSize" type="text" size="5" value="${bufferSize}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The size (in bytes) of the buffer to be provided for input streams created by this connector. By default, buffers of 2048 bytes will be provided.</td>
  </tr>
<!-- CompressableMimeType Field -->
  <tr>
    <td><div align="right">CompressableMimeType: </div></td>
    <td>
      <input name="compressableMimeType" type="text" size="30" value="${compressableMimeType}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The value is a comma separated list of MIME types for which HTTP compression may be used. The default value is text/html,text/xml,text/plain.</td>
  </tr>
<!-- Compression Field -->
  <tr>
    <td><div align="right">Compression: </div></td>
    <td>
      <input name="compression" type="text" size="30" value="${compression}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The Connector may use HTTP/1.1 GZIP compression in an attempt to save server bandwidth. The acceptable values for the parameter is "off" (disable compression), "on" (allow compression, which causes text data to be compressed), "force" (forces compression in all cases), or a numerical integer value (which is equivalent to "on", but specifies the minimum amount of data before the output is compressed). If the content-length is not known and compression is set to "on" or more aggressive, the output will also be compressed. If not specified, this attribute is set to "off".</td>
  </tr>
<!-- ConnectionLinger Field -->
  <tr>
    <td><div align="right">ConnectionLinger: </div></td>
    <td>
      <input name="connectionLinger" type="text" size="5" value="${connectionLinger}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The number of milliseconds during which the sockets used by this Connector will linger when they are closed. The default value is -1 (socket linger is disabled).</td>
  </tr>
<!-- ConnectionTimeout Field -->
  <tr>
    <td><div align="right">ConnectionTimeout: </div></td>
    <td>
      <input name="connectionTimeout" type="text" size="5" value="${connectionTimeout}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The number of milliseconds this Connector will wait, after accepting a connection, for the request URI line to be presented. The default value is 60000 (i.e. 60 seconds).</td>
  </tr>
<!-- KeepAliveTimeout Field -->
  <tr>
    <td><div align="right">KeepAliveTimeout: </div></td>
    <td>
      <input name="keepAliveTimeout" type="text" size="5" value="${keepAliveTimeout}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The number of milliseconds this Connector will wait, subsequent request before closing the connection. The default value is to use the value that has been set for the connectionTimeout.</td>
  </tr>
<!-- DisableUploadTimeout Field -->
  <tr>
    <td><div align="right">DisableUploadTimeout: </div></td>
    <td>
      <input type="checkbox" name="disableUploadTimeout" <c:if test="${!empty disableUploadTimeout}">CHECKED </c:if>/>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>This flag allows the servlet container to use a different, longer connection timeout while a servlet is being executed, which in the end allows either the servlet a longer amount of time to complete its execution, or a longer timeout during data upload. If not specified, this attribute is set to "true".</td>
  </tr>
<!-- MaxHttpHeaderSize Field -->
  <tr>
    <td><div align="right">MaxHttpHeaderSize: </div></td>
    <td>
      <input name="maxHttpHeaderSize" type="text" size="10" value="${maxHttpHeaderSize}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum size of the request and response HTTP header, specified in bytes. If not specified, this attribute is set to 4096 (4 KB).</td>
  </tr>
<!-- maxKeepAliveRequests Field -->
  <tr>
    <td><div align="right">MaxKeepAliveRequests: </div></td>
    <td>
      <input name="maxKeepAliveRequests" type="text" size="10" value="${maxKeepAliveRequests}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum number of HTTP requests which can be pipelined until the connection is closed by the server. Setting this attribute to 1 will disable HTTP/1.0 keep-alive, as well as HTTP/1.1 keep-alive and pipelining. Setting this to -1 will allow an unlimited amount of pipelined or keep-alive HTTP requests. If not specified, this attribute is set to 100.</td>
  </tr>
<!-- maxSpareThreads Field -->
  <tr>
    <td><div align="right">MaxSpareThreads: </div></td>
    <td>
      <input name="maxSpareThreads" type="text" size="10" value="${maxSpareThreads}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The maximum number of unused request processing threads that will be allowed to exist until the thread pool starts stopping the unnecessary threads. The default value is 50.</td>
  </tr>
<!-- minSpareThreads Field -->
  <tr>
    <td><div align="right">MinSpareThreads: </div></td>
    <td>
      <input name="minSpareThreads" type="text" size="10" value="${minSpareThreads}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The number of request processing threads that will be created when this Connector is first started. The connector will also make sure it has the specified number of idle processing threads available. This attribute should be set to a value smaller than that set for maxThreads. The default value is 4.</td>
  </tr>
<!-- noCompressionUserAgents Field -->
  <tr>
    <td><div align="right">NoCompressionUserAgents: </div></td>
    <td>
      <input name="noCompressionUserAgents" type="text" size="30" value="${noCompressionUserAgents}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The value is a comma separated list of regular expressions matching user-agents of HTTP clients for which compression should not be used, because these clients, although they do advertise support for the feature, have a broken implementation. The default value is an empty String (regexp matching disabled).</td>
  </tr>
<!-- restrictedUserAgents Field -->
  <tr>
    <td><div align="right">RestrictedUserAgents: </div></td>
    <td>
      <input name="restrictedUserAgents" type="text" size="30" value="${restrictedUserAgents}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The value is a comma separated list of regular expressions matching user-agents of HTTP clients for which HTTP/1.1 or HTTP/1.0 keep alive should not be used, even if the clients advertise support for these features. The default value is an empty String (regexp matching disabled).</td>
  </tr>
<!-- server Field -->
  <tr>
    <td><div align="right">Server: </div></td>
    <td>
      <input name="serverAttribute" type="text" size="30" value="${serverAttribute}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The Server header for the http response. Unless your paranoid, you won't need this feature. (No offense.  The description is taken from Tomcat documentation.)</td>
  </tr>
<!-- socketBuffer Field -->
  <tr>
    <td><div align="right">SocketBuffer: </div></td>
    <td>
      <input name="socketBuffer" type="text" size="10" value="${socketBuffer}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The size (in bytes) of the buffer to be provided for socket output buffering. -1 can be specified to disable the use of a buffer. By default, a buffers of 9000 bytes will be used.</td>
  </tr>
<!-- strategy Field -->
  <tr>
    <td><div align="right">Strategy: </div></td>
    <td>
      <input name="strategy" type="text" size="30" value="${strategy}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The thread pooling strategy which will be used. The default strategy does not use a master thread, but a more conventional strategy using a master listener thread can be used by setting "ms" as this attribute's value. The master strategy will work significantly better using the threadPriority attribute, which will apply only to the thread which listens on the server socket. This is set to lf by default.</td>
  </tr>
<!-- tcpNoDelay Field -->
  <tr>
    <td><div align="right">TcpNoDelay: </div></td>
    <td>
      <input type="checkbox" name="tcpNoDelay" <c:if test="${!empty tcpNoDelay}">CHECKED </c:if>/>
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>If checked, the TCP_NO_DELAY option will be set on the server socket, which improves performance under most circumstances.</td>
  </tr>
<!-- threadPriority Field -->
  <tr>
    <td><div align="right">ThreadPriority: </div></td>
    <td>
      <input name="threadPriority" type="text" size="30" value="${threadPriority}">
    </td>
  </tr>
  <tr>
    <td><div align="right"></div></td>
    <td>The priority of the request processing threads within the JVM. The default value is java.lang.Thread#NORM_PRIORITY. See the JavaDoc for the java.lang.Thread class for more details on what this priority means.</td>
  </tr>
  
</table>
</div>
<table width="100%"  border="0">
  <tr>
    <td width="175"><div align="right">&nbsp;</div></td>
    <td></td>
  </tr>
</c:if>
<%-- TOMCAT CONNECTOR SPECIFIC ATTRIBUTES: END --%>

<!-- Submit Button -->
  <tr>
    <td><div align="right"></div></td>
    <td>
      <input name="submit" type="submit" value="Save" onClick="return <portlet:namespace/>validateForm();">
      <input name="reset" type="reset" value="Reset">
      <input name="submit" type="submit" value="Cancel">
    </td>    
  </tr>
</table>
</form>
<a href='<portlet:actionURL portletMode="view">
           <portlet:param name="mode" value="list" />
         </portlet:actionURL>'>List connectors</a>
