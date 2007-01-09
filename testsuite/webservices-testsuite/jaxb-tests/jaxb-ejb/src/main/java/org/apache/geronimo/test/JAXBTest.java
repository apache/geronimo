/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.geronimo.test;

import java.io.StringWriter;
import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.geronimo.test.generated.Account;

/**
 * Performs basic tests using the JAXB 2.0 API. 
 */
public class JAXBTest {

    private static final String EXPECTED_OUTPUT = 
        "<FirstName>foo</FirstName><LastName>bar</LastName>";

    private static final String INPUT =
        "<foo:Account xmlns:foo=\"http://geronimo.apache.org\">\r\n" +
        "<FirstName>first</FirstName>\r\n" +
        "<LastName>last</LastName>\r\n" +
        "</foo:Account>\r\n";
    
    JAXBContext jc = null;

    public JAXBTest() throws Exception {
        this.jc = 
            JAXBContext.newInstance("org.apache.geronimo.test.generated");
    }

    public String getImplementation() {
        return this.jc.getClass().getName();
    }

    public void testMarshall() throws Exception {
        
        Account bean = new Account();
        bean.setFirstName("foo");
        bean.setLastName("bar");

        Marshaller m = this.jc.createMarshaller();
        StringWriter writer = new StringWriter();
        m.marshal(bean, writer);
        writer.flush();
        
        String xml = writer.toString();
        System.out.println(xml);

        if (xml.indexOf(EXPECTED_OUTPUT) == -1) {
            throw new Exception("Unexpected xml generated");
        }
    }

    public void testUnmarshall() throws Exception {
        
        Unmarshaller m = this.jc.createUnmarshaller();
        Account bean = (Account)m.unmarshal(new StringReader(INPUT));
        
        if (!(bean.getFirstName().equals("first") &&
              bean.getLastName().equals("last"))) {
            throw new Exception("Unexpected data");
        }
    }


}
