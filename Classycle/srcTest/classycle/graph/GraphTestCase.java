package classycle.graph;

import java.util.Arrays;
import java.util.HashSet;

import classycle.ClassAttributes;

import junit.framework.TestCase;

public abstract class GraphTestCase extends TestCase {
  public static class MockAttributes extends ClassAttributes {
    public MockAttributes(String name) {
      super(name, ClassAttributes.CLASS, 42);
    }
    public int getSize()
    {
      return 0;
    }
    public String toString()
    {
      return getName();
    }
  }

  protected AtomicVertex _externalVertex = new AtomicVertex(null);

  public GraphTestCase(String s) {
    super(s);
  }

  protected StrongComponent[] check(String[] expectedStrongComponents, 
                                    int[][] nodeLinks) {
    HashSet expectedFingerPrints = new HashSet();
    for (int i = 0; i < expectedStrongComponents.length; i++) {
      expectedFingerPrints.add(expectedStrongComponents[i]);
    }
    AtomicVertex[] graph = createGraph(nodeLinks);
    StrongComponentProcessor processor = new StrongComponentProcessor(true);
    process(processor, graph);
    StrongComponent[] components = processor.getStrongComponents();
    for (int i = 0; i < components.length; i++) {
      String fingerPrint = createFingerPrint(components[i]);
      assertTrue("'" + fingerPrint + "' not expected", 
                 expectedFingerPrints.contains(fingerPrint));
    }
    assertEquals("number of strong components", 
                 expectedStrongComponents.length, components.length);

    return components;
  }
  
  protected void process(StrongComponentProcessor processor, 
                         AtomicVertex[] graph) {
    processor.deepSearchFirst(graph);
  }
  
  private String createFingerPrint(StrongComponent component) {
    int[] vertices = new int[component.getNumberOfVertices()];
    for (int i = 0; i < vertices.length; i++) {
      vertices[i] = Integer.parseInt(((MockAttributes) component.getVertex(i)
                                     .getAttributes()).toString());
    }
    Arrays.sort(vertices);
    StringBuffer result = new StringBuffer();
    result.append(component.getLongestWalk()).append(':');
    for (int i = 0; i < vertices.length; i++) {
      result.append(' ').append(Integer.toString(vertices[i]));
    }
    return new String(result);
  }

  protected AtomicVertex[] createGraph(int[][] nodeLinks) {
    AtomicVertex[] result = new AtomicVertex[nodeLinks.length];
    for (int i = 0; i < result.length; i++) {
      result[i] = new AtomicVertex(new MockAttributes(Integer.toString(i)));
    }
    for (int i = 0; i < result.length; i++) {
      int[] links = nodeLinks[i];
      for (int j = 0; j < links.length; j++) {
        int link = links[j];
        result[i].addOutgoingArcTo(link < 0 ? _externalVertex : result[link]);
      }
    }
    return result;
  }
} //class