/**
 *
 * Copyright 2004 The Apache Software Foundation
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

package javax.mail.search;
/**
 * @version $Revision: 1.2 $ $Date: 2004/02/25 09:59:09 $
 */
public abstract class IntegerComparisonTerm extends ComparisonTerm {
    protected int number;
    protected IntegerComparisonTerm(int comparison, int number) {
        super(comparison);
        this.number = number;
    }
    public boolean equals(Object other) {
        return super.equals(other)
            && ((IntegerComparisonTerm) other).number == number;
    }
    public int getComparison() {
        return super.getComparison();
    }
    public int getNumber() {
        return number;
    }
    public int hashCode() {
        return super.hashCode() + number * 47;
    }
    protected boolean match(int match) {
        return compare(number - match);
    }
}
