<!--
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
-->
<script language="JavaScript">
var formID = '<%XSRF_UNIQUEID%>';
function updateLinks() {
    var elements = document.all ? document.all : document.getElementsByTagName('*');
    for (var i=0; i<elements.length; i++) {   
        var link = elements[i].getAttribute('href');
        if (link != null && isURL(link) && link.indexOf('?') != -1) {
            // add formId only if other attributes are present in link
           	// Note: we cannot use setAttribute due to IE issues so we are using element.*=
          	elements[i].href = link + '&formId=' + formID;
        }
    }
}

function updateForms() {
    var forms = document.getElementsByTagName('form');
    for (i=0; i<forms.length; i++) {
        if (forms[i].getAttribute('enctype').toLowerCase() == 'multipart/form-data'){ // add formId in action link
            var link = forms[i].getAttribute('action');
            if (link != null && isURL(link)) {
                if (link.indexOf('?') == -1) {
            		    link = link + '?'
                }
           	    // Note: we cannot use setAttribute due to IE issues so we are using element.*=
           	    forms[i].action = link + '&formId=' + formID;
            }
        } else {
            var input = document.createElement('input');
            if (document.all) {		//IE
                input.type = 'hidden';
                input.name = 'formId';
                input.value = formID;
            } else if (document.getElementById) {	//firefox
                input.setAttribute('type', 'hidden');
                input.setAttribute('name', 'formId');
                input.setAttribute('value', formID);
            }
            forms[i].appendChild(input);
        }
    }
}

function updateOnclickLink(element) {
    var link = element.getAttribute('onclick');
    if ((link != null) && (link != '')) {
        var start = link.indexOf('/');
        if (start != -1) {
            var end = link.indexOf('?',start);
            if (end != -1) {
                var newlink = link.substring(0,end+1) + 'formId=' + formID + '&' + link.substring(end+1);
                var new_onclick = function() { eval(newlink); };
                element.onclick=new_onclick;
            }
        }
    }
    return false;
}

function isURL(link) {
   	if ((typeof link == 'string') && link.constructor == String){
   	   	if (link != '' && (link.substring(0, 4) == 'http' || link.substring(0, 1) == '/')){
   	    	return true;
   	    }
   	}
   	return false;
}
updateLinks();
updateForms();
</script>
</body>
