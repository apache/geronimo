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

package javax.mail.search;

import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.MessagingException;

/**
 * @version $Rev$ $Date$
 */
public final class FlagTerm extends SearchTerm {
    protected Flags flags;
    protected boolean set;

    public FlagTerm(Flags flags, boolean set) {
        this.set = set;
        this.flags = flags;
    }

    public boolean equals(Object other) {
        return super.equals(other)
                && ((FlagTerm) other).flags.equals(flags)
                && ((FlagTerm) other).set == set;
    }

    public Flags getFlags() {
        return flags;
    }

    public boolean getTestSet() {
        return set;
    }

    public int hashCode() {
        return super.hashCode() + flags.hashCode() + (set ? 99 : 234);
    }

    public boolean match(Message message) {
        try {
            return message.getFlags().contains(flags) == set;
        } catch (MessagingException e) {
            return false;
        }
    }
}
