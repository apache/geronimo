//======================================================================
//   Licensed to the Apache Software Foundation (ASF) under one or more
//   contributor license agreements.  See the NOTICE file distributed with
//   this work for additional information regarding copyright ownership.
//   The ASF licenses this file to You under the Apache License, Version 2.0
//   (the "License"); you may not use this file except in compliance with
//   the License.  You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.
//======================================================================

// $Rev$ $Date$



1 copy dojo-release-1.5.1-src(down from dojo site) to ../../
2 run the release.bat(in windows) to generate the compressed dojo files under directory:release;
3 remove the redundant files in the release directory, the final constructure is:
dojo
--dijit
----icons
----nls
----themes
------a11y
------claro
------dijit.css
------dijit_rtl.css
----dijit.js
--dojo
----nls
----resources
----dojo.js
--dojox
----resources
----dojox.js
4 copy the the generated files under webapp
