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


dependencies = {
	layers: [
    // dojo layer
		{
			name: "dojo.js",
			dependencies: [ 
                "dojo.data.ItemFileReadStore",
                "dojo.data.ItemFileWriteStore",
                "dojo.hash",
                "dojo.io.iframe",
                "dojo.parser"
			]
			
		},
        // dijit layer:
        {
            name: "../dijit/dijit.js",
            layerDependencies: [ "dojo.js"],
			dependencies: [
                "dijit.dijit",
                "dijit.Dialog",
                "dijit.Editor",
                "dijit.TitlePane",
                "dijit.Tree",
                "dijit.form.Button",
                "dijit.form.CheckBox",
                "dijit.form.ComboBox",
                "dijit.form.CurrencyTextBox",
                "dijit.form.DateTextBox",
                "dijit.form.FilteringSelect",
                "dijit.form.Form",
                "dijit.form.NumberSpinner",
                "dijit.form.NumberTextBox",
                "dijit.form.RadioButton",
                "dijit.form.Slider",
                "dijit.form.TextBox",
                "dijit.form.Textarea",
                "dijit.form.ValidationTextBox",
                "dijit.layout.AccordionContainer",
                "dijit.layout.ContentPane",
                "dijit.layout.LayoutContainer",
                "dijit.layout.SplitContainer",
                "dijit.layout.TabContainer",
                "dijit.tree.ForestStoreModel"
			]
        },
        // dojox layer:
        {
            name: "../dojox/dojox.js",
            layerDependencies: [ "../dijit/dijit.js"],
			dependencies: [
                "dojox.dojox",
                "dojox.gfx.svg",
                "dojox.charting.Chart2D",
                "dojox.charting.themes.PlotKit.blue",
                "dojox.collections.Dictionary",
                "dojox.fx.easing"
			]
        }

    ],
    
	prefixes: [
		[ "dijit", "../dijit" ],
        [ "dojox", "../dojox" ]
	]
}
