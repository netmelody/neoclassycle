package classycle;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import classycle.graph.AtomicVertex;

import junit.framework.TestCase;

public class GraphBuilderTest extends TestCase
{
  
  public void testTwoClassesWithSameNameButDifferentSources()
  {
    UnresolvedNode[] nodes = createNodes("a:s1:A -> b c a; a:s2 -> b d");
    
    AtomicVertex[] graph = GraphBuilder.createGraph(nodes, false);
    
    assertEquals(1, graph.length);
    ClassAttributes attributes = (ClassAttributes) graph[0].getAttributes();
    assertEquals("a", attributes.getName());
    assertEquals("s1, s2", attributes.getSources()); 
    assertEquals(2, attributes.getSize()); 
    assertEquals(ClassAttributes.CLASS, attributes.getType());
  }
  
  public void testTwoClassesWithSameNameOneWithSourceOneWithoutSource()
  {
    UnresolvedNode[] nodes = createNodes("a:s1 -> b c a; a: :A -> b d");
    
    AtomicVertex[] graph = GraphBuilder.createGraph(nodes, false);
    
    assertEquals(1, graph.length);
    ClassAttributes attributes = (ClassAttributes) graph[0].getAttributes();
    assertEquals("a", attributes.getName());
    assertEquals("s1", attributes.getSources()); 
    assertEquals(2, attributes.getSize()); 
    assertEquals(ClassAttributes.ABSTRACT_CLASS, attributes.getType());
  }
  
  public void testSupressedMergingOfInnerclasses()
  {
    UnresolvedNode[] nodes = createNodes("a:s:A -> b c a$1; a$1:s -> b d");
    
    AtomicVertex[] graph = GraphBuilder.createGraph(nodes, false);
    
    assertEquals(2, graph.length);
    AtomicVertex node = graph[0];
    assertEquals(3, node.getNumberOfOutgoingArcs());
    assertEquals(0, node.getNumberOfIncomingArcs());
    ClassAttributes attributes = (ClassAttributes) node.getAttributes();
    assertEquals("a", attributes.getName());
    node = graph[1];
    assertEquals(2, node.getNumberOfOutgoingArcs());
    assertEquals(1, node.getNumberOfIncomingArcs());
    attributes = (ClassAttributes) node.getAttributes();
    assertEquals("a$1", attributes.getName());
  }
  
  public void testMergingOfInnerclasses()
  {
    UnresolvedNode[] nodes = createNodes("a:s:A -> b c a$1; a$1:s -> b d");
    
    AtomicVertex[] graph = GraphBuilder.createGraph(nodes, true);
    
    assertEquals(1, graph.length);
    AtomicVertex node = graph[0];
    assertEquals(3, node.getNumberOfOutgoingArcs());
    assertEquals(0, node.getNumberOfIncomingArcs());
    ClassAttributes attributes = (ClassAttributes) node.getAttributes();
    assertEquals("a", attributes.getName());
    assertEquals(4, attributes.getSize());
    assertEquals(ClassAttributes.ABSTRACT_CLASS, attributes.getType());
  }
  
  private UnresolvedNode[] createNodes(String description)
  {
    List nodes = new ArrayList();
    StringTokenizer tokenizer = new StringTokenizer(description, ";");
    while (tokenizer.hasMoreTokens())
    {
      nodes.add(createNode(tokenizer.nextToken().trim()));
    }
    return (UnresolvedNode[]) nodes.toArray(new UnresolvedNode[nodes.size()]);
  }
  
  private UnresolvedNode createNode(String description)
  {
    UnresolvedNode node = new UnresolvedNode();
    int indexOfArrow = description.indexOf("->");
    
    String links = description.substring(indexOfArrow + 2).trim();
    StringTokenizer tokenizer = new StringTokenizer(links);
    while (tokenizer.hasMoreTokens())
    {
      node.addLinkTo(tokenizer.nextToken());
    }
    String attributes = description.substring(0, indexOfArrow).trim();
    tokenizer = new StringTokenizer(attributes, ":");
    String name = tokenizer.nextToken();
    String source = tokenizer.nextToken().trim();
    if (source.length() == 0)
    {
      source = null;
    }
    boolean a = tokenizer.hasMoreTokens() && "A".equals(tokenizer.nextToken());
    String type = a ? ClassAttributes.ABSTRACT_CLASS : ClassAttributes.CLASS;
    node.setAttributes(new ClassAttributes(name, source, type, name.length()));
    return node;
  }
}
