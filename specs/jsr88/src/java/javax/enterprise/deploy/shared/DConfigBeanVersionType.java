/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * This source code implements specifications defined by the Java
 * Community Process. In order to remain compliant with the specification
 * DO NOT add / change / or delete method signatures!
 *
 * ====================================================================
 */
package javax.enterprise.deploy.shared;

/**
 *
 * @version $Revision: 1.2 $ $Date: 2003/08/16 01:32:54 $
 */
public class DConfigBeanVersionType {
    private static final int MAX_VALUE = 2;

    public static final DConfigBeanVersionType V1_3 = new DConfigBeanVersionType(0);
    public static final DConfigBeanVersionType V1_3_1 = new DConfigBeanVersionType(1);
    // Found during unittesting against Sun RI. Not in API doc, but expected.
    public static final DConfigBeanVersionType V1_4 = new DConfigBeanVersionType(2);

    private static final DConfigBeanVersionType[] enumValueTable = {
        V1_3,
        V1_3_1,
        V1_4,
    };

    private static final String[] stringTable = {
        "V1_3",
        "V1_3_1",
        "V1_4",
    };

    private int value;

    protected DConfigBeanVersionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    protected String[] getStringTable() {
        return stringTable;
    }

    protected DConfigBeanVersionType[] getEnumValueTable() {
        return enumValueTable;
    }

    public static DConfigBeanVersionType getDConfigBeanVersionType(int value) {
        return enumValueTable[value];
    }

    public String toString() {
        return (value >= getOffset() && value <= getOffset() + MAX_VALUE) ? getStringTable()[value] : String.valueOf(value);
    }

    protected int getOffset() {
        return 0;
    }
}