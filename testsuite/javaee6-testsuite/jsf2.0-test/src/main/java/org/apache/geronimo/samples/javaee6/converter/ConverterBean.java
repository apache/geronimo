/**
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.samples.javaee6.converter;

import java.util.ArrayList;
import java.util.List;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.event.ActionEvent;

@ManagedBean(name = "ConverterBean")
@RequestScoped
public class ConverterBean {

    private String inf = "Hide the rate table";
    private String msg = "Hello@ConverterBean";
    private String value = "1";
    private List<ConvertedValue> convertedList;
    private List<Currency> currencyList;
    private Boolean render = false;

    public String getMsg() {
        return msg;
    }

    public Boolean getRender() {
        return render;
    }

    @SuppressWarnings( { "UnusedDeclaration" })
    public void toggle(ActionEvent ae) {
        render = !render;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public List getConvertedList() {
        return this.convertedList;
    }

    private void setConvertedList() {
        this.convertedList = new ArrayList();
        try {

            for (Currency currency : currencyList) {
                ConvertedValue cv = new ConvertedValue();
                cv.setName("<" + currency.getName() + ">");
                cv.setActualVaule(Double.parseDouble(value) / currency.getRate());
                this.convertedList.add(cv);
            }
        } catch (Exception e) {
        }
    }

    public List getCurrencyList() {
        return currencyList;
    }

    public void setCurrencyList(List currencyList) {
        this.currencyList = currencyList;
        setConvertedList();
    }

    /** Creates a new instance of ConverterBean */
    public ConverterBean() {
        this.currencyList = new ArrayList();
        Currency c = new Currency("USD", 6.8269);
        currencyList.add(c);
        c = new Currency("HKD", 0.87887);
        currencyList.add(c);
        c = new Currency("JPY", 0.00754);
        currencyList.add(c);
        c = new Currency("EUR", 9.6734);
        currencyList.add(c);
        c = new Currency("GBP", 11.1009);
        currencyList.add(c);
        setConvertedList();
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
        setConvertedList();
    }
}
