package classycle.graph;

import junit.framework.TestCase;

public class GraphTestCase extends TestCase
{
  private static class MockAttributes implements Attributes
  {
    private final String _name;
    public MockAttributes(String name)
    {
      _name = name;
    }

    public String toString()
    {
      return _name;
    }
  }

  protected Vertex[] _cyclicGraph, _acyclicGraph;
  protected Vertex _externalVertex = new Vertex(null);

  public GraphTestCase(String s)
  {
    super(s);
  }

  protected void setUp() throws Exception
  {
    _cyclicGraph = createGraph(new int[][] {{1},                 // 0
                                            {2},                 // 1
                                            {2, 3, -1, 4},       // 2
                                            {1, -1, 0, 6},       // 3
                                            {5},                 // 4
                                            {4},                 // 5
                                            {3, 7},              // 6
                                            {-1}                 // 7
                                           });
    _acyclicGraph = createGraph(new int[][] {{1, -1, -1, 2},     // 0
                                             {},                 // 1
                                             {1},                // 2
                                             {4, 1, -1, 5},      // 3
                                             {2},                // 4
                                             {1, -1}             // 5
                                            });
  }

  protected Vertex[] createGraph(int[][] nodeLinks)
  {
    Vertex[] result = new Vertex[nodeLinks.length];
    for (int i = 0; i < result.length; i++)
    {
      result[i] = new Vertex(new MockAttributes(Integer.toString(i)));
    }
    for (int i = 0; i < result.length; i++)
    {
      int[] links = nodeLinks[i];
      for (int j = 0; j < links.length; j++)
      {
        int link = links[j];
        result[i].addOutgoingArcTo(link < 0 ? _externalVertex : result[link]);
      }
    }
    return result;
  }


} //class