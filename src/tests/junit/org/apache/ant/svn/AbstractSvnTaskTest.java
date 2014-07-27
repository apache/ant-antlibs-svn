/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.apache.ant.svn;

import java.io.File;

import org.apache.tools.ant.BuildFileTest;

/**
 */
public class AbstractSvnTaskTest extends BuildFileTest {

    public AbstractSvnTaskTest() {
        this( "AbstractSvnTaskTest" );
    }

    public AbstractSvnTaskTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("src/etc/testcases/abstractsvntask.xml");
    }

    public void tearDown() {
        executeTarget("cleanup");
    }

    public void testAbstractSvnTask() {
        executeTarget("all");
    }

    public void testRevisionAttribute() {
        String tmpDir = getProject().getProperty("tmpdir");
        File f = getProject().resolveFile(tmpDir + "/svn/index.xml");
        assertTrue("starting empty", !f.exists());

        // used to be
        // expectLogContaining("revision-attribute", "A  trunk/build.xml");
        // but the number of spaces between the status and the file depends
        // on the version of the command line client
        executeTarget("revision-attribute");
        String log = getLog();
        int buildFileIndex = Math.max(log.indexOf("svn/index.xml"),
                                      log.indexOf("svn\\index.xml"));
        assertTrue("expected message about index.xml, log was: " + log,
                   buildFileIndex > -1);
        for (int i = buildFileIndex - 1; i > -1; --i) {
            char c = log.charAt(i);
            if (c != ' ') {
                assertEquals('A', c);
                break;
            }
        }
        assertTrue("expected 'A' status for index.xml, log was:" +log,
                   buildFileIndex > -1);

        assertTrue("now it is there", f.exists());
    }
}
