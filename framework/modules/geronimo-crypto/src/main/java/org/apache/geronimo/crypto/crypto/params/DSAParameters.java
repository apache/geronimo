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

package org.apache.geronimo.crypto.crypto.params;

import java.math.BigInteger;

import org.apache.geronimo.crypto.crypto.CipherParameters;

public class DSAParameters
    implements CipherParameters
{
    private BigInteger              g;
    private BigInteger              q;
    private BigInteger              p;
    private DSAValidationParameters validation;

    public DSAParameters(
        BigInteger  p,
        BigInteger  q,
        BigInteger  g)
    {
        this.g = g;
        this.p = p;
        this.q = q;
    }

    public DSAParameters(
        BigInteger              p,
        BigInteger              q,
        BigInteger              g,
        DSAValidationParameters params)
    {
        this.g = g;
        this.p = p;
        this.q = q;
        this.validation = params;
    }

    public BigInteger getP()
    {
        return p;
    }

    public BigInteger getQ()
    {
        return q;
    }

    public BigInteger getG()
    {
        return g;
    }

    public DSAValidationParameters getValidationParameters()
    {
        return validation;
    }

    public boolean equals(
        Object  obj)
    {
        if (!(obj instanceof DSAParameters))
        {
            return false;
        }

        DSAParameters    pm = (DSAParameters)obj;

        return (pm.getP().equals(p) && pm.getQ().equals(q) && pm.getG().equals(g));
    }
}
