/*
 * Copyright 2001-2004 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package javax.xml.rpc.soap;

import javax.xml.namespace.QName;
import javax.xml.soap.Detail;

/**
 * The <code>SOAPFaultException</code> exception represents a
 * SOAP fault.
 * <p>
 * The message part in the SOAP fault maps to the contents of
 * <code>faultdetail</code> element accessible through the
 * <code>getDetail</code> method on the <code>SOAPFaultException</code>.
 * The method <code>createDetail</code> on the
 * <code>javax.xml.soap.SOAPFactory</code> creates an instance
 * of the <code>javax.xml.soap.Detail</code>.
 * <p>
 * The <code>faultstring</code> provides a human-readable
 * description of the SOAP fault. The <code>faultcode</code>
 * element provides an algorithmic mapping of the SOAP fault.
 * <p>
 * Refer to SOAP 1.1 and WSDL 1.1 specifications for more
 * details of the SOAP faults.
 *
 * @version 1.0
 */
public class SOAPFaultException extends RuntimeException {

    /**
     *  Constructor for SOAPFaultException.
     *
     *  @param  faultcode    <code>QName</code> for the SOAP faultcode
     *  @param  faultstring  <code>faultstring</code> element of SOAP fault
     *  @param  faultactor   <code>faultactor</code> element of SOAP fault
     *  @param  detail       <code>faultdetail</code> element of SOAP fault
     */
    public SOAPFaultException(QName faultcode, String faultstring,
                              String faultactor, Detail detail) {

        super(faultstring);

        this.faultcode   = faultcode;
        this.faultstring = faultstring;
        this.faultactor  = faultactor;
        this.detail      = detail;
    }

    /**
     * Gets the <code>faultcode</code> element. The <code>faultcode</code> element provides an algorithmic
     * mechanism for identifying the fault. SOAP defines a small set of SOAP fault codes covering
     * basic SOAP faults.
     * @return  QName of the faultcode element
     */
    public QName getFaultCode() {
        return faultcode;
    }

    /**
     * Gets the <code>faultstring</code> element. The faultstring  provides a human-readable description of
     * the SOAP fault and is not intended for algorithmic processing.
     * @return <code>faultstring</code> element of the SOAP fault
     */
    public String getFaultString() {
        return faultstring;
    }

    /**
     * Gets the <code>faultactor</code> element. The <code>faultactor</code>
     * element provides information about which SOAP node on the SOAP message
     * path caused the fault to happen. It indicates the source of the fault.
     *
     * @return <code>faultactor</code> element of the SOAP fault
     */
    public String getFaultActor() {
        return faultactor;
    }

    /**
     * Gets the detail element. The detail element is intended for carrying
     * application specific error information related to the SOAP Body.
     *
     * @return <code>detail</code> element of the SOAP fault
     */
    public Detail getDetail() {
        return detail;
    }

    /** Qualified name of the faultcode. */
    private QName faultcode;

    /** The faultstring element of the SOAP fault. */
    private String faultstring;

    /** Faultactor element of the SOAP fault. */
    private String faultactor;

    /** Detail element of the SOAP fault. */
    private Detail detail;
}
