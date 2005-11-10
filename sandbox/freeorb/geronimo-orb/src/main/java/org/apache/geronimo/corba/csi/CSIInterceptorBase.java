/**
 *
 * Copyright 2005 The Apache Software Foundation or its licensors, as applicable.
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
package org.apache.geronimo.corba.csi;

import org.omg.CORBA.Any;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.MARSHAL;
import org.omg.CORBA.ORB;
import org.omg.CORBA.OctetSeqHelper;
import org.omg.CORBA.UserException;
import org.omg.CSI.AuthorizationElement;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.CompoundSecMechanismsHelper;
import org.omg.GSSUP.InitialContextToken;
import org.omg.GSSUP.InitialContextTokenHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.FormatMismatch;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.CodecPackage.TypeMismatch;
import org.omg.IOP.SecurityAttributeService;
import org.omg.IOP.ServiceContext;
import org.omg.IOP.TaggedComponent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


public abstract class CSIInterceptorBase extends LocalObject {

    private static final Log log = LogFactory.getLog(CSIInterceptorBase.class);

    static final AuthorizationElement[] EMPTY_AUTH_ELEMENT = new AuthorizationElement[0];

    static final byte[] EMPTY_BARR = new byte[0];

    ORB orb;

    final protected Codec codec;

    CSIInterceptorBase(Codec codec) {
        this.codec = codec;
    }

    /**
     * we need to ORB to be able to create the Any's into which we encode
     * various info
     */
    protected final ORB getOrb() {
        if (orb == null) {
            orb = ORB.init();
        }

        return orb;
    }

    SASContextBody decodeSASContextBody(ServiceContext sasSC) {
        //
        // Decode encapsulated SAS context body
        //
        /*
           * org.omg.CORBA.portable.InputStream in =
           * Porting.open_encapsulated_input_stream( sasSC.context_data, 0,
           * sasSC.context_data.length, getOrb());
           *
           * return SASContextBodyHelper.read(in);
           */
        Any any;
        try {
            any = codec.decode_value(sasSC.context_data, SASContextBodyHelper
                    .type());
        }
        catch (FormatMismatch ex) {
            throw new org.omg.CORBA.INTERNAL(ex.getMessage());
        }
        catch (TypeMismatch ex) {
            throw new org.omg.CORBA.INTERNAL(ex.getMessage());
        }
        return SASContextBodyHelper.extract(any);
    }

    CompoundSecMechList decodeCompoundSecMechList(TaggedComponent seccomp)
            throws FormatMismatch, TypeMismatch
    {
        /*
           * org.omg.CORBA.portable.InputStream in = openEncapsulatedInputStream(
           * seccomp.component_data, 0, seccomp.component_data.length, getOrb());
           *
           * return CompoundSecMechListHelper.read(in);
           */
        Any any = codec.decode_value(seccomp.component_data,
                                     CompoundSecMechanismsHelper.type());
        return CompoundSecMechListHelper.extract(any);
    }

    byte[] utf8encode(String text) {
        if (text == null) {
            return EMPTY_BARR;
        } else {
            try {
                return text.getBytes("UTF8");
            }
            catch (java.io.UnsupportedEncodingException ex) {
                throw new org.omg.CORBA.INTERNAL(ex.getMessage());
            }
        }
    }

    String utf8decode(byte[] data) {
        try {
            return new String(data, "UTF8");
        }
        catch (java.io.UnsupportedEncodingException ex) {
            throw new org.omg.CORBA.INTERNAL(ex.getMessage());
        }
    }

    static final byte[] GSSUP_OID = {0x06, // OBJECT IDENTIFIER
            6, // length of OID
            (2 * 40 + 23), // ISO[2]*40 + INTERNATIONAL[23]
            (byte) 0x81, // 0x80 | (OMG[130] >> 7)
            130 & 0x7f, // OMG[130] & 0x7f
            1, // SECURITY[1]
            1, // AUTHENTICATION[1]
            1 // GSSUP-MECH[1]
    };

    byte[] encapsulateByteArray(byte[] data) {
        // org.omg.CORBA.portable.OutputStream out =
        // Porting.create_encapsulated_output_stream();
        //
        // out.write_long(data.length);
        // out.write_octet_array(data, 0, data.length);
        //
        // return Porting.extract_data(out);

        Any a = getOrb().create_any();
        OctetSeqHelper.insert(a, data);

        try {
            return codec.encode_value(a);
        }
        catch (InvalidTypeForEncoding e) {
            MARSHAL me = new MARSHAL("cannot encode security descriptor", 0,
                                     CompletionStatus.COMPLETED_NO);
            me.initCause(e);
            throw me;
        }
    }

    byte[] encodeGSSUPToken(InitialContextToken gssupToken) {

        // first, create the Any encoding of the token
        Any a = getOrb().create_any();
        InitialContextTokenHelper.insert(a, gssupToken);

        //OutputStream out = a.create_output_stream();
        //a.type(InitialContextTokenHelper.type());
        //InitialContextTokenHelper.write(out, gssupToken);
        //InputStream in = out.create_input_stream();
        //a.read_value(in, InitialContextTokenHelper.type());

        byte[] data;
        try {
            data = codec.encode_value(a);
        }
        catch (InvalidTypeForEncoding e) {
            MARSHAL me = new MARSHAL("cannot encode security descriptor", 0,
                                     CompletionStatus.COMPLETED_NO);
            me.initCause(e);
            throw me;
        }

        //
        // next, wrap the byte encoding in the ASN.1 magic
        //
        int len = data.length + GSSUP_OID.length;
        if (len < (1 << 7)) {
            byte[] result = new byte[len + 2];
            result[0] = 0x60;
            result[1] = (byte) len;
            System.arraycopy(GSSUP_OID, 0, result, 2, GSSUP_OID.length);
            System.arraycopy(data, 0, result, 10, data.length);
            return result;

        } else if (len < (1 << 14)) {
            byte[] result = new byte[len + 3];
            result[0] = 0x60;
            result[1] = (byte) ((byte) (len >> 7) | (byte) 0x80);
            result[2] = ((byte) (len & 0x7f));
            System.arraycopy(GSSUP_OID, 0, result, 3, GSSUP_OID.length);
            System.arraycopy(data, 0, result, 11, data.length);
            return result;

        } else if (len < (1 << 21)) {
            byte[] result = new byte[len + 4];
            result[0] = 0x60;
            result[2] = (byte) ((byte) 0x80 | (byte) (0x7f & (len >> 14)));
            result[1] = (byte) ((byte) 0x80 | (byte) (0x7f & (len >> 7)));
            result[3] = (byte) (len & 0x7f);
            System.arraycopy(GSSUP_OID, 0, result, 4, GSSUP_OID.length);
            System.arraycopy(data, 0, result, 12, data.length);
            return result;

        } else {
            throw new org.omg.CORBA.INTERNAL("user/password too long");
        }

        // return data;
    }

    InitialContextToken decodeGSSUPToken(byte[] data) {
        if (data[0] != 0x60)
            throw new org.omg.CORBA.MARSHAL("Invalid Token");

        int idx = 1;
        int len = 0;
        byte b;

        // collect length
        do {
            len <<= 7;
            len |= (b = data[idx++]) & 0x7f;
        }
        while ((b & 0x80) == 0x80);

        if ((len + idx) != data.length)
            throw new org.omg.CORBA.MARSHAL("Bad Token Size");

        for (int i = 0; i < GSSUP_OID.length; i++) {
            if (data[idx + i] != GSSUP_OID[i]) {
                throw new org.omg.CORBA.NO_PERMISSION("Not GSSUP_OID");
            }
        }

        idx += GSSUP_OID.length;

        byte[] token = new byte[data.length - idx];
        System.arraycopy(data, idx, token, 0, data.length - idx);

        try {
            Any a = codec.decode_value(data, InitialContextTokenHelper.type());
            return InitialContextTokenHelper.extract(a);
        }
        catch (UserException e) {
            MARSHAL me = new MARSHAL("cannot decode local security descriptor",
                                     0, CompletionStatus.COMPLETED_NO);
            me.initCause(e);
            throw me;
        }
    }

    ServiceContext encodeSASContextBody(SASContextBody sasBody) {
        //
        // Create encapsulation for SAS context body
        //

        Any a = getOrb().create_any();
        SASContextBodyHelper.insert(a, sasBody);

        // wrap the ANY in an encapsulation
        byte[] data;
        try {
            data = codec.encode_value(a);
        }
        catch (UserException ex) {
            MARSHAL me = new MARSHAL("cannot encode local security descriptor",
                                     0, CompletionStatus.COMPLETED_NO);
            me.initCause(ex);
            throw me;
        }
        return new ServiceContext(SecurityAttributeService.value, data);
    }


    //
    // thread-local mechanism to shortcut local calls
    //
    static class CallStatus {

        boolean isLocal;

        CallStatus prev;

        CallStatus(boolean l, CallStatus p) {
            isLocal = l;
            prev = p;
        }

        static ThreadLocal status = new ThreadLocal();

        static void pushIsLocal(boolean isLocal) {
            CallStatus cs = new CallStatus(isLocal, (CallStatus) status.get());
            status.set(cs);
        }

        static boolean peekIsLocal() {
            CallStatus cs = (CallStatus) status.get();
            if (cs == null)
                return false;
            else
                return cs.isLocal;
        }

        static boolean popIsLocal() {
            CallStatus cs = (CallStatus) status.get();
            if (cs == null)
                return false;

            status.set(cs.prev);
            return cs.isLocal;
        }
    }

    /**
     * RFC 2743, Section 3.2. Construct a GSS_ExportedName for a GSSUP domain
     * given a String
     */
    byte[] encodeGSSExportedName(String value) {
        byte[] name_data = utf8encode(value);

        int len = 8 + name_data.length + GSSUP_OID.length;

        byte[] result = new byte[len];

        result[0] = 0x04; // Token Identifier
        result[1] = 0x01;

        result[2] = 0x00; // 2-byte Length of GSSUP_OID
        result[3] = (byte) GSSUP_OID.length;

        // the OID
        for (int i = 0; i < GSSUP_OID.length; i++) {
            result[4 + i] = GSSUP_OID[i];
        }

        int name_len = name_data.length;
        int idx = 4 + GSSUP_OID.length;

        // 4-byte length of name
        result[idx + 0] = (byte) ((name_len >> 24) & 0xff);
        result[idx + 1] = (byte) ((name_len >> 16) & 0xff);
        result[idx + 2] = (byte) ((name_len >> 8) & 0xff);
        result[idx + 3] = (byte) ((name_len) & 0xff);

        for (int i = 0; i < name_len; i++) {
            result[idx + 4 + i] = name_data[i];
        }

        return result;
    }

    String decodeGSSExportedName(byte[] data) {
        if (data.length < 8 + GSSUP_OID.length) {
            log.debug("exported name too short len=" + data.length);
            return null;
        }

        if (data[0] != 0x04 || data[1] != 0x01 || data[2] != 0x00
            || data[3] != GSSUP_OID.length)
        {
            log.debug("wrong name header");
            return null;
        }

        for (int i = 0; i < GSSUP_OID.length; i++) {
            if (data[4 + i] != GSSUP_OID[i]) {
                log.debug("wrong name OID @ " + i);
                return null;
            }
        }

        int idx = 4 + GSSUP_OID.length;
        int len = (((int) data[idx + 0] << 24) & 0xff000000)
                  | (((int) data[idx + 1] << 16) & 0x00ff0000)
                  | (((int) data[idx + 2] << 8) & 0x0000ff00)
                  | (((int) data[idx + 3] << 0) & 0x000000ff);

        try {
            return new String(data, idx + 4, data.length - (idx + 4), "UTF8");
        }
        catch (java.io.UnsupportedEncodingException ex) {
            throw new org.omg.CORBA.INTERNAL(ex.getMessage());
        }
    }

}
