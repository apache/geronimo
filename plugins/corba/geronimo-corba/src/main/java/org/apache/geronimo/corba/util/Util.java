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
package org.apache.geronimo.corba.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.io.ObjectOutputStream;
import java.rmi.Remote;
import java.rmi.UnexpectedException;
import java.rmi.RemoteException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import javax.ejb.spi.HandleDelegate;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.apache.geronimo.corba.ORBConfiguration;
import org.apache.geronimo.crypto.asn1.DERInputStream;
import org.apache.geronimo.crypto.asn1.DERObjectIdentifier;
import org.apache.geronimo.crypto.asn1.DEROutputStream;
import org.apache.geronimo.crypto.asn1.x509.GeneralName;
import org.apache.geronimo.crypto.asn1.x509.X509Name;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.CORBA.portable.UnknownException;
import org.omg.CORBA.portable.IDLEntity;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.GSSUP.InitialContextToken;
import org.omg.GSSUP.InitialContextTokenHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA_2_3.portable.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.geronimo.corba.CorbaApplicationServer;
import org.apache.openejb.ProxyInfo;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.core.ServerFederation;

/**
 * Various utility functions.
 * <p/>
 * Note: #getORB() and #getCodec() rely on UtilInitializer to initialze the ORB and codec.
 *
 * @version $Rev: 503493 $ $Date: 2007-02-04 13:47:55 -0800 (Sun, 04 Feb 2007) $
 * @see UtilInitializer
 */
public final class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);
    private static final byte ASN_TAG_NT_EXPORTED_NAME1 = 0x04;
    private static final byte ASN_TAG_NT_EXPORTED_NAME2 = 0x01;
    private static final byte ASN_TAG_OID = 0x06;
    private static final byte ASN_TAG_GSS = 0x60;
    private static ORB orb;
    private static Codec codec;
    private static HandleDelegate handleDelegate;
    private static CorbaApplicationServer corbaApplicationServer = new CorbaApplicationServer();
    private static HashMap<String,ORBConfiguration> configuredOrbs = new HashMap<String,ORBConfiguration>(); 
    
    public static ORB getORB() {
        assert orb != null;
        return orb;
    }
    
    public static void registerORB(String id, ORBConfiguration orb) {
        configuredOrbs.put(id, orb); 
    }
    
    public static ORBConfiguration getRegisteredORB(String id) {
        return configuredOrbs.get(id); 
    }
    
    public static void unregisterORB(String id) {
        configuredOrbs.remove(id); 
    }
    

    public static void setORB(ORB orb) throws UserException {
        if (Util.orb == null) {
            Util.orb = orb;
            CodecFactory factory = (CodecFactory) Util.orb.resolve_initial_references("CodecFactory");
            codec = factory.create_codec(new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2));
        }
    }

    public static Codec getCodec() {
        assert codec != null;
        return codec;
    }

    public static HandleDelegate getHandleDelegate() throws NamingException {
        if (handleDelegate == null) {
            InitialContext ic = new InitialContext();
            handleDelegate = (HandleDelegate) ic.lookup("java:comp/HandleDelegate");
        }
        return handleDelegate;
    }
    
    public static Object getEJBProxy(ProxyInfo info) {
        if (info.getInterfaceType().isHome()) {
            return corbaApplicationServer.getEJBHome(info); 
        }
        else {
            return corbaApplicationServer.getEJBObject(info); 
        }
    }

    public static byte[] encodeOID(String oid) throws IOException {
        oid = (oid.startsWith("oid:") ? oid.substring(4) : oid);
        return encodeOID(new DERObjectIdentifier(oid));
    }

    public static byte[] encodeOID(DERObjectIdentifier oid) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);

        dOut.writeObject(oid);

        return bOut.toByteArray();
    }

    public static String decodeOID(byte[] oid) throws IOException {
        return decodeOIDDERObjectIdentifier(oid).getId();
    }

    public static DERObjectIdentifier decodeOIDDERObjectIdentifier(byte[] oid) throws IOException {
        ByteArrayInputStream bIn = new ByteArrayInputStream(oid);
        DERInputStream dIn = new DERInputStream(bIn);

        return (DERObjectIdentifier) dIn.readObject();
    }

    public static byte[] encodeGeneralName(String name) throws IOException {
        return encodeGeneralName(new X509Name(name));
    }

    public static byte[] encodeGeneralName(X509Name x509Name) throws IOException {
        return encodeGeneralName(new GeneralName(x509Name));
    }

    public static byte[] encodeGeneralName(GeneralName generalName) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);

        dOut.writeObject(generalName);

        return bOut.toByteArray();
    }

    public static String decodeGeneralName(byte[] name) throws IOException {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * This method encodes a name as if it was encoded using the GSS-API
     * gss_export_name() function call (see RFC 2743, page 84).
     * The oid to indicate names of this format is:<br/>
     * {1(iso), 3(org), 6(dod), 1(internet), 5(security), 6(nametypes),
     * 4(gss-api-exported-name)}<br/>
     * The token has the following format:
     * <table>
     * <tr><td><b>Offset</b></td><td><b>Meaning</b></td><td><b>Value</b></td></tr>
     * <tr><td>0</td><td>token id</td><td>0x04</td></tr>
     * <tr><td>1</td><td>token id</td><td>0x01</td></tr>
     * <p/>
     * <tr><td>2</td><td>oid length</td><td>hi-byte (len/0xFF)</td></tr>
     * <tr><td>3</td><td>oid length</td><td>lo-byte (len%0xFF)</td></tr>
     * <p/>
     * <tr><td>4</td><td>oid</td><td>oid:1.3.6.1.5.6.4</td></tr>
     * <p/>
     * <tr><td>n+0</td><td>name length</td><td>len/0xFFFFFF</td></tr>
     * <tr><td>n+1</td><td>name length</td><td>(len%0xFFFFFF)/0xFFFF</td></tr>
     * <tr><td>n+2</td><td>name length</td><td>((len%0xFFFFFF)%0xFFFF)/0xFF</td></tr>
     * <tr><td>n+3</td><td>name length</td><td>((len%0xFFFFFF)%0xFFFF)%0xFF</td></tr>
     * <p/>
     * <tr><td>n+4</td><td>name</td><td>foo</td></tr>
     * </table>
     *
     * @param oid  The oid of the mechanism this name is exported from.
     * @param name The name to be exported.
     * @return The byte array representing the exported name object.
     */
    public static byte[] encodeGSSExportName(String oid, String name) {
        try {
            byte[] oid_arr = encodeOID(oid);
            int oid_len = oid_arr.length;
            byte[] name_arr = name.getBytes("UTF-8");
            int name_len = name_arr.length;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // token id at 0
            baos.write(ASN_TAG_NT_EXPORTED_NAME1);
            baos.write(ASN_TAG_NT_EXPORTED_NAME2);

            // write the two length bytes
            baos.write((byte) (oid_len & 0xFF00) >> 8);
            baos.write((byte) (oid_len & 0x00FF));

            // oid at 2
            baos.write(oid_arr);

            // name length at n
            baos.write((byte) (name_len & 0xFF000000) >> 24);
            baos.write((byte) (name_len & 0x00FF0000) >> 16);
            baos.write((byte) (name_len & 0x0000FF00) >> 8);
            baos.write((byte) (name_len & 0x000000FF));

            // name at n+4
            baos.write(name_arr);
            return baos.toByteArray();
        } catch (Exception ex) {
            // do nothing, return null
        }
        return null;
    }

    /**
     * This function reads a name from a byte array which was created
     * by the gssExportName() method.
     *
     * @param name_tok The GSS name token.
     * @return The name from the GSS name token.
     */
    public static String decodeGSSExportName(byte[] name_tok) {
        String result = null;
        if (name_tok != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(name_tok);
            try {
                // GSSToken tag 1 0x04
                int t1 = bais.read();
                if (t1 == ASN_TAG_NT_EXPORTED_NAME1) {
                    // GSSToken tag 2 0x01
                    int t2 = bais.read();
                    if (t2 == ASN_TAG_NT_EXPORTED_NAME2) {
                        // read the two length bytes
                        int l = bais.read() << 8;
                        l += bais.read();

                        // read the oid
                        byte[] oid_arr = new byte[l];
                        bais.read(oid_arr, 0, l);
                        String oid = decodeOID(oid_arr);

                        if (oid.equals(GSSUPMechOID.value.substring(4))) {
                            int l1 = bais.read();
                            int l2 = bais.read();
                            int l3 = bais.read();
                            int l4 = bais.read();

                            int name_len = (l1 << 24) + (l2 << 16) + (l3 << 8) + l4;
                            byte[] name_arr = new byte[name_len];
                            bais.read(name_arr, 0, name_len);
                            result = new String(name_arr);
                        } else {
                            System.err.print("ASN1Utils.gssImportName: Unknown OID: " + oid +
                                    " ('" + Integer.toHexString(oid_arr[0]) + "')");
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // do nothing, return null
            }
        }
        return result;
    }

    private static final Pattern SCOPED_NAME_EXTRACTION_PATTERN = Pattern.compile("(\\\\\\\\)|(\\\\@)|(@)|(\\z)");

    /**
     * See csiv2 spec 16.2.5 par. 63-64.  We extract the username if any and un-escape any
     * escaped \ and @ characters.
     * 
     * @param scopedNameBytes
     * @return
     * @throws UnsupportedEncodingException
     */
    public static String extractUserNameFromScopedName(byte[] scopedNameBytes) throws UnsupportedEncodingException {
        String scopedUserName = new String(scopedNameBytes, "UTF8");
        return extractUserNameFromScopedName(scopedUserName);
    }

    public static String extractUserNameFromScopedName(String scopedUserName) {
        Matcher m = SCOPED_NAME_EXTRACTION_PATTERN.matcher(scopedUserName);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(buf, "");
            if (m.group(1) != null) {
                buf.append('\\');
            } else if (m.group(2) != null) {
                buf.append("@");
            } else if (m.group(3) != null) {
                break;
            }
        }
        return buf.toString();
    }

    private static final Pattern SCOPED_NAME_ESCAPE_PATTERN = Pattern.compile("(\\\\)|(@)");

    public static String buildScopedUserName(String user, String domain) {
        StringBuffer buf = new StringBuffer();
        if (user != null) {
            escape(user, buf);
        }
        if (domain != null) {
            buf.append('@');
            escape(domain, buf);
        }
        return buf.toString();
    }

    private static void escape(String s, StringBuffer buf) {
        Matcher m = SCOPED_NAME_ESCAPE_PATTERN.matcher(s);
        while (m.find()) {
            m.appendReplacement(buf, "");
            if (m.group(1) != null) {
                buf.append("\\\\");
            } else if (m.group(2) != null) {
                buf.append("\\@");
            }
        }
        m.appendTail(buf);
    }


    /**
     * Encode a mechanism independent initial context token (GSSToken). Defined
     * in [IETF RFC 2743] Section 3.1, "Mechanism-Independent token Format" pp. 81-82.
     * <table>
     * <tr><td><b>Offset</b></td><td><b>Meaning</b></td></tr>
     * <tr><td>0</td><td>ASN1 tag</td></tr>
     * <tr><td>1</td><td>token length (&lt;128)</td></tr>
     * <tr><td>2</td><td>mechanism oid</td></tr>
     * <tr><td>n</td><td>mechanism specific token (e.g. GSSUP::InitialContextToken)</td></tr>
     * </table>
     * Currently only one mechanism specific token is supported: GSS username password
     * (GSSUP::InitialContextToken).
     *
     * @param orb    The orb to get an Any from.
     * @param codec  The codec to do the encoding of the Any.
     * @param user   The username.
     * @param pwd    The password of the user.
     * @param target The target name.
     * @return The byte array of the ASN1 encoded GSSToken.
     */
    public static byte[] encodeGSSUPToken(ORB orb, Codec codec, String user, String pwd, String target) {
        byte[] result = null;
        try {
            // write the GSS ASN tag
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(ASN_TAG_GSS);

            // create and encode a GSSUP initial context token
            InitialContextToken init_token = new InitialContextToken();
            init_token.username = user.getBytes("UTF-8");

            init_token.password = pwd.getBytes("UTF-8");

            init_token.target_name = encodeGSSExportName(GSSUPMechOID.value.substring(4), target);

            Any a = orb.create_any();
            InitialContextTokenHelper.insert(a, init_token);
            byte[] init_ctx_token = codec.encode_value(a);

            // encode the mechanism oid
            byte[] oid_arr = encodeOID(GSSUPMechOID.value.substring(4));

            // write the length
            baos.write((byte) (oid_arr.length + init_ctx_token.length + 2));

            // write the mechanism oid
            baos.write(oid_arr);

            // write the
            baos.write(init_ctx_token);

            // get the bytes
            result = baos.toByteArray();
        } catch (Exception ex) {
            // do nothing, return null
        }
        return result;
    }

    /**
     * Decode an GSSUP InitialContextToken from a GSSToken.
     *
     * @param codec     The codec to do the encoding of the Any.
     * @param gssup_tok The InitialContextToken struct to fill in the decoded values.
     * @return Return true when decoding was successful, false otherwise.
     */
    public static boolean decodeGSSUPToken(Codec codec, byte[] token_arr,
                                           InitialContextToken gssup_tok) {
        boolean result = false;
        if (gssup_tok != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(token_arr);
            try {
                // GSSToken tag
                int c = bais.read();
                if (c == ASN_TAG_GSS) {
                    // GSSToken length
                    int token_len = bais.read();
                    // OID tag
                    int oid_tag = bais.read();
                    if (oid_tag == ASN_TAG_OID) {
                        // OID length
                        int oid_len = bais.read();
                        byte[] oid_tmp_arr = new byte[oid_len];
                        bais.read(oid_tmp_arr, 0, oid_len);
                        byte[] oid_arr = new byte[oid_len + 2];
                        oid_arr[0] = (byte) oid_tag;
                        oid_arr[1] = (byte) oid_len;
                        System.arraycopy(oid_tmp_arr, 0, oid_arr, 2, oid_len);
                        String oid = decodeOID(oid_arr);
                        if (oid.equals(GSSUPMechOID.value.substring(4))) {
                            int len = token_len - oid_len;
                            byte[] init_tok_arr = new byte[len];
                            bais.read(init_tok_arr, 0, len);
                            Any a = codec.decode_value(init_tok_arr,
                                    InitialContextTokenHelper.type());
                            InitialContextToken token = InitialContextTokenHelper.extract(a);
                            if (token != null) {
                                gssup_tok.username = token.username;
                                gssup_tok.password = token.password;
                                gssup_tok.target_name = decodeGSSExportName(token.target_name).getBytes("UTF-8");

                                result = true;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // do nothing, return false
            }
        }
        return result;
    }

    public static String byteToString(byte[] data) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buffer.append(HEXCHAR[(data[i] >>> 4) & 0x0F]);
            buffer.append(HEXCHAR[(data[i]) & 0x0F]);
        }
        return buffer.toString();

    }

    private static final char[] HEXCHAR = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static void writeObject(Class type, Object object, OutputStream out) {
        if (type == Void.TYPE) {
            // do nothing for a void
        } else if (type == Boolean.TYPE) {
            out.write_boolean(((Boolean) object).booleanValue());
        } else if (type == Byte.TYPE) {
            out.write_octet(((Byte) object).byteValue());
        } else if (type == Character.TYPE) {
            out.write_wchar(((Character) object).charValue());
        } else if (type == Double.TYPE) {
            out.write_double(((Double) object).doubleValue());
        } else if (type == Float.TYPE) {
            out.write_float(((Float) object).floatValue());
        } else if (type == Integer.TYPE) {
            out.write_long(((Integer) object).intValue());
        } else if (type == Long.TYPE) {
            out.write_longlong(((Long) object).longValue());
        } else if (type == Short.TYPE) {
            out.write_short(((Short) object).shortValue());
        }  else {
            // object types must bbe written in the context of the corba application server
            // which properly write replaces our objects for corba
            ApplicationServer oldApplicationServer = ServerFederation.getApplicationServer();
            try {
                ServerFederation.setApplicationServer(corbaApplicationServer);

                // todo check if
                // copy the result to force replacement
                // corba does not call writeReplace on remote proxies
                //
                // HOWEVER, if this is an array, then we don't want to do the replacement 
                // because we can end up with a replacement element that's not compatible with the 
                // original array type, which results in an ArrayStoreException.  Fortunately, 
                // the Yoko RMI support appears to be able to sort this out for us correctly. 
                if (object instanceof Serializable && !object.getClass().isArray()) {
                    try {
                        object = copyObj(Thread.currentThread().getContextClassLoader(), object);
                    } catch (Exception e) {
                        log.debug("Exception in result copy", e);
                        throw new UnknownException(e);
                    }
                }

                if (type == Object.class || type == Serializable.class) {
                    javax.rmi.CORBA.Util.writeAny(out, object);
                } else if (org.omg.CORBA.Object.class.isAssignableFrom(type)) {
                    out.write_Object((org.omg.CORBA.Object) object);
                } else if (Remote.class.isAssignableFrom(type)) {
                    javax.rmi.CORBA.Util.writeRemoteObject(out, object);
                } else if (type.isInterface() && Serializable.class.isAssignableFrom(type)) {
                    javax.rmi.CORBA.Util.writeAbstractObject(out, object);
                } else {
                    out.write_value((Serializable) object, type);
                }
            } finally {
                ServerFederation.setApplicationServer(oldApplicationServer);
            }
        }
    }

    private static Object copyObj(ClassLoader classLoader, Object object) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStreamExt ois = new ObjectInputStreamExt(bais, classLoader);
        return ois.readObject();
    }

    public static Object readObject(Class type, InputStream in) {
        if (type == Void.TYPE) {
            return null;
        } else if (type == Boolean.TYPE) {
            return new Boolean(in.read_boolean());
        } else if (type == Byte.TYPE) {
            return new Byte(in.read_octet());
        } else if (type == Character.TYPE) {
            return new Character(in.read_wchar());
        } else if (type == Double.TYPE) {
            return new Double(in.read_double());
        } else if (type == Float.TYPE) {
            return new Float(in.read_float());
        } else if (type == Integer.TYPE) {
            return new Integer(in.read_long());
        } else if (type == Long.TYPE) {
            return new Long(in.read_longlong());
        } else if (type == Short.TYPE) {
            return new Short(in.read_short());
        } else if (type == Object.class || type == Serializable.class) {
            return javax.rmi.CORBA.Util.readAny(in);
        } else if (org.omg.CORBA.Object.class.isAssignableFrom(type)) {
            return in.read_Object(type);
        } else if (Remote.class.isAssignableFrom(type)) {
            return PortableRemoteObject.narrow(in.read_Object(), type);
        } else if (type.isInterface() && Serializable.class.isAssignableFrom(type)) {
            return in.read_abstract_interface();
        } else {
            return in.read_value(type);
        }
    }

    public static void throwException(Method method, InputStream in) throws Throwable {
        // read the exception id
        final String id = in.read_string();

        // get the class name from the id
        if (!id.startsWith("IDL:")) {
            log.warn("Malformed exception id: " + id);
            return;
        }

        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            Class exceptionType = exceptionTypes[i];

            String exceptionId = getExceptionId(exceptionType);
            if (id.equals(exceptionId)) {
                throw (Throwable) in.read_value(exceptionType);
            }
        }
        throw new UnexpectedException(id);
    }

    public static OutputStream writeUserException(Method method, ResponseHandler reply, Exception exception) throws Exception {
        if (exception instanceof RuntimeException || exception instanceof RemoteException) {
            throw exception;
        }

        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            Class exceptionType = exceptionTypes[i];
            if (!exceptionType.isInstance(exception)) {
                continue;
            }

            OutputStream out = (OutputStream) reply.createExceptionReply();
            String exceptionId = getExceptionId(exceptionType);
            out.write_string(exceptionId);
            out.write_value(exception);
            return out;
        }
        throw exception;
    }

    private static String getExceptionId(Class exceptionType) {
        String exceptionName = exceptionType.getName().replace('.', '/');
        if (exceptionName.endsWith("Exception")) {
            exceptionName = exceptionName.substring(0, exceptionName.length() - "Exception".length());
        }
        exceptionName += "Ex";
        String exceptionId = "IDL:" + exceptionName + ":1.0";
        return exceptionId;
    }

    public static String[] createCorbaIds(Class type) {
        List ids = new LinkedList();
        for (Iterator iterator = getAllInterfaces(type).iterator(); iterator.hasNext();) {
            Class superInterface = (Class) iterator.next();
            if (Remote.class.isAssignableFrom(superInterface) && superInterface != Remote.class) {
                ids.add("RMI:" + superInterface.getName() + ":0000000000000000");
            }
        }
        return (String[]) ids.toArray(new String[ids.size()]);
    }

    private static Set getAllInterfaces(Class intfClass) {
        Set allInterfaces = new LinkedHashSet();

        LinkedList stack = new LinkedList();
        stack.addFirst(intfClass);

        while (!stack.isEmpty()) {
            Class intf = (Class) stack.removeFirst();
            allInterfaces.add(intf);
            stack.addAll(0, Arrays.asList(intf.getInterfaces()));
        }

        return allInterfaces;
    }

    public static Map mapMethodToOperation(Class intfClass) {
        return iiopMap(intfClass, false);
    }

    public static Map mapOperationToMethod(Class intfClass) {
        return iiopMap(intfClass, true);
    }

    private static Map iiopMap(Class intfClass, boolean operationToMethod) {
        Method[] methods = getAllMethods(intfClass);

        // find every valid getter
        HashMap getterByMethod = new HashMap(methods.length);
        HashMap getterByName = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String methodName = method.getName();

            // no arguments allowed
            if (method.getParameterTypes().length != 0) {
                continue;
            }

            // must start with get or is
            String verb;
            if (methodName.startsWith("get") && methodName.length() > 3 && method.getReturnType() != void.class) {
                verb = "get";
            } else if (methodName.startsWith("is") && methodName.length() > 2 && method.getReturnType() == boolean.class) {
                verb = "is";
            } else {
                continue;
            }

            // must only throw Remote or Runtime Exceptions
            boolean exceptionsValid = true;
            Class[] exceptionTypes = method.getExceptionTypes();
            for (int j = 0; j < exceptionTypes.length; j++) {
                Class exceptionType = exceptionTypes[j];
                if (!RemoteException.class.isAssignableFrom(exceptionType) &&
                        !RuntimeException.class.isAssignableFrom(exceptionType) &&
                        !Error.class.isAssignableFrom(exceptionType)) {
                    exceptionsValid = false;
                    break;
                }
            }
            if (!exceptionsValid) {
                continue;
            }

            String propertyName;
            if (methodName.length() > verb.length() + 1 && Character.isUpperCase(methodName.charAt(verb.length() + 1))) {
                propertyName = methodName.substring(verb.length());
            } else {
                propertyName = Character.toLowerCase(methodName.charAt(verb.length())) + methodName.substring(verb.length() + 1);
            }
            getterByMethod.put(method, propertyName);
            getterByName.put(propertyName, method);
        }

        HashMap setterByMethod = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            String methodName = method.getName();

            // must have exactally one arg
            if (method.getParameterTypes().length != 1) {
                continue;
            }

            // must return non void
            if (method.getReturnType() != void.class) {
                continue;
            }

            // must start with set
            if (!methodName.startsWith("set") || methodName.length() <= 3) {
                continue;
            }

            // must only throw Remote or Runtime Exceptions
            boolean exceptionsValid = true;
            Class[] exceptionTypes = method.getExceptionTypes();
            for (int j = 0; j < exceptionTypes.length; j++) {
                Class exceptionType = exceptionTypes[j];
                if (!RemoteException.class.isAssignableFrom(exceptionType) &&
                        !RuntimeException.class.isAssignableFrom(exceptionType) &&
                        !Error.class.isAssignableFrom(exceptionType)) {
                    exceptionsValid = false;
                    break;
                }
            }
            if (!exceptionsValid) {
                continue;
            }

            String propertyName;
            if (methodName.length() > 4 && Character.isUpperCase(methodName.charAt(4))) {
                propertyName = methodName.substring(3);
            } else {
                propertyName = Character.toLowerCase(methodName.charAt(3)) + methodName.substring(4);
            }

            // must have a matching getter
            Method getter = (Method) getterByName.get(propertyName);
            if (getter == null) {
                continue;
            }

            // setter property must match gettter return value
            if (!method.getParameterTypes()[0].equals(getter.getReturnType())) {
                continue;
            }
            setterByMethod.put(method, propertyName);
        }

        // index the methods by name... used to determine which methods are overloaded
        HashMap overloadedMethods = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (getterByMethod.containsKey(method) || setterByMethod.containsKey(method)) {
                continue;
            }
            String methodName = method.getName();
            List methodList = (List) overloadedMethods.get(methodName);
            if (methodList == null) {
                methodList = new LinkedList();
                overloadedMethods.put(methodName, methodList);
            }
            methodList.add(method);
        }

        // index the methods by lower case name... used to determine which methods differ only by case
        HashMap caseCollisionMethods = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];
            if (getterByMethod.containsKey(method) || setterByMethod.containsKey(method)) {
                continue;
            }
            String lowerCaseMethodName = method.getName().toLowerCase();
            Set methodSet = (Set) caseCollisionMethods.get(lowerCaseMethodName);
            if (methodSet == null) {
                methodSet = new HashSet();
                caseCollisionMethods.put(lowerCaseMethodName, methodSet);
            }
            methodSet.add(method.getName());
        }

        String className = getClassName(intfClass);
        Map iiopMap = new HashMap(methods.length);
        for (int i = 0; i < methods.length; i++) {
            Method method = methods[i];

            String iiopName = (String) getterByMethod.get(method);
            if (iiopName != null) {
                // if we have a leading underscore prepend with J
                if (iiopName.charAt(0) == '_') {
                    iiopName = "J_get_" + iiopName.substring(1);
                } else {
                    iiopName = "_get_" + iiopName;
                }
            } else {
                iiopName = (String) setterByMethod.get(method);
                if (iiopName != null) {
                    // if we have a leading underscore prepend with J
                    if (iiopName.charAt(0) == '_') {
                        iiopName = "J_set_" + iiopName.substring(1);
                    } else {
                        iiopName = "_set_" + iiopName;
                    }
                } else {
                    iiopName = method.getName();

                    // if we have a leading underscore prepend with J
                    if (iiopName.charAt(0) == '_') {
                        iiopName = "J" + iiopName;
                    }
                }
            }

            // if this name only differs by case add the case index to the end
            Set caseCollisions = (Set) caseCollisionMethods.get(method.getName().toLowerCase());
            if (caseCollisions != null && caseCollisions.size() > 1) {
                iiopName += upperCaseIndexString(iiopName);
            }

            // if this is an overloaded method append the parameter string
            List overloads = (List) overloadedMethods.get(method.getName());
            if (overloads != null && overloads.size() > 1) {
                iiopName += buildOverloadParameterString(method.getParameterTypes());
            }

            // if we have a leading underscore prepend with J
            iiopName = replace(iiopName, '$', "U0024");

            // if we have matched a keyword prepend with an underscore
            if (keywords.contains(iiopName.toLowerCase())) {
                iiopName = "_" + iiopName;
            }

            // if the name is the same as the class name, append an underscore
            if (iiopName.equalsIgnoreCase(className)) {
                iiopName += "_";
            }

            if (operationToMethod) {
                iiopMap.put(iiopName, method);
            } else {
                iiopMap.put(method, iiopName);
            }
        }

        return iiopMap;
    }

    private static Method[] getAllMethods(Class intfClass) {
        LinkedList methods = new LinkedList();
        for (Iterator iterator = getAllInterfaces(intfClass).iterator(); iterator.hasNext();) {
            Class intf = (Class) iterator.next();
            methods.addAll(Arrays.asList(intf.getDeclaredMethods()));
        }

        return (Method[]) methods.toArray(new Method[methods.size()]);
    }

    /**
     * Return the a string containing an underscore '_' index of each uppercase character in the iiop name.
     *
     * This is used for distinction of names that only differ by case, since corba does not support case sensitive names.
     */
    private static String upperCaseIndexString(String iiopName) {
        StringBuffer stringBuffer = new StringBuffer();
        for (int i = 0; i < iiopName.length(); i++) {
            char c = iiopName.charAt(i);
            if (Character.isUpperCase(c)) {
                stringBuffer.append('_').append(i);
            }
        }
        return stringBuffer.toString();
    }

    /**
     * Replaces any occurnace of the specified "oldChar" with the nes string.
     *
     * This is used to replace occurances if '$' in corba names since '$' is a special character
     */
    private static String replace(String source, char oldChar, String newString) {
        StringBuffer stringBuffer = new StringBuffer(source.length());
        for (int i = 0; i < source.length(); i++) {
            char c = source.charAt(i);
            if (c == oldChar) {
                stringBuffer.append(newString);
            } else {
                stringBuffer.append(c);
            }
        }
        return stringBuffer.toString();
    }

    /**
     * Return the a string containing a double underscore '__' list of parameter types encoded using the Java to IDL rules.
     *
     * This is used for distinction of methods that only differ by parameter lists.
     */
    private static String buildOverloadParameterString(Class[] parameterTypes) {
        String name = "";
        if (parameterTypes.length ==0) {
            name += "__";
        } else {
            for (int i = 0; i < parameterTypes.length; i++) {
                Class parameterType = parameterTypes[i];
                name += buildOverloadParameterString(parameterType);
            }
        }
        return name.replace('.', '_');
    }

    /**
     * Returns a single parameter type encoded using the Java to IDL rules.
     */
    private static String buildOverloadParameterString(Class parameterType) {
        String name = "_";

        int arrayDimensions = 0;
        while (parameterType.isArray()) {
            arrayDimensions++;
            parameterType = parameterType.getComponentType();
        }

        // arrays start with org_omg_boxedRMI_
        if (arrayDimensions > 0) {
            name += "_org_omg_boxedRMI";
        }

        // IDLEntity types must be prefixed with org_omg_boxedIDL_
        if (IDLEntity.class.isAssignableFrom(parameterType)) {
            name += "_org_omg_boxedIDL";
        }

        // add package... some types have special mappings in corba
        String packageName = (String) specialTypePackages.get(parameterType.getName());
        if (packageName == null) {
            packageName = getPackageName(parameterType.getName());
        }
        if (packageName.length() > 0) {
            name += "_" + packageName;
        }

        // arrays now contain a dimension indicator
        if (arrayDimensions > 0) {
            name += "_" + "seq" + arrayDimensions;
        }

        // add the class name
        String className = (String) specialTypeNames.get(parameterType.getName());
        if (className == null) {
            className = buildClassName(parameterType);
        }
        name += "_" + className;

        return name;
    }

    /**
     * Returns a string contianing an encoded class name.
     */
    private static String buildClassName(Class type) {
        if (type.isArray()) {
            throw new IllegalArgumentException("type is an array: " + type);
        }

        // get the classname
        String typeName = type.getName();
        int endIndex = typeName.lastIndexOf('.');
        if (endIndex < 0) {
            return typeName;
        }
        StringBuffer className = new StringBuffer(typeName.substring(endIndex + 1));

        // for innerclasses replace the $ separator with two underscores
        // we can't just blindly replace all $ characters since class names can contain the $ character
        if (type.getDeclaringClass() != null) {
            String declaringClassName = getClassName(type.getDeclaringClass());
            assert className.toString().startsWith(declaringClassName + "$");
            className.replace(declaringClassName.length(), declaringClassName.length() + 1, "__");
        }

        // if we have a leading underscore prepend with J
        if (className.charAt(0) == '_') {
            className.insert(0, "J");
        }
        return className.toString();
    }

    private static String getClassName(Class type) {
        if (type.isArray()) {
            throw new IllegalArgumentException("type is an array: " + type);
        }

        // get the classname
        String typeName = type.getName();
        int endIndex = typeName.lastIndexOf('.');
        if (endIndex < 0) {
            return typeName;
        }
        return typeName.substring(endIndex + 1);
    }

    private static String getPackageName(String interfaceName) {
        int endIndex = interfaceName.lastIndexOf('.');
        if (endIndex < 0) {
            return "";
        }
        return interfaceName.substring(0, endIndex);
    }

    private static final Map specialTypeNames;
    private static final Map specialTypePackages;
    private static final Set keywords;

    static {
       specialTypeNames = new HashMap();
       specialTypeNames.put("boolean", "boolean");
       specialTypeNames.put("char", "wchar");
       specialTypeNames.put("byte", "octet");
       specialTypeNames.put("short", "short");
       specialTypeNames.put("int", "long");
       specialTypeNames.put("long", "long_long");
       specialTypeNames.put("float", "float");
       specialTypeNames.put("double", "double");
       specialTypeNames.put("java.lang.Class", "ClassDesc");
       specialTypeNames.put("java.lang.String", "WStringValue");
       specialTypeNames.put("org.omg.CORBA.Object", "Object");

       specialTypePackages = new HashMap();
       specialTypePackages.put("boolean", "");
       specialTypePackages.put("char", "");
       specialTypePackages.put("byte", "");
       specialTypePackages.put("short", "");
       specialTypePackages.put("int", "");
       specialTypePackages.put("long", "");
       specialTypePackages.put("float", "");
       specialTypePackages.put("double", "");
       specialTypePackages.put("java.lang.Class", "javax.rmi.CORBA");
       specialTypePackages.put("java.lang.String", "CORBA");
       specialTypePackages.put("org.omg.CORBA.Object", "");

       keywords = new HashSet();
       keywords.add("abstract");
       keywords.add("any");
       keywords.add("attribute");
       keywords.add("boolean");
       keywords.add("case");
       keywords.add("char");
       keywords.add("const");
       keywords.add("context");
       keywords.add("custom");
       keywords.add("default");
       keywords.add("double");
       keywords.add("enum");
       keywords.add("exception");
       keywords.add("factory");
       keywords.add("false");
       keywords.add("fixed");
       keywords.add("float");
       keywords.add("in");
       keywords.add("inout");
       keywords.add("interface");
       keywords.add("long");
       keywords.add("module");
       keywords.add("native");
       keywords.add("object");
       keywords.add("octet");
       keywords.add("oneway");
       keywords.add("out");
       keywords.add("private");
       keywords.add("public");
       keywords.add("raises");
       keywords.add("readonly");
       keywords.add("sequence");
       keywords.add("short");
       keywords.add("string");
       keywords.add("struct");
       keywords.add("supports");
       keywords.add("switch");
       keywords.add("true");
       keywords.add("truncatable");
       keywords.add("typedef");
       keywords.add("union");
       keywords.add("unsigned");
       keywords.add("valuebase");
       keywords.add("valuetype");
       keywords.add("void");
       keywords.add("wchar");
       keywords.add("wstring");
    }

}
