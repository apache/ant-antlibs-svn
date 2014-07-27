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
public class SvnTagDiffTest extends BuildFileTest {

    public SvnTagDiffTest(String name) {
        super(name);
    }

    public void setUp() {
        configureProject("src/etc/testcases/tagdiff.xml");
    }

    public void tearDown() {
        executeTarget("cleanup");
    }

    public void testDiffWithTwoTags() throws IOException {
        String log = executeTargetAndReadLogFully("diff-with-two-tags");
        assertAttributes(log, "10", "10_BETA1");
        assertAdded1(log);
    }

    public void testDiffWithExplicitTrunk() throws IOException {
        String log = executeTargetAndReadLogFully("diff-with-explicit-trunk");
        assertDiffWithTrunk(log, "toTag");
    }

    public void testDiffWithImplicitTrunk() throws IOException {
        String log = executeTargetAndReadLogFully("diff-with-implicit-trunk");
        assertDiffWithTrunk(log, "toBranch");
    }

    private static void assertDiffWithTrunk(String log, String tag2Name) {
        assertAttributes(log, "10_BETA1", tag2Name, "trunk");
        assertAdded(log);
        assertModified(log);
        assertDeleted(log);
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

    private static final void assertAttributes(String log, String tag1,
                                               String tag2) {
        assertAttributes(log, tag1, "toTag", tag2);
    }

    private static final void assertAttributes(String log, String tag1,
                                               String tag2Name, String tag2) {
        int start = log.indexOf("<tagdiff");
        Assert.assertTrue(start > -1);
        int end = log.indexOf(">", start);
        Assert.assertTrue(end > -1);
        Assert.assertTrue(log.indexOf("fromTag=\"" + tag1 + "\"", start) > -1);
        Assert.assertTrue(log.indexOf("fromTag=\"" + tag1 + "\"", start) < end);
        Assert.assertTrue(log.indexOf(tag2Name + "=\"" + tag2 + "\"",
                                      start) > -1);
        Assert.assertTrue(log.indexOf(tag2Name + "=\"" + tag2 + "\"",
                                      start) < end);
        Assert.assertTrue(log.indexOf("svnurl=\"http://svn.apache.org/",
                                      start) > -1);
        Assert.assertTrue(log.indexOf("svnurl=\"http://svn.apache.org/",
                                      start) < end);
    }

    private static final void assertAdded(String log) {
        int name = log.indexOf("<![CDATA[changes.xml]]>");
        Assert.assertTrue(name > -1);

        int pathAfterName = log.indexOf("</path>", name);
        Assert.assertTrue(pathAfterName > -1);

        Assert.assertTrue(log.indexOf("<action>added</action>", name) > -1);
        Assert.assertTrue(log.indexOf("<action>added</action>", name) 
                          < pathAfterName);
    }

    private static final void assertModified(String log) {
        int name = log.indexOf("<name><![CDATA[NOTICE]]></name>");
        Assert.assertTrue(name > -1);

        int pathAfterName = log.indexOf("</path>", name);
        Assert.assertTrue(pathAfterName > -1);

        Assert.assertTrue(log.indexOf("<action>modified</action>", name) > -1);
        Assert.assertTrue(log.indexOf("<action>modified</action>", name) 
                          < pathAfterName);
    }

    private static final void assertDeleted(String log) {
        // Do nothing
    }

    private static final void assertAdded1(String log) {
        // Do nothing
    }

}
