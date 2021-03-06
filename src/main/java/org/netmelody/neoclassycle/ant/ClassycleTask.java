/*
 * Copyright (c) 2012 Tom Denley
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
 * Copyright (c) 2003-2008, Franz-Josef Elmer, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.netmelody.neoclassycle.ant;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import org.apache.tools.ant.types.ResourceCollection;
import org.netmelody.neoclassycle.util.AndStringPattern;
import org.netmelody.neoclassycle.util.NotStringPattern;
import org.netmelody.neoclassycle.util.StringPattern;
import org.netmelody.neoclassycle.util.TrueStringPattern;
import org.netmelody.neoclassycle.util.WildCardPattern;

/**
 * Common attributes of all Classyle Ant tasks.
 *
 * @author Franz-Josef Elmer
 */
public abstract class ClassycleTask extends Task {
    private boolean _mergeInnerClasses;
    private StringPattern _includingClasses = new TrueStringPattern();
    private StringPattern _excludingClasses = new TrueStringPattern();
    private StringPattern _reflectionPattern;
    private final LinkedList<ResourceCollection> _resources = new LinkedList<ResourceCollection>();
    protected File _reportFile;

    public void setMergeInnerClasses(final boolean mergeInnerClasses) {
        _mergeInnerClasses = mergeInnerClasses;
    }

    public void setIncludingClasses(final String patternList) {
        _includingClasses = WildCardPattern.createFromsPatterns(patternList, ", ");
    }

    public void setExcludingClasses(final String patternList) {
        _excludingClasses = new NotStringPattern(WildCardPattern.createFromsPatterns(patternList, ", "));
    }

    public void setReflectionPattern(final String patternList) {
        if ("".equals(patternList)) {
            _reflectionPattern = new TrueStringPattern();
        }
        else {
            _reflectionPattern = WildCardPattern.createFromsPatterns(patternList, ", ");
        }
    }

    public void add(ResourceCollection rc) {
        _resources.add(rc);
    }

    @Override
    public void execute() throws BuildException {
        super.execute();

        if (_resources.size() == 0) {
            throw new BuildException("at least one file set is required");
        }
    }

    protected String[] getClassFileNames() {
        final ArrayList<String> fileNames = new ArrayList<String>();
        final String fileSeparator = System.getProperty("file.separator");
        for (final ResourceCollection resourceCollection : _resources) {
            if (!(resourceCollection instanceof FileSet)) {
                throw new BuildException("resource collection unsupported " + resourceCollection);
            }
            final FileSet set = (FileSet)resourceCollection;
            final DirectoryScanner scanner = set.getDirectoryScanner(getProject());
            final String path = scanner.getBasedir().getAbsolutePath();
            final String[] localFiles = scanner.getIncludedFiles();
            for (final String localFile : localFiles) {
                fileNames.add(path + fileSeparator + localFile);
            }
        }
        final String[] classFiles = new String[fileNames.size()];
        return fileNames.toArray(classFiles);
    }

    protected StringPattern getPattern() {
        final AndStringPattern pattern = new AndStringPattern();
        pattern.appendPattern(_includingClasses);
        pattern.appendPattern(_excludingClasses);
        return pattern;
    }

    protected StringPattern getReflectionPattern() {
        return _reflectionPattern;
    }

    protected boolean isMergeInnerClasses() {
        return _mergeInnerClasses;
    }

    public void setReportFile(final File xmlFile) {
        _reportFile = xmlFile;
    }

}
