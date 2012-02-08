package org.netmelody.neoclassycle.graph;

import java.util.Arrays;
import java.util.HashSet;

import org.netmelody.neoclassycle.ClassAttributes;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class GraphTestCase {
    public static class MockAttributes extends ClassAttributes {
        public MockAttributes(final String name) {
            super(name, "", ClassAttributes.CLASS, 42);
        }

        @Override
        public int getSize() {
            return 0;
        }

        @Override
        public String toString() {
            return getName();
        }
    }

    protected AtomicVertex _externalVertex = new AtomicVertex(null);

    protected StrongComponent[] check(final String[] expectedStrongComponents, final int[][] nodeLinks) {
        final HashSet<String> expectedFingerPrints = new HashSet<String>();
        for (final String expectedStrongComponent : expectedStrongComponents) {
            expectedFingerPrints.add(expectedStrongComponent);
        }
        final AtomicVertex[] graph = createGraph(nodeLinks);
        final StrongComponentProcessor processor = new StrongComponentProcessor(true);
        process(processor, graph);
        final StrongComponent[] components = processor.getStrongComponents();
        for (final StrongComponent component : components) {
            final String fingerPrint = createFingerPrint(component);
            assertTrue("'" + fingerPrint + "' not expected", expectedFingerPrints.contains(fingerPrint));
        }
        assertEquals("number of strong components", expectedStrongComponents.length, components.length);

        return components;
    }

    protected void process(final StrongComponentProcessor processor, final AtomicVertex[] graph) {
        processor.deepSearchFirst(graph);
    }

    private static String createFingerPrint(final StrongComponent component) {
        final int[] vertices = new int[component.getNumberOfVertices()];
        for (int i = 0; i < vertices.length; i++) {
            vertices[i] = Integer.parseInt(((MockAttributes) component.getVertex(i).getAttributes()).toString());
        }
        Arrays.sort(vertices);
        final StringBuffer result = new StringBuffer();
        result.append(component.getLongestWalk()).append(':');
        for (final int vertice : vertices) {
            result.append(' ').append(Integer.toString(vertice));
        }
        return new String(result);
    }

    protected AtomicVertex[] createGraph(final int[][] nodeLinks) {
        final AtomicVertex[] result = new AtomicVertex[nodeLinks.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = new AtomicVertex(new MockAttributes(Integer.toString(i)));
        }
        for (int i = 0; i < result.length; i++) {
            final int[] links = nodeLinks[i];
            for (final int link : links) {
                result[i].addOutgoingArcTo(link < 0 ? _externalVertex : result[link]);
            }
        }
        return result;
    }
}