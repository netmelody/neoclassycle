/*
 * Created on 19.06.2004
 */
package classycle.util;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author  Franz-Josef Elmer
 */
public class AllTests
{

  public static Test suite()
  {
    TestSuite suite = new TestSuite("Test for classycle.util");
    //$JUnit-BEGIN$
    suite.addTest(new TestSuite(WildCardPatternTest.class));
    //$JUnit-END$
    return suite;
  }
}
