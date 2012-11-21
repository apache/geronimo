/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.geronimo.testsuite.restful;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.geronimo.testsupport.TestSupport;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestRestful extends TestSupport {
    private static String root;
    
	@Test
	public void testPost() throws IOException {
	    String contextroot = System.getProperty("appContext");
	    root = "http://localhost:8080/"+contextroot;
	    
		String order = "<order>"
			+ "<customer>wgz</customer>"
			+ "<productname>water</productname>"
			+ "<price>666</price>"
			+ "<quantity>1</quantity>"
			+ "<seller>shop</seller>"
		    + "</order>";
		URL url = new URL(root + "/resources/orders");
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setDoOutput(true);
		connection.setInstanceFollowRedirects(false);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/xml");
		OutputStream os = connection.getOutputStream();
		os.write(order.getBytes());
		os.flush();
		Assert.assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_CREATED);
		connection.disconnect();
	}
	
	@Test(dependsOnMethods={"testPost"})
	public void testGet() throws IOException {
	    String contextroot = System.getProperty("appContext");
		URL url = new URL(root + "/resources/orders/1");
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/xml");
		Assert.assertEquals(connection.getContentType(), "application/xml");
		Assert.assertEquals(connection.getResponseCode(), 200);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		String line = reader.readLine();
		StringBuilder response = new StringBuilder();
		while(line != null) {
			System.out.println(line);
			response.append(line);
			line = reader.readLine();
		}
		String responseText = response.toString();
		Assert.assertEquals(responseText, 
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><order id=\"1\"><customer>wgz</customer><price>666.0</price><productname>water</productname><quantity>1</quantity><seller>shop</seller></order>");
		connection.disconnect();
		
	}
	
	@Test(dependsOnMethods={"testGet"})
	public void testPut() throws IOException {
		String order = "<order>"
			+ "<customer>rafa</customer>"
			+ "<productname>water</productname>"
			+ "<price>666</price>"
			+ "<quantity>2</quantity>"
			+ "<seller>shop</seller>"
		    + "</order>";
		URL url = new URL(root + "/resources/orders/1");
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("PUT");
		connection.setRequestProperty("Content-Type", "application/xml");
		OutputStream os = connection.getOutputStream();
		os.write(order.getBytes());
		os.flush();
		Assert.assertEquals(connection.getResponseCode(), HttpURLConnection.HTTP_NO_CONTENT);
		connection.disconnect();
		connection = (HttpURLConnection)url.openConnection();
		
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/xml");
		Assert.assertEquals(connection.getContentType(), "application/xml");
		Assert.assertEquals(connection.getResponseCode(), 200);
		
		BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		
		String line = reader.readLine();
		StringBuilder response = new StringBuilder();
		while(line != null) {
			System.out.println(line);
			response.append(line);
			line = reader.readLine();
		}
		String responseText = response.toString();
		Assert.assertEquals(responseText, 
				"<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><order id=\"1\"><customer>rafa</customer><price>666.0</price><productname>water</productname><quantity>2</quantity><seller>shop</seller></order>");
		connection.disconnect();
	}
	
	@Test(dependsOnMethods={"testPut"})
	public void testDelete() throws IOException {
		URL url = new URL(root + "/resources/orders/1");
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("DELETE");
		Assert.assertEquals(connection.getResponseCode(), 204);
		connection.disconnect();
		
		connection = (HttpURLConnection)url.openConnection();
		connection.setRequestMethod("GET");
		connection.setRequestProperty("Accept", "application/xml");
		
		Assert.assertNotSame(connection.getContentType(), "application/xml");	
	}
}
