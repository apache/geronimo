<%@ page contentType="image/svg+xml" %>
<svg width="100%" height="100%"
     xmlns="http://www.w3.org/2000/svg"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     onload="initialize(evt, 480, 300, 'callServer()', 'Server Memory Usage')">
    <script type="text/ecmascript" xlink:href="updatingGraphJS.jsp"/>
    <script type="text/ecmascript" xlink:href="memoryGraphJS.jsp"/>
    <script type='text/ecmascript' xlink:href='../dwr/interface/Jsr77Stats.js' />
    <script type='text/ecmascript' xlink:href='../dwr/engine.js' />
    <script type='text/ecmascript' xlink:href='../dwr/util.js' />
</svg>