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

package org.apache.geronimo.crypto.asn1;

public interface DERTags
{
    public static final int BOOLEAN             = 0x01;
    public static final int INTEGER             = 0x02;
    public static final int BIT_STRING          = 0x03;
    public static final int OCTET_STRING        = 0x04;
    public static final int NULL                = 0x05;
    public static final int OBJECT_IDENTIFIER   = 0x06;
    public static final int EXTERNAL            = 0x08;
    public static final int ENUMERATED          = 0x0a;
    public static final int SEQUENCE            = 0x10;
    public static final int SEQUENCE_OF         = 0x10; // for completeness
    public static final int SET                 = 0x11;
    public static final int SET_OF              = 0x11; // for completeness


    public static final int NUMERIC_STRING      = 0x12;
    public static final int PRINTABLE_STRING    = 0x13;
    public static final int T61_STRING          = 0x14;
    public static final int VIDEOTEX_STRING     = 0x15;
    public static final int IA5_STRING          = 0x16;
    public static final int UTC_TIME            = 0x17;
    public static final int GENERALIZED_TIME    = 0x18;
    public static final int GRAPHIC_STRING      = 0x19;
    public static final int VISIBLE_STRING      = 0x1a;
    public static final int GENERAL_STRING      = 0x1b;
    public static final int UNIVERSAL_STRING    = 0x1c;
    public static final int BMP_STRING          = 0x1e;
    public static final int UTF8_STRING         = 0x0c;

    public static final int CONSTRUCTED         = 0x20;
    public static final int APPLICATION         = 0x40;
    public static final int TAGGED              = 0x80;
}
