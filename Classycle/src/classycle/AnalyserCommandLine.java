/*
 * Copyright (c) 2003-2004, Franz-Josef Elmer, All rights reserved.
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
package classycle;

import classycle.util.AndStringPattern;
import classycle.util.NotStringPattern;
import classycle.util.StringPattern;
import classycle.util.StringPatternSequence;
import classycle.util.WildCardPattern;

/**
 * Process command line arguments and options for the main application
 * {@link Analyser}.
 * 
 * @author Franz-Josef Elmer
 */
public class AnalyserCommandLine 
{
  private static final String INCLUDING_CLASSES = "-includingClasses=";
  private static final String EXCLUDING_CLASSES = "-excludingClasses=";
  private static final String XML_FILE = "-xmlFile=";
  private static final String CSV_FILE = "-csvFile=";
  private static final String TITLE = "-title=";
  
  private boolean _valid = true;
  private StringPatternSequence _pattern = new AndStringPattern();
  private boolean _packagesOnly;
  private boolean _raw;
  private boolean _cycles;
  private boolean _strong;
  private String _title;
  private String _xmlFile;
  private String _csvFile;
  private String[] _classFiles;

  public AnalyserCommandLine(String[] args) 
  {
    int index = 0;
    for (;index < args.length && args[index].charAt(0) == '-'; index++) 
    {
      if (args[index].equals("-raw")) 
      {
        _raw = true;
      } else if (args[index].equals("-packagesOnly")) 
      {
        _packagesOnly = true;
      } else if (args[index].startsWith(INCLUDING_CLASSES)) 
      {
        String patterns = args[index].substring(INCLUDING_CLASSES.length());
        _pattern.appendPattern(WildCardPattern.createFromsPatterns(patterns, ","));
      } else if (args[index].startsWith(EXCLUDING_CLASSES)) 
      {
        String patterns = args[index].substring(EXCLUDING_CLASSES.length());
        StringPattern p = WildCardPattern.createFromsPatterns(patterns, ",");
        _pattern.appendPattern(new NotStringPattern(p));
      } else if (args[index].equals("-cycles")) 
      {
        _cycles = true;
      } else if (args[index].equals("-strong")) 
      {
        _strong = true;
      } else if (args[index].startsWith(TITLE)) 
      {
        _title = args[index].substring(TITLE.length());
        if (_title.length() == 0) 
        {
          _valid = false;
        }
      } else if (args[index].startsWith(XML_FILE)) 
      {
        _xmlFile = args[index].substring(XML_FILE.length());
        if (_xmlFile.length() == 0) 
        {
          _valid = false;
        }
      } else if (args[index].startsWith(CSV_FILE)) 
      {
        _csvFile = args[index].substring(CSV_FILE.length());
        if (_csvFile.length() == 0) 
        {
          _valid = false;
        }
      }
    }
    _classFiles = new String[args.length - index];
    System.arraycopy(args, index, _classFiles, 0, _classFiles.length);
    if (_title == null && _classFiles.length > 0) 
    {
      _title = _classFiles[0];
    }
    if (_classFiles.length == 0) 
    {
      _valid = false;
    }
  }
  
  /** Returns the usage of correct command line arguments and options. */
  public String getUsage() 
  {
    return "[-raw] [-packagesOnly] [-cycles|-strong] "
        + "[" + INCLUDING_CLASSES + "<pattern1>,<pattern2>,...] "
        + "[" + EXCLUDING_CLASSES + "<pattern1>,<pattern2>,...] "
        + "[" + XML_FILE + "<file>] [" + CSV_FILE + "<file>] "
        + "[" + TITLE + "<title>] "
        + "<class files, zip/jar/war/ear files, or folders>";
  }
  
  /** 
   * Returns <tt>true</tt> if the command line arguments and options are
   * valid.
   */
  public boolean isValid() 
  {
    return _valid;
  }
  
  /** Returns <tt>true</tt> if the option <tt>-cycles</tt> has been set. */
  public boolean isCycles() 
  {
    return _cycles;
  }

  /** Returns <tt>true</tt> if the option <tt>-package</tt> has been set. */
  public boolean isPackagesOnly() 
  {
    return _packagesOnly;
  }

  /** Returns <tt>true</tt> if the option <tt>-raw</tt> has been set. */
  public boolean isRaw() 
  {
    return _raw;
  }

  /** Returns <tt>true</tt> if the option <tt>-strong</tt> has been set. */
  public boolean isStrong() 
  {
    return _strong;
  }

  /**
   * Returns the name of the CSV file as defined by the option 
   * <tt>-csvFile</tt>.
   * @return <tt>null</tt> if undefined.
   */
  public String getCsvFile() 
  {
    return _csvFile;
  }

  /**
   * Returns the title by the option <tt>-title</tt>.
   * If undefined {@link #getClassFiles()}<tt>[0]</tt> will be used.
   * @return String
   */
  public String getTitle() 
  {
    return _title;
  }

  /**
   * Returns the name of the XML file as defined by the option 
   * <tt>-xmlFile</tt>.
   * @return <tt>null</tt> if undefined.
   */
  public String getXmlFile() 
  {
    return _xmlFile;
  }

  /**
   * Returns all class file descriptors (i.e., class files, directorys,
   * jar files, or zip files).
   */
  public String[] getClassFiles() 
  {
    return _classFiles;
  }
  
  /**
   * Returns the pattern fully qualified class names have to match. The
   * pattern is based on the options <tt>-includingClasses</tt> and
   * <tt>-excludingClasses</tt>. If <tt>-includingClasses</tt> is missing
   * every classes is included which is not excluded. 
   * If <tt>-excludingClasses</tt> is missing no class is excluded.
   */
  public StringPattern getPattern()
  {
    return _pattern;
  }
}
