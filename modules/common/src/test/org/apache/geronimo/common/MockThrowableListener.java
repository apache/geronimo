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

package org.apache.geronimo.common;

import java.util.ArrayList;

/**
 * Mock object for testing the {@link ThrowableHandler} class.
 *
 * @version $Rev$ $Date$
 */

public class MockThrowableListener implements ThrowableListener {

    private ArrayList unknownList;
    private ArrayList errorList;
    private ArrayList warningList;

    public MockThrowableListener() {
        unknownList = new ArrayList();
        errorList = new ArrayList();
        warningList = new ArrayList();
    }

    public ArrayList getUnknownList() {
        return unknownList;
    }

    public ArrayList getErrorList() {
        return errorList;
    }

    public ArrayList getWarningList() {
        return warningList;
    }

    public void onThrowable(int type, Throwable t) {
        switch (type) {
        case ThrowableHandler.Type.UNKNOWN:
            unknownList.add(t);
            break;
        case ThrowableHandler.Type.WARNING:
            warningList.add(t);
            break;
        case ThrowableHandler.Type.ERROR:
            errorList.add(t);
        }
    }
}
