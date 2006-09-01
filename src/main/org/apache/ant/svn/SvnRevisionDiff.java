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
 * Examines the output of svn diff between two revisions.
 *
 * It produces an XML output representing the list of changes.
 * <PRE>
 * &lt;!-- Root element --&gt;
 * &lt;!ELEMENT revisiondiff ( paths? ) &gt;
 * &lt;!-- Start revision of the report --&gt;
 * &lt;!ATTLIST revisiondiff start NMTOKEN #IMPLIED &gt;
 * &lt;!-- End revision of the report --&gt;
 * &lt;!ATTLIST revisiondiff end NMTOKEN #IMPLIED &gt;
 * &lt;!-- Subversion URL if known  --&gt;
 * &lt;!ATTLIST revisiondiff svnurl NMTOKEN #IMPLIED &gt;
 *
 * &lt;!-- Path added, changed or removed --&gt;
 * &lt;!ELEMENT path ( name,action ) &gt;
 * &lt;!-- Name of the file --&gt;
 * &lt;!ELEMENT name ( #PCDATA ) &gt;
 * &lt;!ELEMENT action (added|modified|deleted)&gt;
 * </PRE>
 *
 * @ant.task name="svnrevisiondiff"
 */
public class SvnRevisionDiff extends AbstractSvnTask {

    /**
     * Used to create the temp file for svn log
     */
    private static final FileUtils FILE_UTILS = FileUtils.getFileUtils();

    /**
     * The earliest revision from which diffs are to be included in the report.
     */
    private String mystartRevision;

    /**
     * The latest revision from which diffs are to be included in the report.
     */
    private String myendRevision;

    /**
     * The file in which to write the diff report.
     */
    private File mydestfile;

    /**
     * Set the start revision.
     *
     * @param s the start revision.
     */
    public void setStart(String s) {
        mystartRevision = s;
    }

    /**
     * Set the end revision.
     *
     * @param s the end revision.
     */
    public void setEnd(String s) {
        myendRevision = s;
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
     * Execute task.
     *
     * @exception BuildException if an error occurs
     */
    public void execute() throws BuildException {
        // validate the input parameters
        validate();

        // build the rdiff command
        setSubCommand("diff");
        setRevision(mystartRevision + ":" + myendRevision);
        addSubCommandArgument("--no-diff-deleted");

        File tmpFile = null;
        try {
            tmpFile = 
                FILE_UTILS.createTempFile("svnrevisiondiff", ".log", null);
            tmpFile.deleteOnExit();
            setOutput(tmpFile);

            // run the svn command
            super.execute();

            // parse the diff
            SvnEntry.Path[] entries = SvnDiffHandler.parseDiff(tmpFile);

            // write the revision diff
            SvnDiffHandler.writeDiff(mydestfile, entries, "revisiondiff",
                                     "start", mystartRevision,
                                     "end", myendRevision, getSvnURL());
        } finally {
            if (tmpFile != null) {
                tmpFile.delete();
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

        if (null == mystartRevision) {
            throw new BuildException("Start revision or start date must be set.");
        }

        if (null == myendRevision) {
            throw new BuildException("End revision or end date must be set.");
        }
    }
}
