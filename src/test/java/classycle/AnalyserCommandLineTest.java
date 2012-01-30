/*
 * Created on 07.07.2004
 */
package classycle;

import classycle.util.StringPattern;
import junit.framework.TestCase;

/**
 * @author  Franz-Josef Elmer
 */
public class AnalyserCommandLineTest extends TestCase
{
  public void testNoOptionsNoClasses()
  {
    AnalyserCommandLine commandLine = new AnalyserCommandLine(new String[0]);
    assertFalse(commandLine.isValid());
    assertEquals(0, commandLine.getClassFiles().length);
  }

  public void testNoOptions()
  {
    AnalyserCommandLine commandLine 
        = new AnalyserCommandLine(new String[] {"bla"});
    checkNoOptions(commandLine);
    assertEquals(1, commandLine.getClassFiles().length);
    assertEquals("bla", commandLine.getClassFiles()[0]);
    assertEquals("bla", commandLine.getTitle());

    commandLine = new AnalyserCommandLine(new String[] {"bla", "foo.jar"});
    checkNoOptions(commandLine);
    assertEquals(2, commandLine.getClassFiles().length);
    assertEquals("bla", commandLine.getClassFiles()[0]);
    assertEquals("foo.jar", commandLine.getClassFiles()[1]);
    assertEquals("bla", commandLine.getTitle());
  }

  private void checkNoOptions(AnalyserCommandLine commandLine)
  {
    assertFalse(commandLine.isRaw());
    assertFalse(commandLine.isStrong());
    assertFalse(commandLine.isPackagesOnly());
    assertFalse(commandLine.isCycles());
    assertNull(commandLine.getCsvFile());
    assertNull(commandLine.getXmlFile());
    StringPattern pattern = commandLine.getPattern();
    assertTrue(pattern.matches("blabla"));
    assertTrue(commandLine.isValid());
  }
  
  public void testOptionsNoClasses()
  {
    AnalyserCommandLine commandLine 
        = new AnalyserCommandLine(new String[] {"-raw"});
    assertFalse(commandLine.isValid());
    assertEquals(0, commandLine.getClassFiles().length);

    commandLine = new AnalyserCommandLine(new String[] {"-raw", "-title=42"});
    assertFalse(commandLine.isValid());
    assertEquals(0, commandLine.getClassFiles().length);
  }
  
  public void testOptionTitle()
  {
    AnalyserCommandLine commandLine 
        = new AnalyserCommandLine(new String[] {"-title=42", "foo.jar"});
    assertTrue(commandLine.isValid());
    assertEquals(1, commandLine.getClassFiles().length);
    assertEquals("foo.jar", commandLine.getClassFiles()[0]);
    assertEquals("42", commandLine.getTitle());
  }

  public void testOptionIncludingClasses()
  {
    AnalyserCommandLine commandLine = new AnalyserCommandLine(
            new String[] {"-includingClasses=foo*,*la", "foo.jar"});
    assertTrue(commandLine.isValid());
    assertEquals(1, commandLine.getClassFiles().length);
    assertEquals("foo.jar", commandLine.getClassFiles()[0]);
    assertEquals("foo.jar", commandLine.getTitle());
    StringPattern pattern = commandLine.getPattern();
    checkPattern(pattern, true, 
                 new String[] {"foo", "foo.jar", "foola", "bla"});
    checkPattern(pattern, false, 
                 new String[] {"bla.jar", "fo"});
  }
  

  public void testOptionExcludingClasses()
  {
    AnalyserCommandLine commandLine = new AnalyserCommandLine(
            new String[] {"-excludingClasses=foo*,*la", "foo.jar"});
    assertTrue(commandLine.isValid());
    StringPattern pattern = commandLine.getPattern();
    checkPattern(pattern, false, 
                 new String[] {"foo", "foo.jar", "foola", "bla"});
    checkPattern(pattern, true, 
                 new String[] {"bla.jar", "fo"});
  }


  public void testOptionIncludingExcludingClasses()
  {
    AnalyserCommandLine commandLine = new AnalyserCommandLine(
            new String[] {"-includingClasses=foo*", "-excludingClasses=*la"});
    StringPattern pattern = commandLine.getPattern();
    checkPattern(pattern, true, 
                 new String[] {"foo", "foo.jar"});
    checkPattern(pattern, false, 
                 new String[] {"foola", "bla"});
  }

  private void checkPattern(StringPattern pattern, 
                            boolean expectedMatchResult, String[] examples)
  {
    for (int i = 0; i < examples.length; i++)
    {
      assertTrue(examples[i] + " match " + expectedMatchResult,
                 pattern.matches(examples[i]) == expectedMatchResult);
    }
  }
}
