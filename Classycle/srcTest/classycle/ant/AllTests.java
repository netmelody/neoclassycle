/*
 * Created on 01.07.2004
 */
package classycle.ant;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author  Franz-Josef Elmer
 */
public class AllTests
{

  public static Test suite()
  {
    TestSuite suite = new TestSuite("Test for classycle.ant");
    //$JUnit-BEGIN$
    suite.addTestSuite(ReportTaskTest.class);
    //$JUnit-END$
    return suite;
  }
}
