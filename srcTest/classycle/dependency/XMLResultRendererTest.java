/*
 * Copyright 2011 Franz-Josef Elmer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package classycle.dependency;

import java.util.Arrays;

import junit.framework.TestCase;
import classycle.PackageAttributes;
import classycle.graph.AtomicVertex;
import classycle.graph.StrongComponent;
import classycle.util.WildCardPattern;

public class XMLResultRendererTest extends TestCase
{
  public void testCyclesResultOk()
  {
    CyclesResult result = new CyclesResult("test statement", false);
    assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n" 
            + "<dependency-checking-results>\n"
            + "  <cycles statement='test statement' vertex-type='class'/>\n"
            + "</dependency-checking-results>\n", new XMLResultRenderer().render(result));
  }
  
  public void testCyclesResultWithCycles()
  {
    CyclesResult result = new CyclesResult("test statement", true);
    StrongComponent c1 = new StrongComponent();
    c1.addVertex(new AtomicVertex(new PackageAttributes("p1")));
    c1.addVertex(new AtomicVertex(new PackageAttributes("p2")));
    c1.calculateAttributes();
    result.addCycle(c1);
    StrongComponent c2 = new StrongComponent();
    c2.addVertex(new AtomicVertex(new PackageAttributes("p3")));
    c2.calculateAttributes();
    result.addCycle(c2);
    assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n" 
            + "<dependency-checking-results>\n"
            + "  <cycles statement='test statement' vertex-type='package'>\n"
            + "    <cycle name='p2 et al.'>\n" 
            + "      <class>p2</class>\n"
            + "      <class>p1</class>\n" 
            + "    </cycle>\n" 
            + "    <cycle name='p3'>\n"
            + "      <class>p3</class>\n" 
            + "    </cycle>\n" 
            + "  </cycles>\n"
            + "</dependency-checking-results>\n", new XMLResultRenderer().render(result));
  }
  
  public void testDependencyResult()
  {
    AtomicVertex a = new AtomicVertex(new PackageAttributes("a"));
    AtomicVertex b = new AtomicVertex(new PackageAttributes("b"));
    AtomicVertex c1 = new AtomicVertex(new PackageAttributes("c1"));
    AtomicVertex c2 = new AtomicVertex(new PackageAttributes("c2"));
    AtomicVertex c3 = new AtomicVertex(new PackageAttributes("c3"));
    AtomicVertex d = new AtomicVertex(new PackageAttributes("d"));
    a.addOutgoingArcTo(b);
    b.addOutgoingArcTo(c1);
    b.addOutgoingArcTo(c2);
    a.addOutgoingArcTo(c3);
    d.addOutgoingArcTo(a);
    c1.addOutgoingArcTo(a);
    AtomicVertex[] vertices = Arrays.asList(a, b, c1, c2, c3, d).toArray(new AtomicVertex[0]);
    WildCardPattern startSet = new WildCardPattern("a*");
    WildCardPattern finalSet = new WildCardPattern("c*");
    DependencyResult result = new DependencyResult(startSet, finalSet, "the statement", vertices);
    assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n" 
            + "<dependency-checking-results>\n"
            + "  <unexpected-dependencies statement='the statement'>\n"
            + "    <node name='a'>\n" 
            + "      <node name='b'>\n"
            + "        <node name='c1'/>\n" 
            + "        <node name='c2'/>\n" 
            + "      </node>\n"
            + "      <node name='c3'/>\n" 
            + "    </node>\n" 
            + "  </unexpected-dependencies>\n"
            + "</dependency-checking-results>\n", new XMLResultRenderer().render(result));
  }
  
  public void testResultContainerAndTextResult()
  {
    ResultContainer c1 = new ResultContainer();
    c1.add(new TextResult("hello world"));
    ResultContainer c2 = new ResultContainer();
    c2.add(new TextResult("Invalid set", false));
    c2.add(new TextResult("Unknown", false));
    c2.add(new TextResult(""));
    c1.add(c2);
    assertEquals("<?xml version=\'1.0' encoding='UTF-8'?>\n" 
            + "<dependency-checking-results>\n"
            + "  <info>hello world</info>\n" 
            + "  <checking-error>Invalid set</checking-error>\n"
            + "  <checking-error>Unknown</checking-error>\n" 
            + "</dependency-checking-results>\n", new XMLResultRenderer().render(c1));
  }
}
