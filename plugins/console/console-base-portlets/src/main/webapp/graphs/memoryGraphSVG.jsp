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
<%@ page contentType="image/svg+xml" %>
<svg width="100%" height="100%"
     xmlns="http://www.w3.org/2000/svg"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     onload="initialize(evt, 480, 300, 'callServer()', 'Server Memory Usage')">
    <script type="text/ecmascript" xlink:href="updatingGraphJS.jsp"/>
    <script type="text/ecmascript" xlink:href="memoryGraphJS.jsp"/>
    <script type='text/ecmascript' xlink:href='../../dwr/interface/Jsr77Stats.js' />
    <script type='text/ecmascript' xlink:href='../../dwr/engine.js' />
    <script type='text/ecmascript' xlink:href='../../dwr/util.js' />
</svg>
