/*
 * Copyright  2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.apache.tools.ant.taskdefs.svn;

import java.io.IOException;
import java.io.FileReader;

import org.apache.tools.ant.BuildFileTest;
import org.apache.tools.ant.util.FileUtils;

import junit.framework.Assert;

/**
 */
public class SvnChangeLogTaskTest extends BuildFileTest {

    public SvnChangeLogTaskTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("src/etc/testcases/changelog.xml");
    }

    public void tearDown() {
        executeTarget("cleanup");
    }

    public void testLog() throws IOException {
        String log = executeTargetAndReadLogFully("log");
        assertRev161885(log);
        assertRev161469(log);
    }

    public void testStart() throws IOException {
        String log = executeTargetAndReadLogFully("start");
        assertRev161885(log);
        assertNoRev161469(log);
    }

    public void testStartDate() throws IOException {
        String log = executeTargetAndReadLogFully("startDate");
        assertRev161885(log);
        assertNoRev161469(log);
    }

    public void testEnd() throws IOException {
        String log = executeTargetAndReadLogFully("end");
        assertNoRev161885(log);
        assertRev161469(log);
    }

    public void testEndDate() throws IOException {
        String log = executeTargetAndReadLogFully("endDate");
        assertNoRev161885(log);
        assertRev161469(log);
    }

    private String executeTargetAndReadLogFully(String target) 
        throws IOException {
        executeTarget(target);
        FileReader r = new FileReader(getProject()
                                      .resolveFile("tmpdir/log.xml"));
        try {
            return FileUtils.readFully(r);
        } finally {
            r.close();
        }
    }

    private static final void assertRev161885(String log) {
        int rev = log.indexOf("<revision>161885</revision>");
        Assert.assertTrue(rev > -1);
        int entryBeforeRev = log.lastIndexOf("<entry>", rev);
        int entryAfterRev = log.indexOf("</entry>", rev);

        Assert.assertTrue(entryBeforeRev > -1);
        Assert.assertTrue(entryAfterRev > -1);

        Assert
            .assertTrue(log.lastIndexOf("<author><![CDATA[bodewig]]></author>",
                                        rev) > entryBeforeRev);
        Assert
            .assertTrue(log.indexOf("<name><![CDATA[/ant/sandbox/antlibs/"
                                    + "antunit/trunk]]></name>", rev)
                        < entryAfterRev);
        Assert
            .assertTrue(log.indexOf("<action>modified</action>", rev) 
                        < entryAfterRev);
        Assert
            .assertTrue(log.indexOf("<message><![CDATA[Make sandboxes build"
                                    + "]]></message>", rev)
                        < entryAfterRev);
    }

    private static final void assertRev161469(String log) {
        int rev = log.indexOf("<revision>161469</revision>");
        Assert.assertTrue(rev > -1);
        int entryBeforeRev = log.lastIndexOf("<entry>", rev);
        int entryAfterRev = log.indexOf("</entry>", rev);

        Assert.assertTrue(entryBeforeRev > -1);
        Assert.assertTrue(entryAfterRev > -1);

        Assert
            .assertTrue(log.lastIndexOf("<![CDATA[bodewig]]>", rev) 
                        > entryBeforeRev);
        Assert
            .assertTrue(log.indexOf("<name><![CDATA[/ant/sandbox/antlibs/"
                                    + "antunit]]></name>", rev)
                        < entryAfterRev);
        Assert
            .assertTrue(log.indexOf("<action>added</action>", rev) 
                        < entryAfterRev);
        Assert
            .assertTrue(log.indexOf("<message><![CDATA[Import sandbox antlibs"
                                    + "]]></message>", rev)
                        < entryAfterRev);
    }

    private static final void assertNoRev161885(String log) {
        int rev = log.indexOf("<revision>161885</revision>");
        Assert.assertEquals(-1, rev);
    }

    private static final void assertNoRev161469(String log) {
        int rev = log.indexOf("<revision>161469</revision>");
        Assert.assertEquals(-1, rev);
    }
}
