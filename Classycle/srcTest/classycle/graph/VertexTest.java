package classycle.graph;

import junit.framework.TestCase;

/**
 * 
 * 
 * @author Franz-Josef Elmer
 */
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

    tail.addOutgoingArcTo(head);
    assertEquals(1, tail.getNumberOfOutgoingArcs());
  }
}
