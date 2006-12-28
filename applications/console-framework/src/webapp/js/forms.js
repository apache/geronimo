function textElementsNotEmpty(formName, elementNameArray){
    var obj;
    for(i in elementNameArray){
        var elem = elementNameArray[i];
        obj = eval("document." + formName + "." + elem); 
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
    var obj = eval("document." + formName + "." + elementName); 
    if(isIntegral(obj.value)) return true;
    else{
        alert(elementName + " must be an integer.");
        obj.focus();
        return false;
    }
}

function isIntegral(value){
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

function validDateMMDDYYYY(inpDate) {
    var d0 = new Date(inpDate);
    var mm = (d0.getMonth() < 9 ? '0' : '') + (d0.getMonth()+1);
    var dd = (d0.getDate() < 10 ? '0' : '') + d0.getDate();
    var yyyy = d0.getFullYear();
    var d1 = mm+'/'+dd+'/'+yyyy;
    return inpDate == d1;
}
