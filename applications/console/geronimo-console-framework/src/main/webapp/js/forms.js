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
function textElementsNotEmpty(formName, elementNameArray){
    var obj;
    for(i in elementNameArray){
        var elem = elementNameArray[i];
        obj = eval("document.forms['" + formName + "'].elements['"+ elem +"']");
        if(isEmptyString(obj.value)){
            alert(elem + " must not be empty.");
            obj.focus(); 
            return false;             
        }
    }
    return true;
}
function isEmptyString(value){
    return value.length < 1;
}
function checkIntegral(formName, elementName){
    var obj = eval("document.forms['" + formName + "'].elements['"+ elementName +"']");
    if(isIntegral(obj.value)) return true;
    else{
        alert(elementName + " must be an integer.");
        obj.focus();
        return false;
    }
}

function isIntegral(value){
    if(value.length < 1) return false;
    var ints = "1234567890";
    for(i = 0; i < value.length; i++){
        if(ints.indexOf(value.charAt(i)) < 0) return false;
    }
    return true;
}

function checkDateMMDDYYYY(formName, elementName) {
    var obj = eval("document.forms['" + formName + "'].elements['"+ elementName +"']");
    if(validDateMMDDYYYY(obj.value)) return true;
    else{
        alert(elementName + " must be a date in MM/DD/YYYY format.");
        obj.focus();
        return false;
    }
}

function checkDateMMDDYY(formName, elementName) {
    var obj = eval("document.forms['" + formName + "'].elements['"+ elementName +"']");
    if(validDateMMDDYY(obj.value)) return true;
    else{
        alert(elementName + " must be a date in MM/DD/YY format.");
        obj.focus();
        return false;
    }
}

function validDateMMDDYYYY(inpDate) {
    var d0 = new Date(inpDate);
    var mm = (d0.getMonth() < 9 ? '0' : '') + (d0.getMonth()+1);
    var dd = (d0.getDate() < 10 ? '0' : '') + d0.getDate();
    var yyyy = d0.getFullYear();
    var d1 = mm+'/'+dd+'/'+yyyy;
    return inpDate == d1;
}

function validDateMMDDYY(inpDate) {
    var d0 = new Date(inpDate);
    var mm = (d0.getMonth() < 9 ? '0' : '') + (d0.getMonth()+1);
    var dd = (d0.getDate() < 10 ? '0' : '') + d0.getDate();
    var yy = (d0.getYear() < 10 ? '0' : '') + d0.getYear();
    var d1 = mm+'/'+dd+'/'+yy;
    return inpDate == d1;
}
    