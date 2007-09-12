/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

def expected1 = new File(basedir, "src/test/resources/META-INF/geronimo-plugin.xml").text
def found1 = new File(basedir, "target/resources/META-INF/geronimo-plugin.xml").text

assert expected1 == found1

def expected2 = new File(basedir, "src/test/resources/META-INF/plan.xml").text
def found2 = new File(basedir, "target/resources/META-INF/plan.xml").text

assert expected2 == found2