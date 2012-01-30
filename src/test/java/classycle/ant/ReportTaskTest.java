/*
 * Created on 01.07.2004
 */
package classycle.ant;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.tools.ant.BuildException;

/**
 * @author  Franz-Josef Elmer
 */
public class ReportTaskTest extends ClassycleTaskTestCase
{
  public ReportTaskTest(String arg0)
  {
    super(arg0);
  }
  
  protected void setUp() throws Exception
  {
    createTempDir();
    configureProject("reportTaskTestBuild.xml");
  }
  
  public void testRaw() throws Exception
  {
    executeTarget("testRaw");
    checkNumberOfLines(23, "reportTaskTest.txt");
  }

  public void testMergeInnerClasses() throws Exception
  {
    executeTarget("testMergeInnerClasses");
    checkNumberOfLines(19, "reportTaskTest.txt");
  }
  
  public void testOnlyA() throws Exception
  {
    executeTarget("testOnlyA");
    checkNumberOfLines(14, "reportTaskTest.txt");
  }

  public void testIncludingA() throws Exception
  {
    executeTarget("testIncludingA");
    checkNumberOfLines(14, "reportTaskTest.txt");
  }

  public void testIncludingAExcludingB() throws Exception
  {
    executeTarget("testIncludingAExcludingB");
    checkNumberOfLines(9, "reportTaskTest.txt");
  }

  public void testIncludingAExcludingBp() throws Exception
  {
    executeTarget("testIncludingAExcludingBp");
    checkNumberOfLines(6, "reportTaskTest.txt");
  }

  public void testCSV() throws Exception
  {
    executeTarget("testCSV");
    checkNumberOfLines(7, "reportTaskTest.csv");
  }

  public void testXML() throws Exception
  {
    executeTarget("testXML");
    checkNumberOfLines(90, "reportTaskTest.xml");
  }

  public void testReflectionAll() throws Exception
  {
    executeTarget("testReflectionAll");
    checkNumberOfLines(24, "reportTaskTest.txt");
  }

  public void testReflectionRestricted() throws Exception
  {
    executeTarget("testReflectionRestricted");
    checkNumberOfLines(23, "reportTaskTest.txt");
  }

  public void testXMLPackagesOnly() throws Exception
  {
    executeTarget("testXMLPackagesOnly");
    checkNumberOfLines(34, "reportTaskTest.xml");
    String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
    checkLine("<classycle title='&lt;hel&amp;lo&gt;' date='" + date + "'>", 3, 
              "reportTaskTest.xml");
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
