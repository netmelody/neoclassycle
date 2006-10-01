/*
 * Created on 18.06.2004
 */
package classycle.util;

import junit.framework.TestCase;

/**
 * @author  Franz-Josef Elmer
 */
public class WildCardPatternTest extends TestCase
{

  /**
   * Constructor for WildCardPatternTest.
   * @param arg0
   */
  public WildCardPatternTest(String arg0)
  {
    super(arg0);
  }
  
  public void testOnNull()
  {
    matchNot("", null);
  }
  
  public void testEmptyPattern()
  {
    match("", "");
    matchNot("", "a");
  }
  
  public void testConstantPattern()
  {
    match("a", "a");
    matchNot("a", "");
    matchNot("a", "affe");
    matchNot("a", "Kaffee");
    matchNot("affe", "a");
  }
  
  public void testStar()
  {
    match("*", "");
    match("*", "a");
    match("*", "affe");
    match("*", "*");
  }
  
  public void testStartConstant()
  {
    match("*t", "t");
    match("*t", "Test");
    match("*t", "test");
    matchNot("*t", "ti");
    matchNot("*t", "iti");
    match("*te", "te");
    match("*te", "ate");
    match("*te", "ate");
    match("*te", "ateate");
    matchNot("*te", "a");
    matchNot("*te", "atea");
  }
  
  public void testConstantStar()
  {
    match("t*", "t");
    match("t*", "test");
    match("t*", "tester");
    matchNot("t*", "");
    matchNot("t*", "a");
    matchNot("t*", "at");
    matchNot("t*", "ate");
    match("af*", "af");
    match("af*", "affe");
    match("af*", "affenkaffee");
    matchNot("af*", "");
    matchNot("af*", "a");
    matchNot("af*", "f");
    matchNot("af*", "kaffee");
  }
  
  public void testStarConstan()
  {
    match("*t*", "t");
    match("*t*", "Test");
    match("*t*", "Tester");
    match("*t*", "test");
    match("*t*", "tester");
    matchNot("*t*", "");
    matchNot("*t*", "a");
    matchNot("*t*", "affe");
  }
  
  public void testConstantStarConstant()
  {
    match("a*e", "ae");
    match("a*e", "affe");
    match("a*e", "affebande");
    matchNot("a*e", "");
    matchNot("a*e", "e");
    matchNot("a*e", "a");
    matchNot("a*e", "af");
    matchNot("a*e", "fe");
    matchNot("a*e", "affen");
    match("a*ea", "aea");
    match("a*ea", "area");
    match("a*ea", "aeaea");
    match("a*ea", "area in area");
    matchNot("a*ea", "");
    matchNot("a*ea", "areas");
    matchNot("a*ea", "aean");
    matchNot("a*ea", "in area");
  }
  
  public void testStarConstantStarConstant()
  {
    match("*a*e", "ae");
    match("*a*e", "are");
    match("*a*e", "tae");
    match("*a*e", "tare");
    match("*a*e", "tare ware");
    matchNot("*a*e", "");
    matchNot("*a*e", "a");
    matchNot("*a*e", "e");
    matchNot("*a*e", "aeaet");
    matchNot("*a*e", "teaer");
    matchNot("*a*e", "aaet");
    matchNot("*a*e", "are?");
    match("*a*a", "aa");
    match("*a*a", "aaa");
    match("*a*a", "taa");
    match("*a*a", "tata");
    match("*a*a", "taatata");
    matchNot("*a*a", "");
    matchNot("*a*a", "a");
    matchNot("*a*a", "ra");
    matchNot("*a*a", "raarar");
  }
  
  private void match(String pattern, String string)
  {
    assertTrue("'" + string + "' expected to match '" + pattern + "'", 
               new WildCardPattern(pattern).matches(string));
  }

  private void matchNot(String pattern, String string)
  {
    assertTrue("'" + string + "' expected not match '" + pattern + "'", 
               !new WildCardPattern(pattern).matches(string));
  }
}
