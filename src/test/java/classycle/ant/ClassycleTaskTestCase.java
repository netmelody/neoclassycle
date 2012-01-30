/*
 * Created on 01.07.2004
 */
package classycle.ant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.tools.ant.BuildFileTest;

/**
 * @author  Franz-Josef Elmer
 */
public abstract class ClassycleTaskTestCase extends BuildFileTest
{
  protected static final String TMP_DIR = "temporaryTestDirectory";
  
  public ClassycleTaskTestCase(String arg0)
  {
    super(arg0);
  }
  
  protected void createTempDir()
  {
    new File(TMP_DIR).mkdir();
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
    FileReader reader = new FileReader(file);
    checkNumberOfLines(reader, expectedNumberOfLines);
  }
  
  protected void checkNumberOfLines(Reader reader, int expectedNumberOfLines) 
                 throws IOException
  {
    BufferedReader br = new BufferedReader(reader);
    int numberOfLines = 0;
    while (br.readLine() != null)
    {
      numberOfLines++;
    }
    assertEquals("Number of lines", expectedNumberOfLines, numberOfLines);
  }

  protected void checkLine(String expectedLine, int lineNumber, String fileName)
                 throws Exception
  {
    File file = new File(TMP_DIR, fileName);
    FileReader reader = new FileReader(file);
    checkLine(reader, expectedLine, lineNumber);
  }

  protected void checkLine(Reader reader, String expectedLine, int lineNumber) throws IOException
  {
    BufferedReader br = new BufferedReader(reader);
    String line = null;
    while ((line = br.readLine()) != null && --lineNumber > 0);
    assertEquals(expectedLine, line);
  }
  
  protected String readFile(String fileName) throws Exception
  {
    File file = new File(TMP_DIR, fileName);
    BufferedReader reader = new BufferedReader(new FileReader(file));
    StringBuilder builder = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null)
    {
      builder.append(line).append('\n');
    }
    return builder.toString();
    
  }

}
