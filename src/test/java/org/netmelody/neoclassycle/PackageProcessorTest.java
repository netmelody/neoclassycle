package org.netmelody.neoclassycle;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.netmelody.neoclassycle.graph.AtomicVertex;
import org.netmelody.neoclassycle.graph.Vertex;

public class PackageProcessorTest{
  private AtomicVertex _a;
  private AtomicVertex _b;
  private AtomicVertex _c;
  private AtomicVertex _d;
  private AtomicVertex _o;
  private AtomicVertex[] _graph;

@Before
  public void setUp() throws Exception
  {
    
    _a = new AtomicVertex(ClassAttributes.createClass("p.A", "s", 1));
    _b = new AtomicVertex(ClassAttributes.createClass("q.B", "r", 2));
    _c = new AtomicVertex(ClassAttributes.createClass("q.C", "s", 3));
    _d = new AtomicVertex(ClassAttributes.createClass("p.D", "s", 4));
    _o = new AtomicVertex(ClassAttributes.createClass("lang.Object", "s", 42));
    _o.setDefaultValueOfGraphVertexFlag(false);
    _a.addOutgoingArcTo(_o);
    _a.addOutgoingArcTo(_b);
    _b.addOutgoingArcTo(_o);
    _c.addOutgoingArcTo(_o);
    _c.addOutgoingArcTo(_d);
    _d.addOutgoingArcTo(_o);
    _graph = new AtomicVertex[] {_a, _b, _c, _d};
  }

  @Test
  public void testClassBExcluded()
  {
    _b.setDefaultValueOfGraphVertexFlag(false);
    PackageProcessor processor = new PackageProcessor();
    processor.deepSearchFirst(_graph);
    
    AtomicVertex[] packageGraph = processor.getGraph();
    assertEquals(2, packageGraph.length);
    
    assertEquals(true, packageGraph[0].isGraphVertex());
    assertEquals(true, packageGraph[1].isGraphVertex());
    
    AtomicVertex p = packageGraph[0];
    AtomicVertex q = packageGraph[1];
    if (!"p".equals(((PackageAttributes)p.getAttributes()).getName())) {
        p = packageGraph[1];
        q = packageGraph[0];
    }
    
    PackageAttributes attributesp = (PackageAttributes) p.getAttributes();
    assertEquals("p", attributesp.getName());
    assertEquals("s", attributesp.getSources());
    assertEquals(2, attributesp.getSize());
    assertEquals(1, p.getNumberOfOutgoingArcs());
    Vertex packageLang = p.getHeadVertex(0);
    assertEquals("lang", ((PackageAttributes) packageLang.getAttributes()).getName());
    
    PackageAttributes attributesq = (PackageAttributes) q.getAttributes();
    assertEquals("q", attributesq.getName());
    assertEquals("r, s", attributesq.getSources());
    assertEquals(2, attributesq.getSize());
    assertEquals(2, q.getNumberOfOutgoingArcs());
    assertSame(packageLang, q.getHeadVertex(0));
    assertSame(p, q.getHeadVertex(1));
  }
  
  @Test
  public void testClassDExcluded()
  {
    _d.setDefaultValueOfGraphVertexFlag(false);
    PackageProcessor processor = new PackageProcessor();
    processor.deepSearchFirst(_graph);
    
    AtomicVertex[] packageGraph = processor.getGraph();
    assertEquals(2, packageGraph.length);
    
    assertEquals(true, packageGraph[0].isGraphVertex());
    assertEquals(true, packageGraph[1].isGraphVertex());
    
    AtomicVertex p = packageGraph[0];
    AtomicVertex q = packageGraph[1];
    if (!"p".equals(((PackageAttributes)p.getAttributes()).getName())) {
        p = packageGraph[1];
        q = packageGraph[0];
    }
    
    PackageAttributes attributes = (PackageAttributes) q.getAttributes();
    assertEquals("q", attributes.getName());
    assertEquals(2, attributes.getSize());
    assertEquals(1, q.getNumberOfOutgoingArcs());
    Vertex packageLang = q.getHeadVertex(0);
    assertEquals("lang", 
            ((PackageAttributes) packageLang.getAttributes()).getName());
    
    attributes = (PackageAttributes) p.getAttributes();
    assertEquals("p", attributes.getName());
    assertEquals(2, attributes.getSize());
    assertEquals(2, p.getNumberOfOutgoingArcs());
    assertSame(packageLang, p.getHeadVertex(0));
    assertSame(q, p.getHeadVertex(1));
  }
  
  @Test
  public void testClassBAndCExcluded()
  {
    _b.setDefaultValueOfGraphVertexFlag(false);
    _c.setDefaultValueOfGraphVertexFlag(false);
    PackageProcessor processor = new PackageProcessor();
    processor.deepSearchFirst(_graph);
    
    AtomicVertex[] packageGraph = processor.getGraph();
    assertEquals(1, packageGraph.length);
    
    AtomicVertex p = packageGraph[0];
    assertEquals(true, p.isGraphVertex());
    PackageAttributes attributes = (PackageAttributes) p.getAttributes();
    assertEquals("p", attributes.getName());
    assertEquals(2, attributes.getSize());
    assertEquals(2, p.getNumberOfOutgoingArcs());
    AtomicVertex packageLang = (AtomicVertex) p.getHeadVertex(0);
    assertEquals("lang", 
            ((PackageAttributes) packageLang.getAttributes()).getName());
    assertEquals(false, packageLang.isGraphVertex());
    AtomicVertex q = (AtomicVertex) p.getHeadVertex(1);
    assertEquals("q", ((PackageAttributes) q.getAttributes()).getName());
    assertEquals(false, q.isGraphVertex());
  }
}
