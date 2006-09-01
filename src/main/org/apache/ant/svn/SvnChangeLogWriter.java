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
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import javax.xml.parsers.DocumentBuilder;

import org.apache.tools.ant.util.DOMElementWriter;
import org.apache.tools.ant.util.DOMUtils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Class used to generate an XML changelog.
 */
public class SvnChangeLogWriter {
    /** output format for dates written to xml file */
    private static final SimpleDateFormat OUTPUT_DATE
        = new SimpleDateFormat("yyyy-MM-dd");
    /** output format for times written to xml file */
    private static final SimpleDateFormat OUTPUT_TIME
        = new SimpleDateFormat("HH:mm");
    /** stateless helper for writing the XML document */
    private static final DOMElementWriter DOM_WRITER = new DOMElementWriter();

    /**
     * Print out the specified entries.
     *
     * @param output writer to which to send output.
     * @param entries the entries to be written.
     */
    public void printChangeLog(final PrintWriter output,
                               final SvnEntry[] entries) throws IOException {
        output.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        Document doc = DOMUtils.newDocument();
        Element root = doc.createElement("changelog");
        DOM_WRITER.openElement(root, output, 0, "\t");
        output.println();
        for (int i = 0; i < entries.length; i++) {
            final SvnEntry entry = entries[i];

            printEntry(output, entry, root);
        }
        DOM_WRITER.closeElement(root, output, 0, "\t", entries.length > 0);
        output.flush();
        output.close();
    }


    /**
     * Print out an individual entry in changelog.
     *
     * @param entry the entry to print
     * @param output writer to which to send output.
     */
    private void printEntry(final PrintWriter output, final SvnEntry entry,
                            final Element element) throws IOException {
        Document doc = element.getOwnerDocument();

        Element ent = doc.createElement("entry");
        DOMUtils.appendTextElement(ent, "date",
                                   OUTPUT_DATE.format(entry.getDate()));
        DOMUtils.appendTextElement(ent, "time",
                                   OUTPUT_TIME.format(entry.getDate()));
        DOMUtils.appendCDATAElement(ent, "author", entry.getAuthor());
        DOMUtils.appendTextElement(ent, "revision", entry.getRevision());

        SvnEntry.Path[] paths = entry.getPaths();
        for (int i = 0; i < paths.length; i++) {
            Element path = DOMUtils.createChildElement(ent, "path");
            DOMUtils.appendCDATAElement(path, "name", paths[i].getName());
            DOMUtils.appendTextElement(path, "action",
                                       paths[i].getActionDescription());
        }
        DOMUtils.appendCDATAElement(ent, "message", entry.getMessage());
        DOM_WRITER.write(ent, output, 1, "\t");
    }
}

