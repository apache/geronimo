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

package javax.activation;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import junit.framework.TestCase;


/**
 *
 * @version $Rev$ $Date$
 */
public class MimeTypeTest extends TestCase {
    private MimeType mimeType;

	public MimeTypeTest(String name) {
		super(name);
	}

	public void setUp() throws Exception {
		super.setUp();
        mimeType = new MimeType();
	}

	public void testDefaultConstructor() throws MimeTypeParseException {
        assertEquals("application/*", mimeType.getBaseType());
		assertEquals("application", mimeType.getPrimaryType());
        // not sure as RFC2045 does not allow "*" but this is what the RI does
		assertEquals("*", mimeType.getSubType());

		assertTrue(mimeType.match(new MimeType()));
		assertTrue(mimeType.match(new MimeType("application/*")));

        assertNull(mimeType.getParameter("foo"));
        assertEquals(0, mimeType.getParameters().size());
        assertTrue(mimeType.getParameters().isEmpty());
	}

	public void testMimeTypeConstructor() throws MimeTypeParseException {
		mimeType = new MimeType("text/plain");
        assertEquals("text/plain", mimeType.getBaseType());
        assertEquals("text", mimeType.getPrimaryType());
        assertEquals("plain", mimeType.getSubType());
        assertEquals("text/plain", mimeType.toString());
	}

    public void testTypeConstructor() throws MimeTypeParseException {
        mimeType = new MimeType("text", "plain");
        assertEquals("text/plain", mimeType.getBaseType());
        assertEquals("text", mimeType.getPrimaryType());
        assertEquals("plain", mimeType.getSubType());
        assertEquals("text/plain", mimeType.toString());
    }

    public void testConstructorWithParams() throws MimeTypeParseException {
        mimeType = new MimeType("text/plain; charset=\"iso-8859-1\"");
        assertEquals("text/plain", mimeType.getBaseType());
        assertEquals("text", mimeType.getPrimaryType());
        assertEquals("plain", mimeType.getSubType());
        MimeTypeParameterList params = mimeType.getParameters();
        assertEquals(1, params.size());
        assertEquals("iso-8859-1", params.get("charset"));
        assertEquals("text/plain; charset=iso-8859-1", mimeType.toString());
    }

    public void testConstructorWithQuotableParams() throws MimeTypeParseException {
        mimeType = new MimeType("text/plain; charset=\"iso(8859)\"");
        assertEquals("text/plain", mimeType.getBaseType());
        assertEquals("text", mimeType.getPrimaryType());
        assertEquals("plain", mimeType.getSubType());
        MimeTypeParameterList params = mimeType.getParameters();
        assertEquals(1, params.size());
        assertEquals("iso(8859)", params.get("charset"));
        assertEquals("text/plain; charset=\"iso(8859)\"", mimeType.toString());
    }

    public void testWriteExternal() throws MimeTypeParseException, IOException {
        mimeType = new MimeType("text/plain; charset=iso8859-1");
        mimeType.writeExternal(new ObjectOutput() {
            public void writeUTF(String str) {
                assertEquals("text/plain; charset=iso8859-1", str);
            }

            public void close() {
                fail();
            }

            public void flush() {
            }

            public void write(int b) {
                fail();
            }

            public void write(byte b[]) {
                fail();
            }

            public void write(byte b[], int off, int len) {
                fail();
            }

            public void writeObject(Object obj) {
                fail();
            }

            public void writeDouble(double v) {
                fail();
            }

            public void writeFloat(float v) {
                fail();
            }

            public void writeByte(int v) {
                fail();
            }

            public void writeChar(int v) {
                fail();
            }

            public void writeInt(int v) {
                fail();
            }

            public void writeShort(int v) {
                fail();
            }

            public void writeLong(long v) {
                fail();
            }

            public void writeBoolean(boolean v) {
                fail();
            }

            public void writeBytes(String s) {
                fail();
            }

            public void writeChars(String s){
                fail();
            }
        });
    }

    public void testReadExternal() throws IOException, ClassNotFoundException {
        mimeType.readExternal(new ObjectInput() {
            public String readUTF() {
                return "text/plain; charset=iso-8859-1";
            }

            public int available() {
                fail();
                throw new AssertionError();
            }

            public int read() {
                fail();
                throw new AssertionError();
            }

            public void close() {
                fail();
                throw new AssertionError();
            }

            public long skip(long n) {
                fail();
                throw new AssertionError();
            }

            public int read(byte b[]) {
                fail();
                throw new AssertionError();
            }

            public int read(byte b[], int off, int len) {
                fail();
                throw new AssertionError();
            }

            public Object readObject() {
                fail();
                throw new AssertionError();
            }

            public byte readByte() {
                fail();
                throw new AssertionError();
            }

            public char readChar() {
                fail();
                throw new AssertionError();
            }

            public double readDouble() {
                fail();
                throw new AssertionError();
            }

            public float readFloat() {
                fail();
                throw new AssertionError();
            }

            public int readInt() {
                fail();
                throw new AssertionError();
            }

            public int readUnsignedByte() {
                fail();
                throw new AssertionError();
            }

            public int readUnsignedShort() {
                fail();
                throw new AssertionError();
            }

            public long readLong() {
                fail();
                throw new AssertionError();
            }

            public short readShort() {
                fail();
                throw new AssertionError();
            }

            public boolean readBoolean() {
                fail();
                throw new AssertionError();
            }

            public int skipBytes(int n) {
                fail();
                throw new AssertionError();
            }

            public void readFully(byte b[]) {
                fail();
            }

            public void readFully(byte b[], int off, int len) {
                fail();
            }

            public String readLine() {
                fail();
                throw new AssertionError();
            }
        });
        assertEquals("text/plain", mimeType.getBaseType());
        assertEquals("text", mimeType.getPrimaryType());
        assertEquals("plain", mimeType.getSubType());
        MimeTypeParameterList params = mimeType.getParameters();
        assertEquals(1, params.size());
        assertEquals("iso-8859-1", params.get("charset"));
        assertEquals("text/plain; charset=iso-8859-1", mimeType.toString());
    }
}
