/*
 * Created on 01.07.2004
 */
package classycle.ant;

import java.io.BufferedReader;
import java.io.FileReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;

/**
 * @author  Franz-Josef Elmer
 */
public class ReportTaskTest extends BuildFileTest
{
  public ReportTaskTest(String arg0)
  {
    super(arg0);
  }
  
  protected void setUp() throws Exception
  {
    configureProject("reportTaskTestBuild.xml");
  }
  
  protected void checkNumberOfLines(int expectedNumberOfLines, String file)
                 throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    int numberOfLines = 0;
    while (reader.readLine() != null)
    {
      numberOfLines++;
    }
    assertEquals("Number of lines in file " + file, 
                 expectedNumberOfLines, numberOfLines);
  }
  
  protected void checkLine(String expectedLine, int lineNumber, String file)
  throws Exception
  {
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    while ((line = reader.readLine()) != null && --lineNumber > 0);
    assertEquals(expectedLine, line);
  }

  public void testRaw() throws Exception
  {
    executeTarget("testRaw");
    checkNumberOfLines(46, "reportTaskTest.txt");
  }

  public void testCSV() throws Exception
  {
    executeTarget("testCSV");
    checkNumberOfLines(70, "reportTaskTest.csv");
  }

  public void testXML() throws Exception
  {
    executeTarget("testXML");
    checkNumberOfLines(1019, "reportTaskTest.xml");
  }

  public void testXMLPackagesOnly() throws Exception
  {
    executeTarget("testXMLPackagesOnly");
    checkNumberOfLines(84, "reportTaskTest.xml");
    checkLine("<classycle title='&lt;hel&amp;lo&gt;'>", 3, 
              "reportTaskTest.xml");
  }

  public void testIncludingClasses() throws Exception
  {
    executeTarget("testIncludingClasses");
    checkNumberOfLines(3, "reportTaskTest.csv");
  }

  public void testExcludingClasses() throws Exception
  {
    executeTarget("testExcludingClasses");
    checkNumberOfLines(68, "reportTaskTest.csv");
  }

  public void testInExcludingClasses() throws Exception
  {
    executeTarget("testInExcludingClasses");
    checkNumberOfLines(2, "reportTaskTest.csv");
  }

  public void testInvalidReportType() throws Exception
  {
    try
    {
      executeTarget("testInvalidReportType");
      fail("BuildException expected");
    } catch (BuildException e)
    {
      assertTrue(e.getMessage().indexOf("foo") >= 0);
    }
  }

  public void testMissingReportFile() throws Exception
  {
    try
    {
      executeTarget("testMissingReportFile");
      fail("BuildException expected");
    } catch (BuildException e)
    {
      assertTrue(e.getMessage().indexOf("'reportFile'") >= 0);
    }
  }

  public void testMissingFileSet() throws Exception
  {
    try
    {
      executeTarget("testMissingFileSet");
      fail("BuildException expected");
    } catch (BuildException e)
    {
      assertTrue(e.getMessage().indexOf("file set") >= 0);
    }
  }
}
