package classycle;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllTests extends TestCase {
  public AllTests(String name) {
    super(name);
  }

  public static void main(String[] args) {
    junit.textui.TestRunner.run(suite());
  }

  public static Test suite() {
    TestSuite suite= new TestSuite();
    suite.addTest(classycle.graph.AllTests.suite());
    return suite;
  }
}
