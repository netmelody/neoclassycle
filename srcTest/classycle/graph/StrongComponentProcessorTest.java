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
  }
  
  public void testAttributesOfStrongComponent() {
    StrongComponent[] components
        = check(new String[] {"0: 0 1 2 3 6", "0: 4 5", "0: 7"},
                new int[][] {{1},                 // 0
                             {2},                 // 1
                             {2, 3, -1, 4},       // 2
                             {1, -1, 0, 6},       // 3
                             {5},                 // 4
                             {4},                 // 5
                             {3, 7},              // 6
                             {-1}                 // 7
                            });
    checkAttributes(new int[] {4, 2, 2, 1, 1, 1, 4, 1},
                    new int[] {4, 3, 2, 2, 1, 1, 3, 0}, components);

    components
        = check(new String[] {"0: 0", "0: 1", "0: 2 6", "0: 4", "0: 3 7 8 9", 
                              "0: 5"},
                new int[][] {{2},                 // 0
                             {-1},                // 1
                             {1, 6, -1},          // 2
                             {7, -1, 4, 9},       // 3
                             {6},                 // 4
                             {1},                 // 5
                             {2, -1},             // 6
                             {8, 5, -1},          // 7
                             {3, -1},             // 8
                             {3, -1}              // 9
                            });
    checkAttributes(new int[] {1, 1, 1, 1, 1, 1, 1, 2, 2, 3},
                    new int[] {0, 0, 1, 2, 0, 0, 1, 3, 2, 3}, components);
  }
  
  private void checkAttributes(int[] expectedMaximumFragmentSizes,
                               int[] expectedEccentricities, 
                               StrongComponent[] components) {
    for (int i = 0; i < components.length; i++) {
      StrongComponent component = components[i];
      GraphAttributes attributes = (GraphAttributes) component.getAttributes();
      for (int j = 0, n = component.getNumberOfVertices(); j < n; j++) {
        int index = Integer.parseInt(component.getVertex(j)
                                              .getAttributes().toString());
        assertEquals("eccentricity of vertex " + index, 
                     expectedEccentricities[index],
                     n == 1 ? 0 : attributes.getEccentricities()[j]);
        assertEquals("max fragment size of vertex " + index, 
                     expectedMaximumFragmentSizes[index],
                     attributes.getMaximumFragmentSizes()[j]);
      }
    }
  }
}
