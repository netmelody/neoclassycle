/*
 * Created on 04.09.2004
 */
package classycle.dependency;

import junit.framework.TestCase;
import classycle.graph.AtomicVertex;
import classycle.graph.GraphTestCase.MockAttributes;

/**
 * @author  Franz-Josef Elmer
 */
public class DependencyProcessorTest extends TestCase
{
  private static final String DIDOF 
      = ' ' + DependencyDefinitionParser.DIRECTLY_INDEPENDENT_OF_KEY_WORD + ' ';
  private static final String CHECK 
      = DependencyDefinitionParser.CHECK_KEY_WORD + ' ';
  private static final String OK = DependencyResult.OK + '\n';
  private String DF = DependencyResult.DEPENDENCIES_FOUND;
  private static final String SHOW_ALLRESULTS = "show allResults\n";
  private static final String SHOW_ALL = "show allPaths allResults\n";
  private static final String SHOW_ONLY_ALL 
       = "show onlyShortestPaths allResults\n";

  public DependencyProcessorTest(String s)
  {
    super(s);
  }

  public void testCheckSets()
  {
    String s = "[a] = a.*\n"
             + "check sets [a] b.* bla.bla";
    check(SHOW_ALLRESULTS + s, 
            new String[] {SHOW_ONLY_ALL, 
                          "Set [a] has 2 classes.\n", 
                          "Set b.* has one class.\n", 
                          "Set bla.bla is empty.\n"});
    check(s, new String[] {"", "", "Set bla.bla is empty.\n"});
  }
  
  public void testClassCycleCheck()
  {
    String s = "[a] = a.*\n"
             + "check absenceOfClassCycles > 1 in b.*\n"
             + "check absenceOfClassCycles > 1 in [a]";
    String result = "check absenceOfClassCycles > 1 in [a]\n" + 
                        "  a.A et al. contains 2 classes:\n" + 
                        "    a.A\n" + 
                        "    a.B\n";
    check(SHOW_ALLRESULTS + s, 
            new String[] {SHOW_ONLY_ALL, 
                          "check absenceOfClassCycles > 1 in b.*\tOK\n", 
                          result});
    check(s, new String[] {"", result});
  }
  
  public void testPackageCycleCheck()
  {
    String s = "[A] = *.A\n"
             + "check absenceOfPackageCycles > 1 in *.B\n"
             + "check absenceOfPackageCycles > 1 in [A]";
    String result = "check absenceOfPackageCycles > 1 in [A]\n" + 
                          "  i et al. contains 2 packages:\n" + 
                          "    i\n" + 
                          "    b\n";
    check(SHOW_ALLRESULTS + s, 
            new String[] {SHOW_ONLY_ALL, 
                          "check absenceOfPackageCycles > 1 in *.B\tOK\n", 
                          result});
    check(s, new String[] {"", result});
  }
  
  public void testSimpleDependencyCheck()
  {
    String s = "check b.* independentOf c.*"; 
    check(SHOW_ALLRESULTS + s + "\nshow allPaths", 
          new String[] {SHOW_ONLY_ALL, 
                        s + OK, 
                        SHOW_ALL});
    s = "check f.* independentOf b.*";
    check(SHOW_ALLRESULTS + s, 
          new String[] {SHOW_ONLY_ALL, s + DF + "\n  f.A\n    -> b.A\n"});
  }
  
  public void testPreferences()
  {
    String s1 = "check b.* independentOf c.*";
    String s2 = "check f.* independentOf b.*";
    String s = s1 + '\n' + s2 + '\n';
    check(s + SHOW_ALLRESULTS + s + "show onlyFailures\n" + s,
          new String[] {"", 
                        s2 + DF + "\n  f.A\n    -> b.A\n",
                        SHOW_ONLY_ALL,
                        s1 + OK,
                        s2 + DF + "\n  f.A\n    -> b.A\n",
                        "", 
                        "",
                        s2 + DF + "\n  f.A\n    -> b.A\n"});
  }
  
  public void testSwitchAllPathsAndBack()
  {
    String s = "check h.* independentOf b.*";
    check(SHOW_ALLRESULTS + s + "\nshow allPaths\n" 
                          + s + "\nshow onlyShortestPaths\n" + s,
          new String[] {SHOW_ONLY_ALL,
                        s + DF + "\n  h.A\n    -> f.A\n      -> b.A\n", 
                        SHOW_ALL,
                        s + DF + "\n  h.A\n    -> e.A\n      -> e.B\n"
                        + "        -> b.A\n    -> f.A\n      -> b.A\n",
                        SHOW_ONLY_ALL,
                        s + DF + "\n  h.A\n    -> f.A\n      -> b.A\n"});
  }
  
  public void testLayering()
  {
    check(SHOW_ALLRESULTS + "layer bc = b.* c.*\n"
          + "layer f = f.*\n"
          + "check layeringOf bc f",
          new String[] {SHOW_ONLY_ALL,
                        CHECK + "b.*" + DIDOF + "c.*" + OK 
                        + CHECK + "c.*" + DIDOF + "b.*" + OK
                        + CHECK + "b.*" + DIDOF + "f.*" + OK
                        + CHECK + "c.*" +DIDOF + "f.*" + OK});  
    String result = CHECK + "[a]" + DIDOF + "b.*" + OK 
                        + CHECK + "[a]" + DIDOF + "c.*" + OK
                        + CHECK + "b.*" + DIDOF + "[a]" + OK
                        + CHECK + "b.*" + DIDOF + "c.*" + OK
                        + CHECK + "c.*" + DIDOF + "[a]" + OK
                        + CHECK + "c.*" + DIDOF + "b.*" + OK
                        + CHECK + "[a]" + DIDOF + "e.*" + OK
                        + CHECK + "[a]" + DIDOF + "f.*" + OK
                        + CHECK + "b.*" + DIDOF + "e.*" + OK
                        + CHECK + "b.*" + DIDOF + "f.*" + OK
                        + CHECK + "c.*" + DIDOF + "e.*" + OK
                        + CHECK + "c.*" + DIDOF + "f.*" + OK
                        + CHECK + "[a]" + DIDOF + "h.*" + OK
                        + CHECK + "b.*" + DIDOF + "h.*" + OK
                        + CHECK + "c.*" + DIDOF + "h.*" + OK
                        + CHECK + "e.*" + DIDOF + "f.*" + OK
                        + CHECK + "f.*" + DIDOF + "e.*" + OK
                        + CHECK + "e.*" + DIDOF + "h.*" + OK
                        + CHECK + "f.*" + DIDOF + "h.*" + OK;
    check(SHOW_ALLRESULTS + "[a] = a.* excluding a.B\n"
          + "layer abc = [a] b.* c.* \n"
          + "layer ef = e.* f.*\n"
          + "layer h = h.*\n"
          + "check layeringOf abc ef h\n"
          + "check strictLayeringOf abc ef h",
          new String[] {SHOW_ONLY_ALL,
                        result,
                        result
                        + CHECK + "h.*" +DIDOF + "[a]" + DF
                        + "\n  h.A\n    -> a.A\n"
                        + CHECK + "h.*" +DIDOF + "b.*" + OK
                        + CHECK + "h.*" +DIDOF + "c.*" + OK
                        });  
    check("[a] = a.* excluding a.B\n"
          + "layer abc = [a] b.* c.* \n"
          + "layer ef = e.* f.*\n"
          + "layer h = h.*\n"
          + "check layeringOf abc ef h\n"
          + "check strictLayeringOf abc ef h",
          new String[] {"", CHECK + "h.*" +DIDOF + "[a]" + DF
                            + "\n  h.A\n    -> a.A\n"});  
  }
  
  private void check(String description, String[] expectedResults)
  {
    AtomicVertex[] graph = createGraph();
    ResultRenderer renderer = new DefaultResultRenderer();
    DependencyProcessor processor 
                          = new DependencyProcessor(description, null, renderer);
    int i = 0;
    while (processor.hasMoreStatements())
    {
      assertEquals("Result " + i, expectedResults[i++], 
                   renderer.render(processor.executeNextStatement(graph)));
    }
    if (i < expectedResults.length)
    {
      fail((expectedResults.length - i) + " more results expected");
    }
  }
  
  private AtomicVertex[] createGraph()
  {
    return createGraph(new String[] {"a.A", "a.B", "b.A", "c.A", 
                                     "e.A", "e.B", "f.A", "h.A", "i.A", },
                       new int[][] {{1},      // 0: a.A
                                    {0},      // 1: a.B
                                    {8},      // 2: b.A
                                    {},       // 3: c.A
                                    {0, 5},   // 4: e.A
                                    {2},      // 5: e.B
                                    {2},      // 6: f.A
                                    {0, 4, 6},// 7: h.A
                                    {2},      // 8: i.A
                                   });

  }

  private AtomicVertex[] createGraph(String[] nodes, int[][] nodeLinks) {
    AtomicVertex[] result = new AtomicVertex[nodes.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = new AtomicVertex(new MockAttributes(nodes[i]));
    }
    for (int i = 0; i < result.length; i++) {
      int[] links = nodeLinks[i];
      for (int j = 0; j < links.length; j++) {
        int link = links[j];
        result[i].addOutgoingArcTo(result[link]);
      }
    }
    return result;
  }
}
