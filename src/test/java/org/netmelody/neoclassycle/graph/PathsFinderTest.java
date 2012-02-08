/*
 * Created on 18.06.2004
 */
package org.netmelody.neoclassycle.graph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * @author Franz-Josef Elmer
 */
public class PathsFinderTest {
    private static class MockVertexCondition implements VertexCondition {
        private final HashSet<Vertex> _vertices = new HashSet<Vertex>();

        public MockVertexCondition(final Vertex[] vertices) {
            for (final Vertex vertice : vertices) {
                _vertices.add(vertice);
            }
        }

        @Override
        public boolean isFulfilled(final Vertex vertex) {
            return _vertices.contains(vertex);
        }
    }

    private static final HashMap<String, MockVertex> REPOSITORY = new HashMap<String, MockVertex>();
    private static final HashSet<Vertex> GRAPH = new HashSet<Vertex>();

    private static MockVertex getVertex(final String name) {
        MockVertex vertex = REPOSITORY.get(name);
        if (vertex == null) {
            vertex = new MockVertex(name);
        }
        if (!name.startsWith("$")) {
            GRAPH.add(vertex);
        }
        return vertex;
    }

    private static MockVertex[] getAllVertices() {
        return GRAPH.toArray(new MockVertex[0]);

    }

    private static class MockVertex extends AtomicVertex {
        public final String name;

        public MockVertex(final String name) {
            super(null);
            this.name = name;
            REPOSITORY.put(name, this);
        }

        @Override
        public void reset() {
            super.reset();
        }

        @Override
        public void visit() {
            super.visit();
        }

        @Override
        public String toString() {
            return name;
        }

    }

    @Test
    public void testFindAllPaths() {
        check("a b c", "e f", "a d e", "a > b c d h, c > b k, d > e g", false);
        final MockVertex a = getVertex("a");
        final MockVertex b = getVertex("b");
        final MockVertex c = getVertex("c");
        assertEquals(0, a.getNumberOfIncomingArcs());
        assertEquals(4, a.getNumberOfOutgoingArcs());
        assertSame(b, a.getHeadVertex(0));
        assertSame(c, a.getHeadVertex(1));
        assertEquals(2, b.getNumberOfIncomingArcs());
        assertEquals(0, b.getNumberOfOutgoingArcs());
        assertSame(a, b.getTailVertex(0));
        assertSame(c, b.getTailVertex(1));

        check("a b c", "e f", "a d e f h i g", "a > b c d h," + "c > b k," + "h > d i," + "i > h f," + "e > f," + "f > e g," + "g > b e,"
                + "d > e g", false);
        check("a b", "h $1 $2", "a b c d e g h $1 $2", "a > b c $2," + "c > a e," + "d > $1 k l," + "e > f g," + "g > f h," + "h > j,"
                + "j > e," + "k > l," + "l > d," + "b > c d", false);
        check("a", "f", "a b c d e f", "a > b, b > c d, c > e, d > e, e > f", false);
        check("a", "c", "", "a > b, c > b", false);
        check("a", "b d e", "a b e", "a > b e, b > c d e", false);
        check("a f", "b d e", "a b e", "a > b e, b > c d e, f > a", false);
        check("a b", "e", "a c e", "a > c, c > e, b > d, d > a", false);
        check("a", "b", "a b c d", "a > b c, c > b d, d > b c", false);
        check("a", "e", "a b c d e f", "a > b, b > c f, c > d, d > e, f > e", false);
    }

    @Test
    public void testFindShortestPaths() {
        check("a b c", "e f", "a d e", "a > b c d h, c > b k, d > e g", true);
        check("a b", "h i", "a b c d e g h i", "a > b c," + "c > a e," + "d > i k l," + "e > f g," + "g > f h," + "h > j," + "j > e,"
                + "k > l," + "l > d," + "b > c d", true);
        check("a", "f", "a b c d e f", "a > b, b > c d, c > e, d > e, e > f", true);
        check("a", "c", "", "a > b, c > b", true);
        check("a", "e", "a b f e", "a > b, b > c f, c > d, d > e, f > e", true);
        check("a b", "e", "a b c d e", "a > c, c > e, b > d, d > c e", true);
        check("a e", "d h", "a b h e f d", "a > b, b > c h, c > d," + "e > f, f > d g, g > h", true);
        check("a", "h", "a b c h", "a > b i," + "b > a c," + "c > d h," + "d > b e," + "e > f," + "f > g," + "i > j," + "j > i d", true);
    }

    @Test
    public void testDirectPaths() {
        check("a b", "a c", "a", "", false, true);
        check("a b", "a c", "a", "", true, true);
        check("a d f g", "c e", "a d g c e", "a > b e, b > c, d > c, f > d, g > b e h, h > i, i > e", false, true);
        check("a d f g", "c e", "a d g c e", "a > b e, b > c, d > c, f > d, g > b e h, h > i, i > e", true, true);
    }

    private static void check(final String startVertices, final String endVertices, final String pathVertices, final String graphDescription, final boolean shortestOnly) {
        check(startVertices, endVertices, pathVertices, graphDescription, shortestOnly, false);
    }

    private static void check(final String startVertices, final String endVertices, final String pathVertices, final String graphDescription, final boolean shortestOnly,
            final boolean directPathsOnly) {
        REPOSITORY.clear();
        final MockVertexCondition startCondition = new MockVertexCondition(createVertices(startVertices));
        final MockVertexCondition endCondition = new MockVertexCondition(createVertices(endVertices));
        final PathsFinder pathsFinder = new PathsFinder(startCondition, endCondition, shortestOnly, directPathsOnly);
        final MockVertex[] expectedPaths = createVertices(pathVertices);
        final StringTokenizer tokenizer = new StringTokenizer(graphDescription, ",");
        while (tokenizer.hasMoreTokens()) {
            createLinks(tokenizer.nextToken().trim());
        }
        final Vertex[] paths = pathsFinder.findPaths(getAllVertices());

        final HashSet<Vertex> pathNodes = new HashSet<Vertex>(Arrays.asList(paths));
        if (expectedPaths.length == pathNodes.size() && expectedPaths.length == paths.length) {
            for (final MockVertex expectedPath : expectedPaths) {
                assertTrue(expectedPath.name + " expected", pathNodes.contains(expectedPath));
            }
        }
        else {
            fail("Expected " + Arrays.asList(expectedPaths) + " but was " + Arrays.asList(paths));
        }
    }

    private static MockVertex[] createVertices(final String vertexList) {
        final StringTokenizer tokenizer = new StringTokenizer(vertexList);
        final MockVertex[] result = new MockVertex[tokenizer.countTokens()];
        for (int i = 0; i < result.length; i++) {
            result[i] = getVertex(tokenizer.nextToken());
        }
        return result;
    }

    private static void createLinks(final String linkDescription) {
        StringTokenizer tokenizer = new StringTokenizer(linkDescription, ">");
        final Vertex startVertex = getVertex(tokenizer.nextToken().trim());
        tokenizer = new StringTokenizer(tokenizer.nextToken().trim());
        while (tokenizer.hasMoreTokens()) {
            startVertex.addOutgoingArcTo(getVertex(tokenizer.nextToken()));
        }
    }

}
