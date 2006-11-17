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
<%@ page contentType="text/javascript" %>
// =============== Infrastructure ==============
// SVG namespace
var svgNS = "http://www.w3.org/2000/svg";
// the SVG document
var graphDocument = null;
// The main <g> element that groups all graph elements
var graphGroup = null;
// The selected point
var selectedPoint = null;
// All points
var allPoints = new Array(60);
// The path through all of the points
var dataPath = null;
// The space to the left of the graph
var xOffset = 100;
// The space above the graph
var yOffset = 50;
// The height of the graph
var graphHeight;
// The length of the graph
var graphLength;

// =============== Runtime Data ==============
// Data
var data = new Array(60);
// Y-Max
var yMax = 1;
// Y Caption
var yCaption = null;
// Bottom Caption
var lowerCaption = null;

/** Saved bootstrap reference **/
function initialize(evt, length, height, updater, caption) {
  // Get the document
  graphDocument = evt.target.ownerDocument;
  renderChart(length, height, updater, caption);
}

/** Sets up the initial objects and draws the chart **/
function renderChart(length, height, updater, captionText) {
  graphLength = length;
  graphHeight = height;
  for(var i=0; i<data.length; i++) {
    data[i] = 0;
  }

  // Create the group
  graphGroup = graphDocument.createElementNS(svgNS, "g");

  // Create the caption
  var caption = graphDocument.createElementNS(svgNS, "text");
  caption.setAttribute("x", (xOffset+length)/2);
  caption.setAttribute("y", 20);
  caption.setAttribute("text-anchor", "middle");
  caption.appendChild(graphDocument.createTextNode(captionText));
  graphGroup.appendChild(caption);

  // Create the axes
  var axisGroup = graphDocument.createElementNS(svgNS, "g");
  // create the X axis line as a <path> element
  var axisPath = graphDocument.createElementNS(svgNS, "path");
  // X axis line settings
  axisPath.setAttribute("stroke", "black");
  axisPath.setAttribute("stroke-width", "2");
  axisPath.setAttribute("fill", "none");
  axisPath.setAttribute("d", "M "+xOffset+" "+yOffset+" L "+xOffset+" "+(yOffset+graphHeight)+" L "+(xOffset+graphLength)+" "+(yOffset+graphHeight));
  axisGroup.appendChild(axisPath);
  var topY = graphDocument.createElementNS(svgNS, "text");
  topY.setAttribute("x", 1);
  topY.setAttribute("y", yOffset+10);
  yCaption = graphDocument.createTextNode("Max MB");
  topY.appendChild(yCaption);
  axisGroup.appendChild(topY);
  // Done with axes
  graphGroup.appendChild(axisGroup);

  // Create the points
  for(var i=0; i<allPoints.length; i++) {
    allPoints[i] = graphDocument.createElementNS(svgNS, "circle");
    allPoints[i].setAttribute("cx", xOffset+(i*graphLength/(data.length-1)));
    allPoints[i].setAttribute("cy", yOffset+graphHeight);
    allPoints[i].setAttribute("r", 2);
    allPoints[i].setAttribute("fill", "blue");
  }

  // Create the data path
  dataPath = graphDocument.createElementNS(svgNS, "path");
  dataPath.setAttribute("stroke", "black");
  dataPath.setAttribute("stroke-width", "1");
  dataPath.setAttribute("fill", "none");

  // add the data path to the chart group
  graphGroup.appendChild(dataPath);
  // add the points on top of the data path
  for(var i = 0; i < allPoints.length; i++) {
    graphGroup.appendChild(allPoints[i]);
  }

  // Render initial data
  syncPoints();
  setPathDef();

  graphDocument.documentElement.appendChild(graphGroup);
  setTimeout(updater, 100);
}

/** Updates the points to the most current data **/
function syncPoints() {
//  for(var i = 0; i < data.length; i++) {
//    if(data[i] > yMax) {
//      yMax = data[i] * 1.3;
//    }
//  }
  var bottom = yOffset+graphHeight;
  var ratio = graphHeight/yMax;
  for(var i = 0; i < data.length; i++) {
    allPoints[i].setAttribute("cy", bottom-(data[i]*ratio));
  }
}

/** Updates the path to go through all the points **/
function setPathDef() {
  var pathDef = "";
  for(var i = 0; i < allPoints.length; i++) {
    if(pathDef == "") {
      pathDef += "M "+allPoints[i].getAttribute("cx")+" "+allPoints[i].getAttribute("cy");
    } else {
      pathDef += "L "+allPoints[i].getAttribute("cx")+" "+allPoints[i].getAttribute("cy");
    }
  }
  dataPath.setAttribute("d", pathDef);
}

/** Adds a data point **/
function addPoint(value) {
  for(var i=0; i<data.length-1; i++) {
    data[i] = data[i+1];
  }
  data[data.length-1] = value
  syncPoints();
  setPathDef();
}

/** Ensures the Y axis is at least this large **/
function checkMaxValue(value, text) {
  if(value > yMax) {
    yMax = value;
    yCaption.data = text;
  }
}

/** Sets the maximum Y value to the specified value **/
function setMaxValue(value, text) {
  yMax = value;
  yCaption.data = text;
}

/** Sets the lower caption **/
function setLowerCaption(text) {
  if(lowerCaption == null) {
    var caption = graphDocument.createElementNS(svgNS, "text");
    caption.setAttribute("x", (xOffset+graphLength)/2);
    caption.setAttribute("y", yOffset+graphHeight+30);
    caption.setAttribute("text-anchor", "middle");
    lowerCaption = graphDocument.createTextNode("   "+text+"   ");
    caption.appendChild(lowerCaption);
    graphGroup.appendChild(caption);
  }
  lowerCaption.data = "  "+text+"  ";
}
