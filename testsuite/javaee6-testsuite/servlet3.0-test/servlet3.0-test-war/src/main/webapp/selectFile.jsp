<!--
    Licensed to the Apache Software Foundation (ASF) under one
    or more contributor license agreements.  See the NOTICE file
    distributed with this work for additional information
    regarding copyright ownership.  The ASF licenses this file
    to you under the Apache License, Version 2.0 (the
    "License"); you may not use this file except in compliance
    with the License.  You may obtain a copy of the License at
    
     http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing,
    software distributed under the License is distributed on an
    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
    KIND, either express or implied.  See the License for the
    specific language governing permissions and limitations
    under the License.
-->
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
    "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <form action="showServlet" method="post" enctype="multipart/form-data">
            <h2>This File Upload system demonstrates following new features.</h2>
            <ul>
                <li>Better file upload support in JavaEE6.</li>
                <li>Use <b>@WebFilter</b> to register a filter.</li>
                <li>Use <b>@WebListener</b> to register a listener.</li>
                <li>Use <b>@WebServlet</b> to register servlet.</li>
            </ul>
            <h2>Try to upload a file less then <font color="green"><b>10kb</b></font>,Otherwise,it will be filtered and you could not see the detail information.</h2>
            <hr>
            <input name="testFile" type="file"/>
            <br/>
            <input type="submit" value="Submit The File!" />
        </form>
    </body>
</html>
