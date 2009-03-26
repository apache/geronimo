/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
<script language="JavaScript">
var formID = '<%XSRF_UNIQUEID%>';
function updateLinks() {
    var elements = document.all ? document.all : document.getElementsByTagName('*');
    var len = elements.length;
    for (var i=0; i<len; i++) {
        var element = elements[i];      
        updateLink(element, 'src');
        updateLink(element, 'href');
//        updateOnclickLink(element);
    }
}
function updateForms() {
   var forms = document.getElementsByTagName('form');
   for (i=0; i<forms.length; i++) {
       var input = document.createElement('input');
       if (document.all) {
          input.type = 'hidden';
          input.name = 'formId';
          input.value = formID;
       } else if (document.getElementById) {
          input.setAttribute('type', 'hidden');
          input.setAttribute('name', 'formId');
          input.setAttribute('value', formID);
       }
       forms[i].appendChild(input);
   }
}
function updateLink(element, attr) {
    var link = element.getAttribute(attr);
    if ((link != null) && (link != '') && isURL(link)) {
        var i = link.indexOf('?');
        // add formId only if other attributes are present in link
        if (i != -1) {
            link = link + '&formId=' + formID;
            // Note: we cannot use setAttribute due to IE issues so we are using element.*=
            if (attr.substring(0,3) == 'src') {
                element.src=link;
            }
            else {
                element.href=link;
            }
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
    var rc = 0;
    if (link.substring(0, 4) == 'http' || link.substring(0, 1) == '/') {
        rc = 1;
    }
    return rc;
}
updateLinks();
updateForms();
</script>
</body>