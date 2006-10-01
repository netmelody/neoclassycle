/*
 * Created on 30.08.2004
 */
package classycle.dependency;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author  Franz-Josef Elmer
 */
public class AllTests
{

  public static Test suite()
  {
    TestSuite suite = new TestSuite("Test for dependency");
    //$JUnit-BEGIN$
    suite.addTestSuite(DependencyDefinitionParserTest.class);
    suite.addTestSuite(DependencyProcessorTest.class);
    //$JUnit-END$
    return suite;
  }
}