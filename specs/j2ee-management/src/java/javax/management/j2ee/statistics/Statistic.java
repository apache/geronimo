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

//
// This source code implements specifications defined by the Java
// Community Process. In order to remain compliant with the specification
// DO NOT add / change / or delete method signatures!
//

package javax.management.j2ee.statistics;

/**
 *
 *
 *
 * @version $Rev$
 */
public interface Statistic {
    // Defined in JSR77.6.4.1.2
    public final static String UNIT_TIME_HOUR = "HOUR";
    public final static String UNIT_TIME_MINUTE = "MINUTE";
    public final static String UNIT_TIME_SECOND = "SECOND";
    public final static String UNIT_TIME_MILLISECOND = "MILLISECOND";
    public final static String UNIT_TIME_MICROSECOND = "MICROSECOND";
    public final static String UNIT_TIME_NANOSECOND = "NANOSECOND";

    public String getName();

    public String getUnit();

    public String getDescription();

    public long getStartTime();

    public long getLastSampleTime();
}