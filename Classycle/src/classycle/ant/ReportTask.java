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
package classycle.ant;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashSet;

import org.apache.tools.ant.BuildException;

import classycle.Analyser;

/**
 * Ant Task for creating a Classycle report, either raw, CSV, or XML.
 * <p>
 * <table border="1" cellpadding="5" cellspacing="0">
 * <tr><th>Attribute</th><th>Description</th><th>Required</th></tr>
 * <tr><td>reportFile</td>
 *     <td>Path of the report file relative to the base directory.</td>
 *     <td>Yes</td>
 * <tr><td>reportType</td>
 *     <td>Type of the report. Has to be either <tt>raw</tt>, <tt>csv</tt>,
 *         or <tt>xml</tt>.</td>
 *     <td>No. Default is <tt>xml</tt></td>
 * </tr> 
 * <tr><td>title</td>
 *     <td>Title of the XML report.</td>
 *     <td>No. Default is the first file in the file set.</td>
 * </tr> 
 * <tr><td>packagesOnly</td>
 *     <td>If <tt>true</tt> only packages and their dependencies
 *         are analysed and reported (only in XML report).</td>
 *     <td>No. Default is <tt>false</tt>.</td>
 * </tr> 
 * <tr><td>includingClasses</td>
 *     <td>Comma or space separated list of wild-card patterns of
 *         fully-qualified class name which are included in the analysis.
 *         Only '*' are recognized as wild-card character.
 *     </td>
 *     <td>No. By default all classes defined in the file set are included.
 *     </td>
 * </tr> 
 * <tr><td>excludingClasses</td>
 *     <td>Comma or space separated list of wild-card patterns of
 *         fully-qualified class name which are excluded from the analysis.
 *         Only '*' are recognized as wild-card character.
 *     </td>
 *     <td>No. By default no class defined in the file set is excluded.
 *     </td>
 * </tr> 
 * </table>
 *  
 * @author Boris Gruschko
 * @author Franz-Josef Elmer
 */
public class ReportTask extends ClassycleTask
{
  public static final String TYPE_RAW = "raw",
                             TYPE_CSV = "csv",
                             TYPE_XML = "xml";
  private static final HashSet TYPES = new HashSet();
  
  static 
  {
    TYPES.add(TYPE_RAW);
    TYPES.add(TYPE_CSV);
    TYPES.add(TYPE_XML);
  }
  
  private boolean _packagesOnly;
  private String _reportFile;
  private String _reportType = TYPE_XML;
  private String _title;
  
  public void setPackagesOnly(boolean packagesOnly)
  {
    _packagesOnly = packagesOnly;
  }

  public void setReportFile(String xmlFile)
  {
    _reportFile = xmlFile;
  }

  public void setReportType(String csvFile)
  {
    _reportType = csvFile;
  }

  public void setTitle(String title)
  {
    _title = title;
  }
  
  public void execute() throws BuildException
  {
    super.execute();

    if (!TYPES.contains(_reportType))
    {
      throw new BuildException("invalid attribute 'reportType': " 
                               + _reportType);
    }
    if (_reportFile == null)
    {
      throw new BuildException("missing attribute 'reportFile'.");
    }
    
    String[] classFiles = getClassFileNames();
    if (classFiles.length > 0 && _title == null)
    {
      _title = classFiles[0];
    }
    Analyser analyser = new Analyser(classFiles, getPattern());
    try
    {
      analyser.readAndAnalyse(_packagesOnly);
      File f = new File(getOwningTarget().getProject().getBaseDir(), 
                        _reportFile);
      PrintWriter writer = new PrintWriter(new FileWriter(f));
      if (_reportType.equals(TYPE_XML))
      {
        analyser.printXML(_title, _packagesOnly, writer);
      } else if (_reportType.equals(TYPE_CSV))
      {
        analyser.printCSV(writer);
      } else if (_reportType.equals(TYPE_RAW))
      {
        analyser.printRaw(writer);
      }
    } catch (Exception e)
    {
      throw new BuildException(e);
    }
  }
}