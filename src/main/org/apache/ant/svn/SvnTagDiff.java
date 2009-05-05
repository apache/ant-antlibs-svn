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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.util.FileUtils;

/**
 * Examines the output of svn diff between two tags or a tag and trunk.
 *
 * <p>This task only works if you follow the best-practice structure of
 * <pre>
 * BASEURL
 *   |
 *   |
 *   -----&gt; trunk
 *   -----&gt; tags
 *            |
 *            |
 *            ----------&gt; fromTag
 *            ----------&gt; toTag
 *   -----&gt; branches
 *            |
 *            |
 *            ----------&gt; fromBranch
 *            ----------&gt; toBranch
 * </pre>
 *
 * <p>It produces an XML output representing the list of changes.</p>
 * 
 * <p>The task will compare any combination of branches and tags.  To
 * compare a newer branch to an older tag use the fromTag and toBranch
 * attributes.  To compare two tags use fromTag and toTag All
 * combinations work as expected.</p>
 * 
 * <p>You can specify the trunk for any of the four branch/tag
 * attibutes by using the special value "trunk" (without the quotes).</p>
 * 
 * <p>The older syntax using tag1 tag2 attributes is deprecated (but
 * it still works)</p>
 * 
 * <PRE>
 * &lt;!-- Root element --&gt;
 * &lt;!ELEMENT tagdiff ( paths? ) &gt;
 * &lt;!-- First tag --&gt;
 * &lt;!ATTLIST tagdiff fromTag NMTOKEN #IMPLIED &gt;
 * &lt;!-- Second tag --&gt;
 * &lt;!ATTLIST tagdiff toTag NMTOKEN #IMPLIED &gt;
 * &lt;!-- First branch --&gt;
 * &lt;!ATTLIST tagdiff fromBranch NMTOKEN #IMPLIED &gt;
 * &lt;!-- Second branch --&gt;
 * &lt;!ATTLIST tagdiff toBranch NMTOKEN #IMPLIED &gt;
 * &lt;!-- Subversion BaseURL --&gt;
 * &lt;!ATTLIST tagdiff svnurl NMTOKEN #IMPLIED &gt;
 *
 * &lt;!-- Path added, changed or removed --&gt;
 * &lt;!ELEMENT path ( name,action ) &gt;
 * &lt;!-- Name of the file --&gt;
 * &lt;!ELEMENT name ( #PCDATA ) &gt;
 * &lt;!ELEMENT action (added|modified|deleted)&gt;
 * </PRE>
 *
 * @ant.task name="svntagdiff"
 */
public class SvnTagDiff extends AbstractSvnTask {

    private static final String TRUNK = "trunk";
    private static final String TRUNK_SLASH = TRUNK + "/";

    /**
     * Used to create the temp file for svn log
     */
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    /**
     * The earliest revision from which diffs are to be included in the report.
     */
    private String fromTag;

    /**
     * The latest revision from which diffs are to be included in the report.
     */
    private String toTag;
    /**
     * The earliest revision from which diffs are to be included in the report.
     */
    private String fromBranch;
    /**
     * The latest revision from which diffs are to be included in the report.
     */
    private String toBranch;
    /**
     * The file in which to write the diff report.
     */
    private File mydestfile;

    /**
     * Base URL.
     */
    private String baseURL;
    /**
     * the name of the older branch or tag
     */
    private String fromCopy;
    /**
     * the name of the newer branch or tag
     */
    private String toCopy;
    /**
     * Base the name of the attribute in the output
     * e.g "fromBranch" or "fromTag" for the older copy
     */
    private String fromName;
    /**
     * Base the name of the attribute in the output
     * e.g "toBranch" or "toTag" for the newer copy
     */
    private String toName;
    /**
     * Base the name of the directory of branches or tags
     * e.g "branches/" or "tags/" for the older copy
     */
    private String fromDir;
    /**
     * Base the name of the directory of branches or tags
     * e.g "branches/" or "tags/" for the newer copy
     */
    private String toDir; 


    /**
     * Set the first tag.
     *
     * @param s the first tag.
     */
    public void setFromTag(String s) {
        fromTag = s;
    }

    /**
     * Set the second tag.
     *
     * @param s the second tag.
     */
    public void setToTag(String s) {
        toTag = s;
    }

    /**
     * Set the first tag.
     *
     * @param s the first tag.
     * @deprecated use fromTag
     */
    public void setTag1(String s) {
        setFromTag(s);
    }

    /**
     * Set the second tag.
     *
     * @param s the second tag.
     * @deprecated use toTag
     */
    public void setTag2(String s) {
        setToTag(s);
    }

    /**
     * Set the first branch.
     *
     * @param s the first branch.
     */
    public void setFromBranch(String s) {
        fromBranch = s;
    }

    /**
     * Set the second branch.
     *
     * @param s the second branch.
     */
    public void setToBranch(String s) {
        toBranch = s;
    }

    /**
     * Set the output file for the diff.
     *
     * @param f the output file for the diff.
     */
    public void setDestFile(File f) {
        mydestfile = f;
    }

    /**
     * Set the base URL from which to calculate tag URLs.
     *
     * @param u the base URL from which to calculate tag URLs.
     */
    public void setBaseURL(String u) {
        baseURL = u;
        if (!u.endsWith("/")) {
            baseURL += "/";
        }
    }

    /**
     * Execute task.
     *
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {
        // validate the input parameters
        validate();

        // sort out whats tags and whats branches
        this.fromCopy = fromTag !=null ? fromTag : fromBranch;
        this.toCopy = toTag != null ? toTag 
            : toBranch != null ? toBranch : TRUNK;
        this.fromName = fromTag != null ? "fromTag" : "fromBranch";
        this.toName = toTag != null ? "toTag" : "toBranch";
        this.fromDir = fromTag != null ? "tags/" : "branches/";
        this.toDir = toTag != null ? "tags/" : "branches/";

        // build the rdiff command
        setSubCommand("diff");
        addSubCommandArgument("--no-diff-deleted");
        addDiffArguments();

        File tmpFile = null;
        try {
            tmpFile = 
                FILE_UTILS.createTempFile("svntagdiff", ".log", null);
            tmpFile.deleteOnExit();
            setOutput(tmpFile);

            // run the svn command
            super.execute();

            // parse the diff
            SvnEntry.Path[] entries = SvnDiffHandler.parseDiff(tmpFile);

            // write the revision diff
            SvnDiffHandler.writeDiff(mydestfile, entries, "tagdiff",
                                     fromName, fromCopy, toName, 
                                     toCopy, 
                                     baseURL);
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
            }
        }
    }

    private void addDiffArguments(){
        if (fromCopy.equals(TRUNK) || fromCopy.equals(TRUNK_SLASH)) {
            addSubCommandArgument(baseURL + TRUNK_SLASH);
        } else {
            if (fromCopy.endsWith("/")) {
                addSubCommandArgument(baseURL + fromDir + fromCopy);
            } else {
                addSubCommandArgument(baseURL + fromDir + fromCopy + "/");
            }
        }
        if (toCopy.equals(TRUNK) || toCopy.equals(TRUNK_SLASH)) {
            addSubCommandArgument(baseURL + TRUNK_SLASH);
        } else {
            if (toCopy.endsWith("/")) {
                addSubCommandArgument(baseURL + toDir + toCopy);
            } else {
                addSubCommandArgument(baseURL + toDir + toCopy + "/");
            }
        }
    }

    /**
     * Validate the parameters specified for task.
     *
     * @exception BuildException if a parameter is not correctly set
     */
    private void validate() throws BuildException {
        if (null == mydestfile) {
            throw new BuildException("Destfile must be set.");
        }

        if (null == fromTag && null== fromBranch) {
            throw new BuildException("fromTag or fromBranch must be set.");
        }

        if (null == baseURL) {
            throw new BuildException("baseURL must be set.");
        }
    }
}
