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

public class DSAValidationParameters
{
    private byte[]  seed;
    private int     counter;

    public DSAValidationParameters(
        byte[]  seed,
        int     counter)
    {
        this.seed = seed;
        this.counter = counter;
    }

    public int getCounter()
    {
        return counter;
    }

    public byte[] getSeed()
    {
        return seed;
    }

    public boolean equals(
        Object o)
    {
        if (o == null || !(o instanceof DSAValidationParameters))
        {
            return false;
        }

        DSAValidationParameters  other = (DSAValidationParameters)o;

        if (other.counter != this.counter)
        {
            return false;
        }

        if (other.seed.length != this.seed.length)
        {
            return false;
        }

        for (int i = 0; i != other.seed.length; i++)
        {
            if (other.seed[i] != this.seed[i])
            {
                return false;
            }
        }

        return true;
    }
}
