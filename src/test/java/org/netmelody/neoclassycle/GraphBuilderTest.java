package org.netmelody.neoclassycle;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.junit.Test;
import org.netmelody.neoclassycle.graph.AtomicVertex;

import static org.junit.Assert.assertEquals;

public final class GraphBuilderTest {

    @Test
    public void testTwoClassesWithSameNameButDifferentSources() {
        final UnresolvedNode[] nodes = createNodes("a:s1:A -> b c a; a:s2 -> b d");

        final AtomicVertex[] graph = GraphBuilder.createGraph(nodes, false);

        assertEquals(1, graph.length);
        final ClassAttributes attributes = (ClassAttributes) graph[0].getAttributes();
        assertEquals("a", attributes.getName());
        assertEquals("s1, s2", attributes.getSources());
        assertEquals(2, attributes.getSize());
        assertEquals(ClassAttributes.CLASS, attributes.getType());
    }

    @Test
    public void testTwoClassesWithSameNameOneWithSourceOneWithoutSource() {
        final UnresolvedNode[] nodes = createNodes("a:s1 -> b c a; a: :A -> b d");

        final AtomicVertex[] graph = GraphBuilder.createGraph(nodes, false);

        assertEquals(1, graph.length);
        final ClassAttributes attributes = (ClassAttributes) graph[0].getAttributes();
        assertEquals("a", attributes.getName());
        assertEquals("s1", attributes.getSources());
        assertEquals(2, attributes.getSize());
        assertEquals(ClassAttributes.ABSTRACT_CLASS, attributes.getType());
    }

    @Test
    public void testSupressedMergingOfInnerclasses() {
        final UnresolvedNode[] nodes = createNodes("a:s:A -> b c a$1; a$1:s -> b d");

        final AtomicVertex[] graph = GraphBuilder.createGraph(nodes, false);

        assertEquals(2, graph.length);
        AtomicVertex node1 = graph[0];
        AtomicVertex node2 = graph[1];
        if (!"a".equals(((ClassAttributes) node1.getAttributes()).getName())) {
            node1 = graph[1];
            node2 = graph[0];
        }

        assertEquals(3, node1.getNumberOfOutgoingArcs());
        assertEquals(0, node1.getNumberOfIncomingArcs());
        ClassAttributes attributes = (ClassAttributes) node1.getAttributes();
        assertEquals("a", attributes.getName());
        assertEquals(2, node2.getNumberOfOutgoingArcs());
        assertEquals(1, node2.getNumberOfIncomingArcs());
        attributes = (ClassAttributes) node2.getAttributes();
        assertEquals("a$1", attributes.getName());
    }

    @Test
    public void testMergingOfInnerclasses() {
        final UnresolvedNode[] nodes = createNodes("a:s:A -> b c a$1; a$1:s -> b d");

        final AtomicVertex[] graph = GraphBuilder.createGraph(nodes, true);

        assertEquals(1, graph.length);
        final AtomicVertex node = graph[0];
        assertEquals(3, node.getNumberOfOutgoingArcs());
        assertEquals(0, node.getNumberOfIncomingArcs());
        final ClassAttributes attributes = (ClassAttributes) node.getAttributes();
        assertEquals("a", attributes.getName());
        assertEquals(4, attributes.getSize());
        assertEquals(ClassAttributes.ABSTRACT_CLASS, attributes.getType());
    }

    private static UnresolvedNode[] createNodes(final String description) {
        final List<UnresolvedNode> nodes = new ArrayList<UnresolvedNode>();
        final StringTokenizer tokenizer = new StringTokenizer(description, ";");
        while (tokenizer.hasMoreTokens()) {
            nodes.add(createNode(tokenizer.nextToken().trim()));
        }
        return nodes.toArray(new UnresolvedNode[nodes.size()]);
    }

    private static UnresolvedNode createNode(final String description) {
        final UnresolvedNode node = new UnresolvedNode();
        final int indexOfArrow = description.indexOf("->");

        final String links = description.substring(indexOfArrow + 2).trim();
        StringTokenizer tokenizer = new StringTokenizer(links);
        while (tokenizer.hasMoreTokens()) {
            node.addLinkTo(tokenizer.nextToken());
        }
        final String attributes = description.substring(0, indexOfArrow).trim();
        tokenizer = new StringTokenizer(attributes, ":");
        final String name = tokenizer.nextToken();
        String source = tokenizer.nextToken().trim();
        if (source.length() == 0) {
            source = null;
        }
        final boolean a = tokenizer.hasMoreTokens() && "A".equals(tokenizer.nextToken());
        final String type = a ? ClassAttributes.ABSTRACT_CLASS : ClassAttributes.CLASS;
        node.setAttributes(new ClassAttributes(name, source, type, name.length()));
        return node;
    }
}
