package classycle.graph;



/**
 * 
 * 
 * @author Franz-Josef Elmer
 */
public class StrongComponentProcessorTest extends GraphTestCase {
  public StrongComponentProcessorTest(String s) {
    super(s);
  }

  public void testZeroVertexGraph() {
    check(new String[0], new int[0][0]);
  }

  public void testOneVertexGraphs() {
    check(new String[] {"0: 0"}, new int[][] {{}});
    check(new String[] {"0: 0"}, new int[][] {{0}});
    check(new String[] {"0: 0"}, new int[][] {{-1}});
    check(new String[] {"0: 0"}, new int[][] {{-1,0}});
    check(new String[] {"0: 0"}, new int[][] {{0,-1}});
    check(new String[] {"0: 0"}, new int[][] {{-1,0,-1}});
  }

  public void testTwoVerticesGraphs() {
    check(new String[] {"0: 0 1"}, new int[][] {{1},{0}});
    check(new String[] {"0: 0 1"}, new int[][] {{1,0},{0,-1}});
    check(new String[] {"0: 0 1"}, new int[][] {{0,1},{0,1}});
    check(new String[] {"0: 0","0: 1"}, new int[][] {{},{}});
    check(new String[] {"0: 0","0: 1"}, new int[][] {{},{0}});
    check(new String[] {"0: 0","0: 1"}, new int[][] {{1},{}});
    check(new String[] {"0: 1","0: 0"}, new int[][] {{0},{0,-1}});
  }

  public void testThreeVerticesGraphs() {
    check(new String[] {"0: 0", "0: 1", "0: 2"}, new int[][] {{},{},{-1}});
    check(new String[] {"0: 0", "0: 1", "0: 2"}, new int[][] {{0},{1},{2}});
    check(new String[] {"0: 0 1", "0: 2"}, new int[][] {{1},{0},{-1,2}});
    check(new String[] {"0: 0 1", "0: 2"}, new int[][] {{1},{0,1},{0}});
    check(new String[] {"0: 2", "0: 0 1"}, new int[][] {{1},{0},{0,1}});
    check(new String[] {"0: 0 2", "0: 1"}, new int[][] {{-1,2},{0},{-1,0}});
    check(new String[] {"0: 0 1 2"}, new int[][] {{2},{0},{1}});
    check(new String[] {"0: 0 1 2"}, new int[][] {{2},{0,2},{1}});
    check(new String[] {"0: 0 1 2"}, new int[][] {{2},{2,0},{1}});
  }
  
  public void testComplexGraphs() {
    check(new String[] {"0: 0 1 2 3", "0: 4 5"},
          new int[][] {{1, 2},              // 0
                       {0},                 // 1
                       {3, -1, -1},         // 2
                       {1, -1, 4},          // 3
                       {5},                 // 4
                       {4},                 // 5
                      });
    check(new String[] {"0: 0 1 2 3", "0: 4 5"},
          new int[][] {{1, 2},              // 0
                       {0},                 // 1
                       {3, -1, -1},         // 2
                       {1, -1, 4},          // 3
                       {5},                 // 4
                       {4},                 // 5
                      });
    check(new String[] {"0: 0 1 2 3", "0: 4 5"},
          new int[][] {{1, 2},              // 0
                       {0},                 // 1
                       {3, -1, -1},         // 2
                       {4, -1, 1},          // 3
                       {5},                 // 4
                       {4},                 // 5
                      });
    check(new String[] {"0: 0 1 2 3 6", "0: 4 5", "0: 7"},
          new int[][] {{1},                 // 0
                       {2},                 // 1
                       {2, 3, -1, 4},       // 2
                       {1, -1, 0, 6},       // 3
                       {5},                 // 4
                       {4},                 // 5
                       {3, 7},              // 6
                       {-1}                 // 7
                      });
    check(new String[] {"0: 0", "0: 1", "0: 2 6", "0: 4", "0: 3 7 8", "0: 5"},
          new int[][] {{2},                 // 0
                       {-1},                // 1
                       {1, 6, -1},          // 2
                       {7, -1, 4},          // 3
                       {6},                 // 4
                       {1},                 // 5
                       {2, -1},             // 6
                       {8, 5, -1},          // 7
                       {3, -1}              // 8
                      });
  }
}
