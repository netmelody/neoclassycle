package classycle.graph;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase
{
  public AllTests(String s)
  {
    super(s);
  }

  public static Test suite()
  {
    TestSuite suite = new TestSuite("All package tests in "
        + "classycle.graph");

    suite.addTestSuite(AtomicVertexTest.class);
    suite.addTestSuite(LongestWalkProcessorTest.class);
    suite.addTestSuite(StrongComponentProcessorTest.class);
    suite.addTestSuite(VertexTest.class);

    return suite;
  }
} //class