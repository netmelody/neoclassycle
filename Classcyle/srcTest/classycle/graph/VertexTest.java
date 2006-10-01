package classycle.graph;

import junit.framework.TestCase;

public class VertexTest extends TestCase {
  public VertexTest(String name) {
    super(name);
  }

  public void testAddOutgoingArc() {
    Vertex tail = new Vertex(null);
    Vertex head = new Vertex(null);

    tail.addOutgoingArcTo(head);
    assertEquals(0, tail.getNumberOfIncomingArcs());
    assertEquals(1, tail.getNumberOfOutgoingArcs());
    assertSame(head, tail.getHeadVertex(0));
    assertEquals(0, head.getNumberOfOutgoingArcs());
    assertEquals(1, head.getNumberOfIncomingArcs());
    assertSame(tail, head.getTailVertex(0));

    // multiple arcs are not possible
    tail.addOutgoingArcTo(head);
    assertEquals(1, tail.getNumberOfOutgoingArcs());
    
    // add second arc
    Vertex head2 = new Vertex(null);
    tail.addOutgoingArcTo(head2);
    assertEquals(0, tail.getNumberOfIncomingArcs());
    assertEquals(2, tail.getNumberOfOutgoingArcs());
    assertSame(head, tail.getHeadVertex(0));
    assertSame(head2, tail.getHeadVertex(1));
  }

  public void testAddIncomingArc() {
    Vertex head = new Vertex(null);
    Vertex tail = new Vertex(null);

    head.addIncomingArcTo(tail);
    assertEquals(0, head.getNumberOfOutgoingArcs());
    assertEquals(1, head.getNumberOfIncomingArcs());
    assertSame(tail, head.getTailVertex(0));
    assertEquals(0, tail.getNumberOfIncomingArcs());
    assertEquals(1, tail.getNumberOfOutgoingArcs());
    assertSame(head, tail.getHeadVertex(0));

    // multiple arcs are not possible
    head.addIncomingArcTo(tail);
    assertEquals(1, head.getNumberOfIncomingArcs());
    
    // add second arc
    Vertex tail2 = new Vertex(null);
    head.addIncomingArcTo(tail2);
    assertEquals(0, head.getNumberOfOutgoingArcs());
    assertEquals(2, head.getNumberOfIncomingArcs());
    assertSame(tail, head.getTailVertex(0));
    assertSame(tail2, head.getTailVertex(1));
  }
  
  public void testVisit() {
    Vertex vertex = new Vertex(null);
    assertTrue(!vertex.isVisited());
    vertex.visit();
    assertTrue(vertex.isVisited());
    vertex.visit();
    assertTrue(vertex.isVisited());
    vertex.reset();
    assertTrue(!vertex.isVisited());
    vertex.reset();
    assertTrue(!vertex.isVisited());
  }
}
