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
package javax.xml.rpc.encoding;

import javax.xml.namespace.QName;

/**
 * Constants representing XML Types.
 *
 * @version 1.0
 */
public class XMLType {
    // fixme: Thsi is a constants class - should be final and/or have a private
    // constructor
    public XMLType() {}

    /** XSD type for string.           */
    public static final QName XSD_STRING =
        new QName("http://www.w3.org/2001/XMLSchema", "string");

    /** XSD type for float.           */
    public static final QName XSD_FLOAT =
        new QName("http://www.w3.org/2001/XMLSchema", "float");

    /** XSD type for boolean.           */
    public static final QName XSD_BOOLEAN =
        new QName("http://www.w3.org/2001/XMLSchema", "boolean");

    /** XSD type for double.           */
    public static final QName XSD_DOUBLE =
        new QName("http://www.w3.org/2001/XMLSchema", "double");

    /** XSD type for integer.           */
    public static final QName XSD_INTEGER =
        new QName("http://www.w3.org/2001/XMLSchema", "integer");

    /** XSD type for int.           */
    public static final QName XSD_INT =
        new QName("http://www.w3.org/2001/XMLSchema", "int");

    /** XSD type for long.           */
    public static final QName XSD_LONG =
        new QName("http://www.w3.org/2001/XMLSchema", "long");

    /** XSD type for short.           */
    public static final QName XSD_SHORT =
        new QName("http://www.w3.org/2001/XMLSchema", "short");

    /** XSD type for decimal.           */
    public static final QName XSD_DECIMAL =
        new QName("http://www.w3.org/2001/XMLSchema", "decimal");

    /** XSD type for base64Binary.           */
    public static final QName XSD_BASE64 =
        new QName("http://www.w3.org/2001/XMLSchema", "base64Binary");

    /** XSD type for hexBinary.           */
    public static final QName XSD_HEXBINARY =
        new QName("http://www.w3.org/2001/XMLSchema", "hexBinary");

    /** XSD type for byte.           */
    public static final QName XSD_BYTE =
        new QName("http://www.w3.org/2001/XMLSchema", "byte");

    /** XSD type for dateTime.           */
    public static final QName XSD_DATETIME =
        new QName("http://www.w3.org/2001/XMLSchema", "dateTime");

    /** XSD type for QName.           */
    public static final QName XSD_QNAME =
        new QName("http://www.w3.org/2001/XMLSchema", "QName");

    /** SOAP type for string.           */
    public static final QName SOAP_STRING =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "string");

    /** SOAP type for boolean.           */
    public static final QName SOAP_BOOLEAN =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "boolean");

    /** SOAP type for double.           */
    public static final QName SOAP_DOUBLE =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "double");

    /** SOAP type for base64.           */
    public static final QName SOAP_BASE64 =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "base64");

    /** SOAP type for float.           */
    public static final QName SOAP_FLOAT =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "float");

    /** SOAP type for int.           */
    public static final QName SOAP_INT =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "int");

    /** SOAP type for long.           */
    public static final QName SOAP_LONG =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "long");

    /** SOAP type for short.           */
    public static final QName SOAP_SHORT =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "short");

    /** SOAP type for byte.           */
    public static final QName SOAP_BYTE =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "byte");

    /** SOAP type for Array.           */
    public static final QName SOAP_ARRAY =
        new QName("http://schemas.xmlsoap.org/soap/encoding/", "Array");
}

