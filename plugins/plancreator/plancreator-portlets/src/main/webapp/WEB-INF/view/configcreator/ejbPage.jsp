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

<%-- $Rev$ $Date$ --%>

<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>
<portlet:defineObjects />

<script type="text/javascript" src="/dojo/dojo.js"></script>

<script type="text/javascript">
  dojo.require("dojo.lang.*");
  dojo.require("dojo.widget.*");
  // Pane includes
  dojo.require("dojo.widget.ContentPane");
  dojo.require("dojo.widget.LayoutContainer"); // Before: LayoutPane
  dojo.require("dojo.widget.SplitContainer"); // Before: SplitPane
  // Tree includes
  dojo.require("dojo.widget.Tree");
  dojo.require("dojo.widget.TreeBasicController");
  dojo.require("dojo.widget.TreeContextMenu");
  dojo.require("dojo.widget.TreeSelector");
  // Etc includes
  dojo.require("dojo.widget.SortableTable");
  dojo.require("dojo.widget.ComboBox");
  dojo.require("dojo.widget.Tooltip");
  dojo.require("dojo.widget.validate");
  // Includes Dojo source for debugging
  // dojo.hostenv.writeIncludes();
</script>

<script>
  dojo.addOnLoad(function() {
  }
</script>

<div dojoType="dijit.layout.SplitContainer" id="rightPane"
        orientation="horizontal"  sizerWidth="5"  activeSizing="0">
        <div id="listPane" dojoType="dijit.layout.ContentPane" sizeMin="20" sizeShare="20">
              Tree goes here
        </div>
                                       
        <div id="message" dojoType="dijit.layout.ContentPane" sizeMin="20" sizeShare="80">
            Editors goes here
        </div>
</div>
