/**
 *
 * Copyright 2003-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.webservices;

import java.util.ArrayList;

public class Handler {
    private String handlerName;
    private String handlerClass;
    private ArrayList soapHeaderList = new ArrayList();
    private ArrayList soapRoleList = new ArrayList();

    public String getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(String handlerName) {
        this.handlerName = handlerName;
    }

    public String getHandlerClass() {
        return handlerClass;
    }

    public void setHandlerClass(String handlerClass) {
        this.handlerClass = handlerClass;
    }


    public void addSoapHeader(String soapHeader) throws IndexOutOfBoundsException {
        soapHeaderList.add(soapHeader);
    }

    public void addSoapHeader(int index, String soapHeader) throws IndexOutOfBoundsException {
        soapHeaderList.add(index, soapHeader);
    }

    public boolean removeSoapHeader(String soapHeader) {
        return soapHeaderList.remove(soapHeader);
    }

    public String getSoapHeader(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > soapHeaderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (String) soapHeaderList.get(index);
    }

    public String[] getSoapHeader() {
        int size = soapHeaderList.size();
        String[] mArray = new String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String) soapHeaderList.get(index);
        }
        return mArray;
    }

    public void setSoapHeader(int index, String soapHeader) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > soapHeaderList.size())) {
            throw new IndexOutOfBoundsException();
        }
        soapHeaderList.set(index, soapHeader);
    }

    public void setSoapHeader(String[] soapHeaderArray) {
        soapHeaderList.clear();
        for (int i = 0; i < soapHeaderArray.length; i++) {
            String soapHeader = soapHeaderArray[i];
            soapHeaderList.add(soapHeader);
        }
    }

    public void clearSoapHeader() {
        soapHeaderList.clear();
    }


    public void addSoapRole(String soapRole) throws IndexOutOfBoundsException {
        soapRoleList.add(soapRole);
    }

    public void addSoapRole(int index, String soapRole) throws IndexOutOfBoundsException {
        soapRoleList.add(index, soapRole);
    }

    public boolean removeSoapRole(String soapRole) {
        return soapRoleList.remove(soapRole);
    }

    public String getSoapRole(int index) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > soapRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        return (String) soapRoleList.get(index);
    }

    public String[] getSoapRole() {
        int size = soapRoleList.size();
        String[] mArray = new String[size];
        for (int index = 0; index < size; index++) {
            mArray[index] = (String) soapRoleList.get(index);
        }
        return mArray;
    }

    public void setSoapRole(int index, String soapRole) throws IndexOutOfBoundsException {
        if ((index < 0) || (index > soapRoleList.size())) {
            throw new IndexOutOfBoundsException();
        }
        soapRoleList.set(index, soapRole);
    }

    public void setSoapRole(String[] soapRoleArray) {
        soapRoleList.clear();
        for (int i = 0; i < soapRoleArray.length; i++) {
            String soapRole = soapRoleArray[i];
            soapRoleList.add(soapRole);
        }
    }

    public void clearSoapRole() {
        soapRoleList.clear();
    }


}
