package org.netmelody.neoclassycle.graph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class VertexTest {

    @Test
    public void testAddOutgoingArc() {
        final Vertex tail = new Vertex(null);
        final Vertex head = new Vertex(null);

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
        final Vertex head2 = new Vertex(null);
        tail.addOutgoingArcTo(head2);
        assertEquals(0, tail.getNumberOfIncomingArcs());
        assertEquals(2, tail.getNumberOfOutgoingArcs());
        assertSame(head, tail.getHeadVertex(0));
        assertSame(head2, tail.getHeadVertex(1));
    }

    @Test
    public void testAddIncomingArc() {
        final Vertex head = new Vertex(null);
        final Vertex tail = new Vertex(null);

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
        final Vertex tail2 = new Vertex(null);
        head.addIncomingArcTo(tail2);
        assertEquals(0, head.getNumberOfOutgoingArcs());
        assertEquals(2, head.getNumberOfIncomingArcs());
        assertSame(tail, head.getTailVertex(0));
        assertSame(tail2, head.getTailVertex(1));
    }

    @Test
    public void testVisit() {
        final Vertex vertex = new Vertex(null);
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
