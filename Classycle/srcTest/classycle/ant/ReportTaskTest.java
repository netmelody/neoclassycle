/*
 * Created on 01.07.2004
 */
package classycle.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildFileTest;

/**
 * @author  Franz-Josef Elmer
 */
public class ReportTaskTest extends BuildFileTest
{
  private static final String TMP_DIR = "temporaryTestDirectory";
  
  public ReportTaskTest(String arg0)
  {
    super(arg0);
  }
  
  protected void setUp() throws Exception
  {
    new File(TMP_DIR).mkdir();
    configureProject("reportTaskTestBuild.xml");
  }
  
  protected void tearDown() throws Exception
  {
    File dir = new File(TMP_DIR);
    String[] files = dir.list();
    for (int i = 0; i < files.length; i++)
    {
      File file = new File(TMP_DIR, files[i]);
      assertTrue("Couldn't delete " + file, file.delete());
    }
    dir.delete();
  }
  
  protected void checkNumberOfLines(int expectedNumberOfLines, String fileName)
                 throws Exception
  {
    File file = new File(TMP_DIR, fileName);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    int numberOfLines = 0;
    while (reader.readLine() != null)
    {
      numberOfLines++;
    }
    assertEquals("Number of lines in file " + fileName, 
                 expectedNumberOfLines, numberOfLines);
  }
  
  protected void checkLine(String expectedLine, int lineNumber, String fileName)
                 throws Exception
  {
    File file = new File(TMP_DIR, fileName);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    String line = null;
    while ((line = reader.readLine()) != null && --lineNumber > 0);
    assertEquals(expectedLine, line);
  }

  public void testRaw() throws Exception
  {
    executeTarget("testRaw");
    checkNumberOfLines(50, "reportTaskTest.txt");
  }

  public void testCSV() throws Exception
  {
    executeTarget("testCSV");
    checkNumberOfLines(4, "reportTaskTest.csv");
  }

  public void testXML() throws Exception
  {
    executeTarget("testXML");
    checkNumberOfLines(78, "reportTaskTest.xml");
  }

  public void testReflectionAll() throws Exception
  {
    executeTarget("testReflectionAll");
    checkNumberOfLines(88, "reportTaskTest.xml");
  }

  public void testReflectionRestricted() throws Exception
  {
    executeTarget("testReflectionRestricted");
    checkNumberOfLines(82, "reportTaskTest.xml");
  }

  public void testXMLPackagesOnly() throws Exception
  {
    executeTarget("testXMLPackagesOnly");
    checkNumberOfLines(19, "reportTaskTest.xml");
    checkLine("<classycle title='&lt;hel&amp;lo&gt;'>", 3, 
              "reportTaskTest.xml");
  }

  public void testIncludingClasses() throws Exception
  {
    executeTarget("testIncludingClasses");
    checkNumberOfLines(4, "reportTaskTest.csv");
  }

  public void testExcludingClasses() throws Exception
  {
    executeTarget("testExcludingClasses");
    checkNumberOfLines(2, "reportTaskTest.csv");
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
