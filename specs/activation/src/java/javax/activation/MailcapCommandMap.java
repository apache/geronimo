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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @version $Rev$ $Date$
 */
public class MailcapCommandMap extends CommandMap {
    private final Map preferredCommands = new HashMap();
    private final Map allCommands = new HashMap();
    private URL url;
    private ClassLoader cl;

    public MailcapCommandMap() {
        // process /META-INF/mailcap.default
        try {
            InputStream is = MailcapCommandMap.class.getResourceAsStream("/META-INF/mailcap.default");
            if (is != null) {
                try {
                    parseMailcap(is);
                } finally {
                    is.close();
                }
            }
        } catch (IOException e) {
            // ignore
        }

        // process /META-INF/mailcap resources
        try {
            cl = MailcapCommandMap.class.getClassLoader();
            Enumeration e = cl.getResources("META-INF/mailcap");
            while (e.hasMoreElements()) {
                url = ((URL) e.nextElement());
                try {
                    InputStream is = url.openStream();
                    try {
                        parseMailcap(is);
                    } finally {
                        is.close();
                    }
                } catch (IOException e1) {
                    continue;
                }
            }
        } catch (SecurityException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        // process ${java.home}/lib/mailcap
        try {
            File file = new File(System.getProperty("java.home"), "lib/mailcap");
            InputStream is = new FileInputStream(file);
            try {
                parseMailcap(is);
            } finally {
                is.close();
            }
        } catch (SecurityException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }

        // process ${user.home}/lib/mailcap
        try {
            File file = new File(System.getProperty("user.home"), ".mailcap");
            InputStream is = new FileInputStream(file);
            try {
                parseMailcap(is);
            } finally {
                is.close();
            }
        } catch (SecurityException e) {
            // ignore
        } catch (IOException e) {
            // ignore
        }
    }

    public MailcapCommandMap(String fileName) throws IOException {
        this();
        FileReader reader = new FileReader(fileName);
        try {
            parseMailcap(reader);
        } finally {
            reader.close();
        }
    }

    public MailcapCommandMap(InputStream is) {
        this();
        parseMailcap(is);
    }

    private void parseMailcap(InputStream is) {
        try {
            parseMailcap(new InputStreamReader(is));
        } catch (IOException e) {
            // spec API means all we can do is swallow this
        }
    }

    void parseMailcap(Reader reader) throws IOException {
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            addMailcap(line);
        }
    }

    public synchronized void addMailcap(String mail_cap) {
        int index = 0;
        // skip leading whitespace
        index = skipSpace(mail_cap, index);
        if (index == mail_cap.length() || mail_cap.charAt(index) == '#') {
            return;
        }

        // get primary type
        int start = index;
        index = getToken(mail_cap, index);
        if (start == index) {
            return;
        }
        String mimeType = mail_cap.substring(start, index);

        // skip any spaces after the primary type
        index = skipSpace(mail_cap, index);
        if (index == mail_cap.length() || mail_cap.charAt(index) == '#') {
            return;
        }

        // get sub-type
        if (mail_cap.charAt(index) == '/') {
            index = skipSpace(mail_cap, ++index);
            start = index;
            index = getToken(mail_cap, index);
            mimeType = mimeType + '/' + mail_cap.substring(start, index);
        } else {
            mimeType = mimeType + "/*";
        }

        // skip spaces after mime type
        index = skipSpace(mail_cap, index);

        // expect a ';' to terminate field 1
        if (index == mail_cap.length() || mail_cap.charAt(index) != ';') {
            return;
        }
        index = getMText(mail_cap, index);
        // expect a ';' to terminate field 2
        if (index == mail_cap.length() || mail_cap.charAt(index) != ';') {
            return;
        }

        // parse fields
        while (index < mail_cap.length() && mail_cap.charAt(index) == ';') {
            index = skipSpace(mail_cap, index + 1);
            start = index;
            index = getToken(mail_cap, index);
            String fieldName = mail_cap.substring(start, index).toLowerCase();
            index = skipSpace(mail_cap, index);
            if (index < mail_cap.length() && mail_cap.charAt(index) == '=') {
                index = skipSpace(mail_cap, index + 1);
                start = index;
                index = getMText(mail_cap, index);
                String value = mail_cap.substring(start, index);
                index = skipSpace(mail_cap, index);
                if (fieldName.startsWith("x-java-") && fieldName.length() > 7) {
                    String command = fieldName.substring(7);
                    addCommand(mimeType, command, value);
                }
            }
        }

    }

    private void addCommand(String mimeType, String cmdName, String commandClass) {
        CommandInfo info = new CommandInfo(cmdName, commandClass);

        Map commands = (Map) preferredCommands.get(mimeType);
        if (commands == null) {
            commands = new HashMap();
            preferredCommands.put(mimeType, commands);
        }
        commands.put(info.getCommandName(), info);

        List cmdList = (List) allCommands.get(mimeType);
        if (cmdList == null) {
            cmdList = new ArrayList();
            allCommands.put(mimeType, cmdList);
        }
        cmdList.add(info);
    }

    private int skipSpace(String s, int index) {
        while (index < s.length() && Character.isWhitespace(s.charAt(index))) {
            index++;
        }
        return index;
    }

    private int getToken(String s, int index) {
        while (index < s.length() && s.charAt(index) != '#' && !MimeType.isSpecial(s.charAt(index))) {
            index++;
        }
        return index;
    }

    private int getMText(String s, int index) {
        while (index < s.length()) {
            char c = s.charAt(index);
            if (c == '#' || c == ';' || Character.isISOControl(c)) {
                return index;
            }
            if (c == '\\') {
                index++;
                if (index == s.length()) {
                    return index;
                }
            }
            index++;
        }
        return index;
    }

    public synchronized CommandInfo[] getPreferredCommands(String mimeType) {
        Map commands = (Map) preferredCommands.get(mimeType.toLowerCase());
        if (commands == null) {
            return null;
        }
        return (CommandInfo[]) commands.values().toArray(new CommandInfo[commands.size()]);
    }

    public synchronized CommandInfo[] getAllCommands(String mimeType) {
        List commands = (List) allCommands.get(mimeType.toLowerCase());
        return (CommandInfo[]) commands.toArray(new CommandInfo[commands.size()]);
    }

    public synchronized CommandInfo getCommand(String mimeType, String cmdName) {
        // search for an exact match
        Map commands = (Map) preferredCommands.get(mimeType.toLowerCase());
        if(commands != null) {
            return (CommandInfo) commands.get(cmdName.toLowerCase());
        }
        int i = mimeType.indexOf('/');
        if (i == -1) {
            mimeType = mimeType + "/*";
        } else {
            mimeType = mimeType.substring(0, i + 1) + "*";
        }
        commands = (Map) preferredCommands.get(mimeType.toLowerCase());
        if(commands != null)
            return (CommandInfo) commands.get(cmdName.toLowerCase());
        return null;
    }

    public synchronized DataContentHandler createDataContentHandler(String mimeType) {
        CommandInfo info = getCommand(mimeType, "content-handler");
        if (info == null) {
            return null;
        }

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) {
            cl = getClass().getClassLoader();
        }
        try {
            return (DataContentHandler) cl.loadClass(info.getCommandClass()).newInstance();
        } catch (ClassNotFoundException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        } catch (InstantiationException e) {
            return null;
        }
    }
}
