/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.geronimo.jaxws.mtom;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.IIOImage;
import javax.imageio.ImageReader;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.soap.MTOMFeature;

import junit.framework.Assert;

import org.apache.geronimo.echo.Echo;

public abstract class TestServlet extends HttpServlet {

    protected String address;
    protected Service service;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String testName = request.getParameter("test");
        if (testName == null || !testName.startsWith("test")) {
            throw new ServletException("Invalid test name");
        }
        Method testMethod = null;
        try {
            testMethod = getClass().getMethod(testName, new Class[] {});
        } catch (Exception e1) {
            throw new ServletException("No such test: " + testName);
        }
        try {
            testMethod.invoke(this, (Object[]) null);
        } catch (IllegalArgumentException e) {
            throw new ServletException("Error invoking test: " + e.getMessage());
        } catch (IllegalAccessException e) {
            throw new ServletException("Error invoking test: " + e.getMessage());
        } catch (InvocationTargetException e) {
            Throwable root = e.getTargetException();
            ServletException ex = new ServletException("Test '" + testName + "' failed");
            ex.initCause(root);
            throw ex;
        }
        response.setContentType("text/plain");
        response.getWriter().println("Test '" + testName + "' passed");
    }

    public void testEchoBytesWithMTOMSupport() throws Exception {
        testEchoBytes(true);
    }

    public void testEchoBytesWithoutMTOMSupport() throws Exception {
        testEchoBytes(false);
    }

    private void testEchoBytes(boolean mtomSupport) throws Exception {
        Echo echo = null;
        if (mtomSupport)
            echo = service.getPort(Echo.class, new MTOMFeature());
        else
            echo = service.getPort(Echo.class);

        byte[] expectedBytes = loadImageAsBytes();
        byte[] echoBytes = echo.echoBytes(mtomSupport, expectedBytes);
        Assert.assertEquals(expectedBytes.length, echoBytes.length);
        for (int i=0;i<expectedBytes.length;i++) {
            Assert.assertEquals("" + i, expectedBytes[i], echoBytes[i]);
        }
    }

    public void testEchoImageWithMTOMSupport() throws Exception {
        testEchoImage(true);
    }

    public void testEchoImageWithoutMTOMSupport() throws Exception {
        testEchoImage(false);
    }

    private void testEchoImage(boolean mtomSupport) throws Exception {
        Echo echo = null;
        if (mtomSupport)
            echo = service.getPort(Echo.class, new MTOMFeature());
        else
            echo = service.getPort(Echo.class);

        BufferedImage expectedImage = loadImage();
        Image echoImage = echo.echoImage(mtomSupport, expectedImage);
        byte[] actualImageBytes = convertImagetoBytes(echoImage);
        byte[] expectedImageBytes = convertImagetoBytes(reserializeImage(expectedImage));
        Assert.assertEquals(expectedImageBytes.length, actualImageBytes.length);
        for (int i=0; i<expectedImageBytes.length; i++) {
            Assert.assertEquals("" + i, expectedImageBytes[i], actualImageBytes[i]);
        }
    }
    
    private BufferedImage loadImage() throws Exception {
        URL source = this.getClass().getResource("/image.jpg");
        return ImageIO.read(source);
    }
    
    private Image reserializeImage(BufferedImage image) throws Exception {
        byte [] beforeBytes = convertImagetoBytes(image);        
        return ImageIO.read(new ByteArrayInputStream(beforeBytes));
    }
    
    private byte[] loadImageAsBytes() throws Exception {
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        InputStream in = null;
        int iCurrentReadBytes = -1;
        byte[] bytesBuffer = new byte[512];
        try {
            in = getClass().getResourceAsStream("/image.jpg");
            while ((iCurrentReadBytes = in.read(bytesBuffer)) != -1)
                imageBytes.write(bytesBuffer, 0, iCurrentReadBytes);
            return imageBytes.toByteArray();
        } finally {
            if (in != null)
                try {
                    in.close();
                } catch (Exception e) {
                }
        }
    }
    
    private byte[] convertImagetoBytes(Image image) throws Exception {
        ByteArrayOutputStream imageBytes = new ByteArrayOutputStream();
        Iterator iterator = ImageIO.getImageWritersByMIMEType("image/jpeg");
        ImageWriter imageWriter = (ImageWriter) iterator.next();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageOutputStream ios = ImageIO.createImageOutputStream(baos);
        imageWriter.setOutput(ios);
        imageWriter.write(new IIOImage((BufferedImage)image, null, null));
        ios.flush();
        imageWriter.dispose();
        return baos.toByteArray();
    }

    protected void updateAddress() {
        Echo echo = service.getPort(Echo.class);
        BindingProvider binding = (BindingProvider) echo;
        this.address = (String) binding.getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
        System.out.println("Set address: " + this.address);
    }
}
