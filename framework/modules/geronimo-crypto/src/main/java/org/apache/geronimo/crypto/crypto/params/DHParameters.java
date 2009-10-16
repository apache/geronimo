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

public class DHParameters
    implements CipherParameters
{
    private BigInteger              g;
    private BigInteger              p;
    private BigInteger              q;
    private int                     j;
    private DHValidationParameters  validation;

    public DHParameters(
        BigInteger  p,
        BigInteger  g)
    {
        this.g = g;
        this.p = p;
    }

    public DHParameters(
        BigInteger  p,
        BigInteger  g,
        BigInteger  q,
        int         j)
    {
        this.g = g;
        this.p = p;
        this.q = q;
        this.j = j;
    }

    public DHParameters(
        BigInteger              p,
        BigInteger              g,
        BigInteger              q,
        int                     j,
        DHValidationParameters  validation)
    {
        this.g = g;
        this.p = p;
        this.q = q;
        this.j = j;
    }

    public BigInteger getP()
    {
        return p;
    }

    public BigInteger getG()
    {
        return g;
    }

    public BigInteger getQ()
    {
        return q;
    }

    /**
     * Return the private value length in bits - if set, zero otherwise (use bitLength(P) - 1).
     *
     * @return the private value length in bits, zero otherwise.
     */
    public int getJ()
    {
        return j;
    }

    public DHValidationParameters getValidationParameters()
    {
        return validation;
    }

    public boolean equals(
        Object  obj)
    {
        if (!(obj instanceof DHParameters))
        {
            return false;
        }

        DHParameters    pm = (DHParameters)obj;

        if (this.getValidationParameters() != null)
        {
            if (!this.getValidationParameters().equals(pm.getValidationParameters()))
            {
                return false;
            }
        }
        else
        {
            if (pm.getValidationParameters() != null)
            {
                return false;
            }
        }

        if (this.getQ() != null)
        {
            if (!this.getQ().equals(pm.getQ()))
            {
                return false;
            }
        }
        else
        {
            if (pm.getQ() != null)
            {
                return false;
            }
        }

        return (j == pm.getJ()) && pm.getP().equals(p) && pm.getG().equals(g);
    }
}
