/**
 *
 *  Copyright 2004-2005 The Apache Software Foundation
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
 *
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.interop.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.PrintStream;

import org.apache.geronimo.interop.SystemException;


public class ProcessUtil
        //implements org.apache.geronimo.interop.bootstrap.BootstrapObject
        {
    /*
    public static final Attribute[] attributes =
    {
        // Bootstrap classes must have inline static attributes.
    }
    ;
    */

    //public static final Component component = new Component(ProcessUtil.class);

    public static ProcessUtil getInstance() {
        /*
        if (component == null || SystemProperties.bootstrap())
        {
            return new ProcessUtil();
        }
        else
        {
            return (ProcessUtil)component.getInstance();
        }
        */

        return new ProcessUtil();
    }

    // private data

    private String _cmd;

    private boolean _echo;

    private PrintStream _echoStream;

    private int _exitValue;

    private byte[] _errorBytes;

    private byte[] _inputBytes;

    // internal methods

    protected ProcessUtil() {
        // Used by getInstance in bootstrap mode.
        // Prevents direct instantiation of class.
    }

    // public methods

    public void setEcho(boolean echo) {
        _echo = echo;
        if (_echo) {
            _echoStream = System.out;
        } else {
            _echoStream = null;
        }
    }

    public void setEcho(PrintStream stream) {
        _echo = stream != null;
        _echoStream = stream;
    }

    public void run(String cmd) {
        run(cmd, null, null);
    }

    public void run(String cmd, String[] env, String dir) {
        _cmd = cmd;
        Process process;
        try {
            if (_echo) {
                _echoStream.println(cmd);
            }
            File dirFile = dir == null ? null : new File(dir);
            process = Runtime.getRuntime().exec(cmd, env, dirFile);
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
        run(process);
    }

    public void run(String[] cmd, String[] env, String dir) {
        _cmd = cmd.length == 0 ? "" : cmd[0];
        for (int i = 1; i < cmd.length; i++) {
            _cmd += " " + cmd[i];
        }
        Process process;
        try {
            if (_echo) {
                _echoStream.println(cmd);
            }
            File dirFile = dir == null ? null : new File(dir);
            process = Runtime.getRuntime().exec(cmd, env, dirFile);
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
        run(process);
    }

    public void run(Process process) {
        try {
            GetBytesThread errorThread = new GetBytesThread(process.getErrorStream());
            GetBytesThread inputThread = new GetBytesThread(process.getInputStream());
            errorThread.start();
            inputThread.start();
            process.waitFor();
            errorThread.join();
            inputThread.join();
            _errorBytes = errorThread.getBytes();
            _inputBytes = inputThread.getBytes();
            _exitValue = process.exitValue();
        } catch (Exception ex) {
            throw new SystemException(ex);
        }
    }

    public void checkStatus() {
        if (_exitValue != 0) {
            // TODO: I18N
            String result = getResult();
            throw new SystemException("Command Failed: " + _cmd
                                      + "\nExit Status: " + _exitValue
                                      + (result.length() == 0 ? "" : ("\nOutput: " + getResult())));
        }
    }

    public int exitValue() {
        return _exitValue;
    }

    public String getResult() {
        return new String(getResultBytes());
    }

    public String getError() {
        return new String(getErrorBytes());
    }

    public String getInput() {
        return new String(getInputBytes());
    }

    public byte[] getResultBytes() {
        byte[] bytes = new byte[_errorBytes.length + _inputBytes.length];
        System.arraycopy(_errorBytes, 0, bytes, 0, _errorBytes.length);
        System.arraycopy(_inputBytes, 0, bytes, _errorBytes.length, _inputBytes.length);
        return bytes;
    }

    public byte[] getErrorBytes() {
        return _errorBytes;
    }

    public byte[] getInputBytes() {
        return _inputBytes;
    }

    private class GetBytesThread extends Thread {
        InputStream _input;

        ByteArrayOutputStream _bytes;

        GetBytesThread(InputStream input) {
            _input = new BufferedInputStream(input);
            _bytes = new ByteArrayOutputStream();
            setDaemon(true);
        }

        public void run() {
            try {
                int c;
                while ((c = _input.read()) != -1) {
                    _bytes.write(c);
                    if (_echo) {
                        _echoStream.print((char) c);
                        if (c == '\n') {
                            _echoStream.flush();
                        }
                    }
                }
            } catch (Exception ex) {
                throw new SystemException(ex);
            }
        }

        public byte[] getBytes() {
            return _bytes.toByteArray();
        }
    }
}
