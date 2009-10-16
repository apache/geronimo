/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * Loads text from a file an normalizes its EOL-style for platform-safe validation.
 */
def loadText = { filename ->
    def file = new File(basedir, "$filename")
    
    def tmp = File.createTempFile('validate', null, basedir)
    tmp.deleteOnExit()
    
    ant.copy(file: file, tofile: tmp)
    ant.fixcrlf(eol: 'unix', file: tmp)
    
    def text = tmp.text
    tmp.delete()
    
    return text
}

/**
 * Asserts that the text of both files are the same in a platform-safe fasion.
 */
def assertSame = { file1, file2 ->
    def expect = loadText(file1)
    def found = loadText(file2)
    
    assert expect == found
}

assertSame('src/test/resources/META-INF/geronimo-plugin.xml', 'target/resources/META-INF/geronimo-plugin.xml')
