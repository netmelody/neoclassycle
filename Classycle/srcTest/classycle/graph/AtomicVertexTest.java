package classycle.graph;

import junit.framework.TestCase;

public class AtomicVertexTest extends TestCase {
  public AtomicVertexTest(String name) {
    super(name);
  }

  public void testGraphVertexProperty() {
    AtomicVertex vertex = new AtomicVertex(null);
    assertTrue(!vertex.isGraphVertex());
    vertex.reset();
    assertTrue(vertex.isGraphVertex());
    vertex.reset();
    assertTrue(vertex.isGraphVertex());
  }
  
  public void testOrderProperty() {
    AtomicVertex vertex = new AtomicVertex(null);
    assertEquals(0, vertex.getOrder());
    vertex.reset();
    assertEquals(-1, vertex.getOrder());
    vertex.reset();
    assertEquals(-1, vertex.getOrder());
    vertex.setOrder(42);
    assertEquals(42, vertex.getOrder());
    vertex.reset();
    assertEquals(-1, vertex.getOrder());
  }    
  
  public void testLowProperty() {
    AtomicVertex vertex = new AtomicVertex(null);
    assertEquals(0, vertex.getLow());
    vertex.reset();
    assertEquals(-1, vertex.getLow());
    vertex.reset();
    assertEquals(-1, vertex.getLow());
    vertex.setLow(42);
    assertEquals(42, vertex.getLow());
    vertex.reset();
    assertEquals(-1, vertex.getLow());
  }    
}
