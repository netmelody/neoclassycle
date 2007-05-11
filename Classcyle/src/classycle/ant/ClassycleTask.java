/*
 * Copyright (c) 2003-2007, Franz-Josef Elmer, All rights reserved.
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
package classycle.ant;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;

import classycle.util.AndStringPattern;
import classycle.util.NotStringPattern;
import classycle.util.StringPattern;
import classycle.util.TrueStringPattern;
import classycle.util.WildCardPattern;

/**
 * Common attributes of all Classyle Ant tasks.
 * 
 * @author  Franz-Josef Elmer
 */
public abstract class ClassycleTask extends Task
{
  private boolean _mergeInnerClasses;
  private StringPattern _includingClasses = new TrueStringPattern();
  private StringPattern _excludingClasses = new TrueStringPattern();
  private StringPattern _reflectionPattern;
  private LinkedList _fileSets = new LinkedList();

  public void setMergeInnerClasses(boolean mergeInnerClasses)
  {
    _mergeInnerClasses = mergeInnerClasses;
  }
  
  public void setIncludingClasses(String patternList)
  {
    _includingClasses = WildCardPattern.createFromsPatterns(patternList, ", ");
  }

  public void setExcludingClasses(String patternList)
  {
    _excludingClasses = new NotStringPattern(
                        WildCardPattern.createFromsPatterns(patternList, ", "));
  }
  
  public void setReflectionPattern(String patternList)
  {
    if ("".equals(patternList))
    {
      _reflectionPattern = new TrueStringPattern();
    } else 
    {
      _reflectionPattern 
          = WildCardPattern.createFromsPatterns(patternList, ", ");
    }
  }

  public void addConfiguredFileset(FileSet set)
  {
    _fileSets.add(set);
  }
  
  public void execute() throws BuildException
  {
    super.execute();

    if (_fileSets.size() == 0)
    {
      throw new BuildException("at least one file set is required");
    }
  }

  protected String[] getClassFileNames()
  {
    ArrayList fileNames = new ArrayList();
    String fileSeparator = System.getProperty("file.separator");
    for (Iterator i = _fileSets.iterator(); i.hasNext();)
    {
      FileSet set = (FileSet) i.next();
      DirectoryScanner scanner = set.getDirectoryScanner(getProject());
      String path = scanner.getBasedir().getAbsolutePath();
      String[] localFiles = scanner.getIncludedFiles();
      for (int j = 0; j < localFiles.length; j++)
      {
        fileNames.add(path + fileSeparator + localFiles[j]);
      }
    }
    String[] classFiles = new String[fileNames.size()];
    return (String[]) fileNames.toArray(classFiles);
  }

  protected StringPattern getPattern()
  {
    AndStringPattern pattern = new AndStringPattern();
    pattern.appendPattern(_includingClasses);
    pattern.appendPattern(_excludingClasses);
    return pattern;
  }
  
  protected StringPattern getReflectionPattern()
  {
    return _reflectionPattern;
  }

  protected boolean isMergeInnerClasses()
  {
    return _mergeInnerClasses;
  }
  
 
}
