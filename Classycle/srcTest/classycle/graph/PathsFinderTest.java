/*
 * Created on 18.06.2004
 */
package classycle.graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import junit.framework.TestCase;

/**
 * @author  Franz-Josef Elmer
 */
public class PathsFinderTest extends TestCase
{
  private static class MockVertexCondition implements VertexCondition
  {
    private final HashSet _vertices = new HashSet();
    
    public MockVertexCondition(Vertex[] vertices)
    {
      for (int i = 0; i < vertices.length; i++)
      {
        _vertices.add(vertices[i]);
      }
    }
    
    public boolean isFulfilled(Vertex vertex)
    {
      return _vertices.contains(vertex);
    }
  }
  
  private static final HashMap REPOSITORY = new HashMap();
  private static final HashSet GRAPH = new HashSet();
  
  private static MockVertex getVertex(String name)
  {
    MockVertex vertex = (MockVertex) REPOSITORY.get(name);
    if (vertex == null)
    {
      vertex = new MockVertex(name);
    }
    if (!name.startsWith("$"))
    {
      GRAPH.add(vertex);
    }
    return vertex;
  }
  
  private static MockVertex[] getAllVertices()
  {
    return (MockVertex[]) GRAPH.toArray(new MockVertex[0]);
    
  }

  private static class MockVertex extends AtomicVertex
  {
    public int countReset;
    public int countVisit;
    public final String name;

    public MockVertex(String name)
    {
      super(null);
      this.name = name;
      REPOSITORY.put(name, this);
    }

    public void reset()
    {
      countReset++;
      super.reset();
    }

    public void visit()
    {
      countVisit++;
      super.visit();
    }
    
    
    public String toString()
    {
      return name;
    }

  }
  
  public PathsFinderTest(String name)
  {
    super(name);
  }
  
  public void testFindAllPaths()
  {
    check("a b c", "e f", "a d e", "a > b c d h, c > b k, d > e g", false);
    MockVertex a = getVertex("a");
    MockVertex b = getVertex("b");
    MockVertex c = getVertex("c");
    assertEquals(0, a.getNumberOfIncomingArcs());
    assertEquals(4, a.getNumberOfOutgoingArcs());
    assertSame(b, a.getHeadVertex(0));
    assertSame(c, a.getHeadVertex(1));
    assertEquals(2, b.getNumberOfIncomingArcs());
    assertEquals(0, b.getNumberOfOutgoingArcs());
    assertSame(a, b.getTailVertex(0));
    assertSame(c, b.getTailVertex(1));

    check("a b c", "e f", "a d e f h i g", "a > b c d h,"
                                         + "c > b k,"
                                         + "h > d i,"
                                         + "i > h f,"
                                         + "e > f,"
                                         + "f > e g,"
                                         + "g > b e,"
                                         + "d > e g", false);
    check("a b", "h $1 $2", "a b c d e g h $1 $2", "a > b c $2,"
                                                 + "c > a e,"
                                                 + "d > $1 k l,"
                                                 + "e > f g,"
                                                 + "g > f h,"
                                                 + "h > j,"
                                                 + "j > e,"
                                                 + "k > l,"
                                                 + "l > d,"
                                                 + "b > c d", false);
    check("a", "f", "a b c d e f", "a > b, b > c d, c > e, d > e, e > f", false);
    check("a", "c", "", "a > b, c > b", false);
    check("a", "b d e", "a b e", "a > b e, b > c d e", false);
    check("a f", "b d e", "a b e", "a > b e, b > c d e, f > a", false);
    check("a b", "e", "a c e", "a > c, c > e, b > d, d > a", false);
    check("a", "b", "a b c d", "a > b c, c > b d, d > b c", false);
    check("a", "e", "a b c d e f", "a > b, b > c f, c > d, d > e, f > e", false);
  }
  
  public void testFindShortestPaths()
  {
    check("a b c", "e f", "a d e", "a > b c d h, c > b k, d > e g", true);
    check("a b", "h i", "a b c d e g h i", "a > b c,"
                                         + "c > a e,"
                                         + "d > i k l,"
                                         + "e > f g,"
                                         + "g > f h,"
                                         + "h > j,"
                                         + "j > e,"
                                         + "k > l,"
                                         + "l > d,"
                                         + "b > c d", true);
    check("a", "f", "a b c d e f", "a > b, b > c d, c > e, d > e, e > f", true);
    check("a", "c", "", "a > b, c > b", true);
    check("a", "e", "a b f e", "a > b, b > c f, c > d, d > e, f > e", true);
    check("a b", "e", "a b c d e", "a > c, c > e, b > d, d > c e", true);
    check("a e", "d h", "a b h e f d", "a > b, b > c h, c > d,"
                                     + "e > f, f > d g, g > h", true);
    check("a", "h", "a b c h", "a > b i,"
                             + "b > a c,"
                             + "c > d h,"
                             + "d > b e,"
                             + "e > f,"
                             + "f > g,"
                             + "i > j,"
                             + "j > i d", true);
  }
  
  public void testDirectPaths()
  {
    check("a b", "a c", "a", "", false, true);
    check("a b", "a c", "a", "", true, true);
    check("a d f g", "c e", "a d g c e", 
          "a > b e, b > c, d > c, f > d, g > b e h, h > i, i > e", false, true);
    check("a d f g", "c e", "a d g c e", 
          "a > b e, b > c, d > c, f > d, g > b e h, h > i, i > e", true, true);
  }
  
  private void check(String startVertices, String endVertices, 
                     String pathVertices, String graphDescription,
                     boolean shortestOnly)
  {
    check(startVertices, endVertices, pathVertices, graphDescription, 
          shortestOnly, false);
  }
  
  private void check(String startVertices, String endVertices, 
                     String pathVertices, String graphDescription,
                     boolean shortestOnly, boolean directPathsOnly)
  {
    REPOSITORY.clear();
    MockVertexCondition startCondition 
        = new MockVertexCondition(createVertices(startVertices));
    MockVertexCondition endCondition 
        = new MockVertexCondition(createVertices(endVertices));
    PathsFinder pathsFinder 
        = new PathsFinder(startCondition, endCondition, shortestOnly, directPathsOnly);
    MockVertex[] expectedPaths = createVertices(pathVertices);
    StringTokenizer tokenizer = new StringTokenizer(graphDescription, ",");
    while (tokenizer.hasMoreTokens())
    {
      createLinks(tokenizer.nextToken().trim());
    }
    Vertex[] paths = pathsFinder.findPaths(getAllVertices());

    HashSet pathNodes = new HashSet(Arrays.asList(paths));
    if (expectedPaths.length == pathNodes.size() 
        && expectedPaths.length == paths.length)
    {
      for (int i = 0; i < expectedPaths.length; i++)
      {
        assertTrue(expectedPaths[i].name + " expected", 
                   pathNodes.contains(expectedPaths[i]));
      }
    } else {
      fail("Expected " + Arrays.asList(expectedPaths) 
           + " but was " + Arrays.asList(paths));
    }
  }
  
  private MockVertex[] createVertices(String vertexList)
  {
    StringTokenizer tokenizer = new StringTokenizer(vertexList);
    MockVertex[] result = new MockVertex[tokenizer.countTokens()];
    for (int i = 0; i < result.length; i++)
    {
      result[i] = getVertex(tokenizer.nextToken());
    }
    return result;
  }
  
  private void createLinks(String linkDescription)
  {
    StringTokenizer tokenizer = new StringTokenizer(linkDescription, ">");
    Vertex startVertex = getVertex(tokenizer.nextToken().trim());
    tokenizer = new StringTokenizer(tokenizer.nextToken().trim());
    while (tokenizer.hasMoreTokens())
    {
      startVertex.addOutgoingArcTo(getVertex(tokenizer.nextToken()));
    }
  }

}
