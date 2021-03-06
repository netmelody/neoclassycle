package org.netmelody.neoclassycle.graph;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class AtomicVertexTest {

    @Test
    public void testGraphVertexProperty() {
        final AtomicVertex vertex = new AtomicVertex(null);
        assertTrue(!vertex.isGraphVertex());
        vertex.reset();
        assertTrue(vertex.isGraphVertex());
        vertex.reset();
        assertTrue(vertex.isGraphVertex());
    }

    @Test
    public void testOrderProperty() {
        final AtomicVertex vertex = new AtomicVertex(null);
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

    @Test
    public void testLowProperty() {
        final AtomicVertex vertex = new AtomicVertex(null);
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
