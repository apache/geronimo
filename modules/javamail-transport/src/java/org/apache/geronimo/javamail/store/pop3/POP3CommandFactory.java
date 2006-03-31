/**
 *
 * Copyright 2003-2005 The Apache Software Foundation
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

package org.apache.geronimo.javamail.store.pop3;

/**
 * Provides concrete implementations of
 * org.apache.geronimo.javamail.store.pop3.POP3Command objects representing the
 * POP3 commands defined in rfc 1939
 * 
 * @link http://www.faqs.org/rfcs/rfc1939.html
 * @version $Rev$ $Date$
 */
public final class POP3CommandFactory implements POP3Constants {

    public static POP3Command getCOMMAND_USER(final String user) {
        return new POP3Command() {
            public String getCommand() {
                return "USER" + SPACE + user + CRLF;
            }

            public boolean isMultiLineResponse() {
                return false;
            }
        };
    }

    public static POP3Command getCOMMAND_PASS(final String passwd) {
        return new POP3Command() {
            public String getCommand() {
                return "PASS" + SPACE + passwd + CRLF;
            }

            public boolean isMultiLineResponse() {
                return false;
            }
        };
    }

    public static POP3Command getCOMMAND_QUIT() {
        return new POP3Command() {
            public String getCommand() {
                return "QUIT" + CRLF;
            }

            public boolean isMultiLineResponse() {
                return false;
            }
        };
    }

    public static POP3Command getCOMMAND_NOOP() {
        return new POP3Command() {
            public String getCommand() {
                return "NOOP" + CRLF;
            }

            public boolean isMultiLineResponse() {
                return false;
            }
        };
    }

    public static POP3Command getCOMMAND_STAT() {
        return new POP3Command() {
            public String getCommand() {
                return "STAT" + CRLF;
            }

            public boolean isMultiLineResponse() {
                return false;
            }
        };
    }

    public static POP3Command getCOMMAND_LIST() {
        return getCOMMAND_LIST(-1);
    }

    public static POP3Command getCOMMAND_LIST(final int msgNo) {
        return new POP3Command() {
            public String getCommand() {
                if (msgNo > 0) {
                    return "LIST" + SPACE + msgNo + CRLF;
                } else {
                    return "LIST" + CRLF;
                }
            }

            /**
             * If a msg num is specified then the the message details will be on
             * the first line for ex. +OK 3 4520
             * 
             * if no msgnum is specified then all the msg details are return in
             * a multiline format for ex. +OK 2 messages 1 456 2 46456 ..... n
             * 366
             */
            public boolean isMultiLineResponse() {
                return (msgNo < 0);
            }
        };
    }

    public static POP3Command getCOMMAND_RETR(final int msgNo) {
        return new POP3Command() {
            public String getCommand() {
                return "RETR" + SPACE + msgNo + CRLF;
            }

            public boolean isMultiLineResponse() {
                return true;
            }
        };
    }

    public static POP3Command getCOMMAND_DELE(final int msgNo) {
        return new POP3Command() {
            public String getCommand() {
                return "DELE" + SPACE + msgNo + CRLF;
            }

            public boolean isMultiLineResponse() {
                return false;
            }
        };
    }

    public static POP3Command getCOMMAND_REST(final int msgNo) {
        return new POP3Command() {
            public String getCommand() {
                return "REST" + SPACE + msgNo + CRLF;
            }

            public boolean isMultiLineResponse() {
                return false;
            }
        };
    }

    public static POP3Command getCOMMAND_TOP(final int msgNo, final int numLines) {
        return new POP3Command() {
            public String getCommand() {
                return "TOP" + SPACE + msgNo + SPACE + numLines + CRLF;
            }

            public boolean isMultiLineResponse() {
                return true;
            }
        };
    }

    public static POP3Command getCOMMAND_UIDL() {
        return getCOMMAND_UIDL(-1);
    }

    public static POP3Command getCOMMAND_UIDL(final int msgNo) {
        return new POP3Command() {
            public String getCommand() {
                if (msgNo > 0) {
                    return "UIDL" + SPACE + msgNo + CRLF;
                } else {
                    return "UIDL" + CRLF;
                }
            }

            public boolean isMultiLineResponse() {
                return true;
            }
        };
    }

}
