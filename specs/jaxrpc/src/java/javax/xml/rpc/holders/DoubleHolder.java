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
package javax.xml.rpc.holders;

/**
 * Holder for <code>double</code>s.
 *
 * @version 1.0
 */
public final class DoubleHolder implements Holder {

    /** The <code>double</code> contained by this holder. */
    public double value;

    /**
     * Make a new <code>DoubleHolder</code> with a <code>null</code> value.
     */
    public DoubleHolder() {}

    /**
     * Make a new <code>DoubleHolder</code> with <code>value</code> as
     * the value.
     *
     * @param value  the <code>double</code> to hold
     */
    public DoubleHolder(double value) {
        this.value = value;
    }
}

