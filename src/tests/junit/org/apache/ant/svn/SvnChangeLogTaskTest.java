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
        assertRev482074(log);
        assertRev371996(log);
    }

    public void testStart() throws IOException {
        String log = executeTargetAndReadLogFully("start");
        assertRev482074(log);
        assertNoRev371996(log);
    }

    public void testStartDate() throws IOException {
        String log = executeTargetAndReadLogFully("startDate");
        assertRev482074(log);
        assertNoRev371996(log);
    }

    public void testEnd() throws IOException {
        String log = executeTargetAndReadLogFully("end");
        assertNoRev482074(log);
        assertRev371996(log);
    }

    public void testEndDate() throws IOException {
        String log = executeTargetAndReadLogFully("endDate");
        assertNoRev482074(log);
        assertRev371996(log);
    }

    private String executeTargetAndReadLogFully(String target) 
        throws IOException {
        executeTarget(target);
        String tmpDir = getProject().getProperty("tmpdir");
        FileReader r = new FileReader(getProject()
                                      .resolveFile(tmpDir + "/log.xml"));
        try {
            return FileUtils.readFully(r);
        } finally {
            r.close();
        }
    }

    private static final void assertRev482074(String log) {
        int rev = log.indexOf("<revision>482074</revision>");
        Assert.assertTrue("Expected to find revision 482074, but log was "
                          + log, rev > -1);
        int entryBeforeRev = log.lastIndexOf("<entry>", rev);
        int entryAfterRev = log.indexOf("</entry>", rev);

        Assert.assertTrue(entryBeforeRev > -1);
        Assert.assertTrue(entryAfterRev > -1);

        Assert
            .assertTrue(log.lastIndexOf("<author><![CDATA[bodewig]]></author>",
                                        rev) > entryBeforeRev);
        Assert
            .assertTrue(log.indexOf("<name><![CDATA[ant/site/ant/sources/"
                                    + "antlibs/svn]]></name>", rev)
                        < entryAfterRev);
        Assert
            .assertTrue(log.indexOf("<action>modified</action>", rev) 
                        < entryAfterRev);
        Assert
            .assertTrue(log.indexOf("<message><![CDATA[linefeeds]]></message>",
                                    rev)
                        < entryAfterRev);
    }

    private static final void assertRev371996(String log) {
        int rev = log.indexOf("<revision>371996</revision>");
        Assert.assertTrue(rev > -1);
        int entryBeforeRev = log.lastIndexOf("<entry>", rev);
        int entryAfterRev = log.indexOf("</entry>", rev);

        Assert.assertTrue(entryBeforeRev > -1);
        Assert.assertTrue(entryAfterRev > -1);

        Assert
            .assertTrue(log.lastIndexOf("<![CDATA[bodewig]]>", rev) 
                        > entryBeforeRev);
        Assert
            .assertTrue(log.indexOf("<name><![CDATA[ant/site/ant/sources/"
                                    + "antlibs/svn]]></name>", rev)
                        < entryAfterRev);
        Assert
            .assertTrue(log.indexOf("<action>added</action>", rev) 
                        < entryAfterRev);
        Assert
            .assertTrue(log.indexOf("<message><![CDATA[Update Antlib status]]>"
                                    + "</message>", rev)
                        < entryAfterRev);
    }

    private static final void assertNoRev482074(String log) {
        int rev = log.indexOf("<revision>482074</revision>");
        Assert.assertEquals(-1, rev);
    }

    private static final void assertNoRev371996(String log) {
        int rev = log.indexOf("<revision>371996</revision>");
        Assert.assertEquals(-1, rev);
    }
}
