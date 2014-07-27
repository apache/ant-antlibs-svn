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
public class SvnRevisionDiffTest extends BuildFileTest {

    public SvnRevisionDiffTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("src/etc/testcases/revisiondiff.xml");
    }

    public void tearDown() {
        executeTarget("cleanup");
    }

    public void testDiff() throws IOException {
        String log = executeTargetAndReadLogFully("diff");
        assertAttributesNoURL(log);
        assertModified(log);
    }

    public void testDiffUrl() throws IOException {
        String log = executeTargetAndReadLogFully("diff-using-url");
        assertAttributesWithURL(log);
        assertModified(log);
    }

    private String executeTargetAndReadLogFully(String target) 
        throws IOException {
        executeTarget(target);
        String tmpDir = getProject().getProperty("tmpdir");
        FileReader r = new FileReader(getProject()
                                      .resolveFile(tmpDir + "/diff.xml"));
        try {
            return FileUtils.readFully(r);
        } finally {
            r.close();
        }
    }

    private static final void assertAttributes(String log) {
        int start = log.indexOf("<revisiondiff");
        Assert.assertTrue(start > -1);
        int end = log.indexOf(">", start);
        Assert.assertTrue(end > -1);
        Assert.assertTrue(log.indexOf("start=\"371996\"", start) > -1);
        Assert.assertTrue(log.indexOf("start=\"371996\"", start) < end);
        Assert.assertTrue(log.indexOf("end=\"439435\"", start) > -1);
        Assert.assertTrue(log.indexOf("end=\"439435\"", start) < end);
    }

    private static final void assertAttributesNoURL(String log) {
        assertAttributes(log);
        Assert.assertEquals(-1, log.indexOf("svnurl="));
    }

    private static final void assertAttributesWithURL(String log) {
        assertAttributes(log);
        int start = log.indexOf("<revisiondiff");
        int end = log.indexOf(">", start);
        Assert.assertTrue(log.indexOf("svnurl=\"http://svn.apache.org/repos/"
                                      + "asf/ant/site/ant/sources/antlibs/svn/\"",
                                      start)
                          > -1);
        Assert.assertTrue(log.indexOf("svnurl=\"http://svn.apache.org/repos/"
                                      + "asf/ant/site/ant/sources/antlibs/svn/\"",
                                      start)
                          < end);
    }

    private static final void assertModified(String log) {
        int name = log.indexOf("<name><![CDATA[index.xml]]></name>");
        Assert.assertTrue(name > -1);

        int pathAfterName = log.indexOf("</path>", name);
        Assert.assertTrue(pathAfterName > -1);

        Assert.assertTrue(log.indexOf("<action>modified</action>", name) > -1);
        Assert.assertTrue(log.indexOf("<action>modified</action>", name) 
                          < pathAfterName);
    }

}
