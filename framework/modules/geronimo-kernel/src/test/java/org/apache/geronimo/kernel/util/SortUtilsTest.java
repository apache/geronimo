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

package org.apache.geronimo.kernel.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.apache.geronimo.kernel.util.CircularReferencesException;
import org.apache.geronimo.kernel.util.IllegalNodeConfigException;
import org.apache.geronimo.kernel.util.SortUtils;
import org.apache.geronimo.kernel.util.SortUtils.Visitor;
import org.junit.Test;

/**
 * @version $Rev$ $Date$
 */
public class SortUtilsTest {

    @Test
    public void testFragmentSortA() throws Exception {
        List<String> fragmentList = new ArrayList<String>();
        for (int i = 0; i < 6; i++) {
            fragmentList.add(String.valueOf((char) ('A' + i)));
        }
        List<String> sortedList = SortUtils.sort(fragmentList, new Visitor<String>() {

            @Override
            public boolean afterOthers(String t) {
                if (t.equals("A") || t.equals("C")) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean beforeOthers(String t) {
                if (t.equals("B") || t.equals("F")) {
                    return true;
                }
                return false;
            }

            @Override
            public List<String> getAfterNames(String t) {
                if (t.equals("A")) {
                    return Arrays.asList("C");
                }
                return Collections.emptyList();
            }

            @Override
            public List<String> getBeforeNames(String t) {
                if (t.equals("F")) {
                    return Arrays.asList("B");
                }
                return Collections.emptyList();
            }

            @Override
            public String getName(String t) {
                return t;
            }

        });
        Assert.assertEquals(Arrays.asList("F", "B", "D", "E", "C", "A"), sortedList);
    }

    @Test
    public void testFragmentSortB() throws Exception {
        List<String> fragmentList = new ArrayList<String>();
        for (int i = 0; i < 6; i++) {
            fragmentList.add(String.valueOf((char) ('A' + i)));
        }
        List<String> sortedList = SortUtils.sort(fragmentList, new Visitor<String>() {

            @Override
            public boolean afterOthers(String t) {
                if (t.equals("A") || t.equals("D")) {
                    return true;
                }
                return false;
            }

            @Override
            public boolean beforeOthers(String t) {
                if (t.equals("B") || t.equals("E")) {
                    return true;
                }
                return false;
            }

            @Override
            public List<String> getAfterNames(String t) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getBeforeNames(String t) {
                if (t.equals("A")) {
                    return Arrays.asList("C");
                }
                return Collections.emptyList();
            }

            @Override
            public String getName(String t) {
                return t;
            }

        });
        Assert.assertEquals(Arrays.asList("B", "E", "F", "A", "C", "D"), sortedList);
    }

    @Test
    public void testFragmentSortC() throws Exception {
        List<String> fragmentList = new ArrayList<String>();
        for (int i = 0; i < 4; i++) {
            fragmentList.add(String.valueOf((char) ('A' + i)));
        }
        List<String> sortedList = SortUtils.sort(fragmentList, new Visitor<String>() {

            @Override
            public boolean afterOthers(String t) {
                return t.equals("A");
            }

            @Override
            public boolean beforeOthers(String t) {
                return t.equals("C");
            }

            @Override
            public List<String> getAfterNames(String t) {
                return Collections.emptyList();
            }

            @Override
            public List<String> getBeforeNames(String t) {
                return Collections.emptyList();
            }

            @Override
            public String getName(String t) {
                return t;
            }

        });
        Assert.assertEquals(Arrays.asList("C", "B", "D", "A"), sortedList);
    }

    @Test
    public void testBothAfterAndBeforeOthers() {
        List<String> fragmentList = new ArrayList<String>();
        for (int i = 0; i < 2; i++) {
            fragmentList.add(String.valueOf((char) ('A' + i)));
        }
        Exception actualException = null;
        try {
            SortUtils.sort(fragmentList, new Visitor<String>() {

                @Override
                public boolean afterOthers(String t) {
                    return t.equals("A");
                }

                @Override
                public boolean beforeOthers(String t) {
                    return t.equals("A");
                }

                @Override
                public List<String> getAfterNames(String t) {
                    return Collections.emptyList();
                }

                @Override
                public List<String> getBeforeNames(String t) {
                    return Collections.emptyList();
                }

                @Override
                public String getName(String t) {
                    return t;
                }

            });
        } catch (IllegalNodeConfigException e) {
            actualException = e;
        } catch (CircularReferencesException e) {
            Assert.fail("Should not get CircularReference Exception here");
        }
        Assert.assertNotNull("Should get an IllegalNodeConfigException", actualException);
    }

    @Test
    public void testBeforeOrAfterItselfException() {
        List<String> fragmentList = new ArrayList<String>();
        for (int i = 0; i < 2; i++) {
            fragmentList.add(String.valueOf((char) ('A' + i)));
        }
        IllegalNodeConfigException actualException = null;
        try {
            SortUtils.sort(fragmentList, new Visitor<String>() {

                @Override
                public boolean afterOthers(String t) {
                    return false;
                }

                @Override
                public boolean beforeOthers(String t) {
                    return false;
                }

                @Override
                public List<String> getAfterNames(String t) {
                    if (t.equals("A")) {
                        return Arrays.asList("A");
                    } else {
                        return Collections.emptyList();
                    }
                }

                @Override
                public List<String> getBeforeNames(String t) {
                    return Collections.emptyList();
                }

                @Override
                public String getName(String t) {
                    return t;
                }

            });
        } catch (IllegalNodeConfigException e) {
            actualException = e;

        } catch (CircularReferencesException e) {
            Assert.fail("Should not get CircularReferencesException here");
        }
        Assert.assertNotNull("Should get an IllegalNodeConfigException", actualException);
    }

    @Test
    public void testCircularReferenceExceptionA() {
        List<String> fragmentList = new ArrayList<String>();
        for (int i = 0; i < 2; i++) {
            fragmentList.add(String.valueOf((char) ('A' + i)));
        }
        CircularReferencesException actualException = null;
        try {
            SortUtils.sort(fragmentList, new Visitor<String>() {

                @Override
                public boolean afterOthers(String t) {
                    return false;
                }

                @Override
                public boolean beforeOthers(String t) {
                    return false;
                }

                @Override
                public List<String> getAfterNames(String t) {
                    if (t.equals("A")) {
                        return Arrays.asList("B");
                    } else {
                        return Collections.emptyList();
                    }
                }

                @Override
                public List<String> getBeforeNames(String t) {
                    if (t.equals("A")) {
                        return Arrays.asList("B");
                    } else {
                        return Collections.emptyList();
                    }
                }

                @Override
                public String getName(String t) {
                    return t;
                }

            });
        } catch (IllegalNodeConfigException e) {
            Assert.fail("Should not get IllegalNodeConfigException here");
        } catch (CircularReferencesException e) {
            actualException = e;
        }
        Assert.assertNotNull("Should get an CircularReferencesException", actualException);
    }

    @Test
    public void testCircularReferenceExceptionB() {
        List<String> fragmentList = new ArrayList<String>();
        for (int i = 0; i < 3; i++) {
            fragmentList.add(String.valueOf((char) ('A' + i)));
        }
        Exception actualException = null;
        try {
            SortUtils.sort(fragmentList, new Visitor<String>() {

                @Override
                public boolean afterOthers(String t) {
                    return false;
                }

                @Override
                public boolean beforeOthers(String t) {
                    return false;
                }

                @Override
                public List<String> getAfterNames(String t) {
                    if (t.equals("A")) {
                        return Arrays.asList("B");
                    } else if (t.equals("B")) {
                        return Arrays.asList("C");
                    } else if (t.equals("C")) {
                        return Arrays.asList("A");
                    }
                    return Collections.emptyList();
                }

                @Override
                public List<String> getBeforeNames(String t) {
                    return Collections.emptyList();
                }

                @Override
                public String getName(String t) {
                    return t;
                }

            });
        } catch (IllegalNodeConfigException e) {
            Assert.fail("Should not get IllegalNodeConfigException here");
        } catch (CircularReferencesException e) {
            actualException = e;
        }
        Assert.assertNotNull("Should get an CircularReferencesException", actualException);
    }
}
