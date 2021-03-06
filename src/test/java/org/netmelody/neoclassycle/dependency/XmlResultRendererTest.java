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
package org.netmelody.neoclassycle.dependency;

import java.util.Arrays;

import org.junit.Test;
import org.netmelody.neoclassycle.PackageAttributes;
import org.netmelody.neoclassycle.graph.AtomicVertex;
import org.netmelody.neoclassycle.graph.StrongComponent;
import org.netmelody.neoclassycle.util.WildCardPattern;

import static org.junit.Assert.assertEquals;

public final class XmlResultRendererTest {
    @Test
    public void testCyclesResultOk() {
        final CyclesResult result = new CyclesResult("test statement", false);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n" + "<dependency-checking-results>\n"
                + "  <cycles statement='test statement' vertex-type='class'/>\n" + "</dependency-checking-results>\n",
                new XmlResultRenderer().render(result));
    }

    @Test
    public void testCyclesResultWithCycles() {
        final CyclesResult result = new CyclesResult("test statement", true);
        final StrongComponent c1 = new StrongComponent();
        c1.addVertex(new AtomicVertex(new PackageAttributes("p1")));
        c1.addVertex(new AtomicVertex(new PackageAttributes("p2")));
        c1.calculateAttributes();
        result.addCycle(c1);
        final StrongComponent c2 = new StrongComponent();
        c2.addVertex(new AtomicVertex(new PackageAttributes("p3")));
        c2.calculateAttributes();
        result.addCycle(c2);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n" + "<dependency-checking-results>\n"
                + "  <cycles statement='test statement' vertex-type='package'>\n" + "    <cycle name='p2 et al.'>\n"
                + "      <class>p2</class>\n" + "      <class>p1</class>\n" + "    </cycle>\n" + "    <cycle name='p3'>\n"
                + "      <class>p3</class>\n" + "    </cycle>\n" + "  </cycles>\n" + "</dependency-checking-results>\n",
                new XmlResultRenderer().render(result));
    }

    @Test
    public void testDependencyResult() {
        final AtomicVertex a = new AtomicVertex(new PackageAttributes("a"));
        final AtomicVertex b = new AtomicVertex(new PackageAttributes("b"));
        final AtomicVertex c1 = new AtomicVertex(new PackageAttributes("c1"));
        final AtomicVertex c2 = new AtomicVertex(new PackageAttributes("c2"));
        final AtomicVertex c3 = new AtomicVertex(new PackageAttributes("c3"));
        final AtomicVertex d = new AtomicVertex(new PackageAttributes("d"));
        a.addOutgoingArcTo(b);
        b.addOutgoingArcTo(c1);
        b.addOutgoingArcTo(c2);
        a.addOutgoingArcTo(c3);
        d.addOutgoingArcTo(a);
        c1.addOutgoingArcTo(a);
        final AtomicVertex[] vertices = Arrays.asList(a, b, c1, c2, c3, d).toArray(new AtomicVertex[0]);
        final WildCardPattern startSet = new WildCardPattern("a*");
        final WildCardPattern finalSet = new WildCardPattern("c*");
        final DependencyResult result = new DependencyResult(startSet, finalSet, "the statement", vertices);
        assertEquals("<?xml version='1.0' encoding='UTF-8'?>\n" + "<dependency-checking-results>\n"
                + "  <unexpected-dependencies statement='the statement'>\n" + "    <node name='a'>\n" + "      <node name='b'>\n"
                + "        <node name='c1'/>\n" + "        <node name='c2'/>\n" + "      </node>\n" + "      <node name='c3'/>\n"
                + "    </node>\n" + "  </unexpected-dependencies>\n" + "</dependency-checking-results>\n",
                new XmlResultRenderer().render(result));
    }

    @Test
    public void testResultContainerAndTextResult() {
        final ResultContainer c1 = new ResultContainer();
        c1.add(new TextResult("hello world"));
        final ResultContainer c2 = new ResultContainer();
        c2.add(new TextResult("Invalid set", false));
        c2.add(new TextResult("Unknown", false));
        c2.add(new TextResult(""));
        c1.add(c2);
        assertEquals("<?xml version=\'1.0' encoding='UTF-8'?>\n" + "<dependency-checking-results>\n" + "  <info>hello world</info>\n"
                + "  <checking-error>Invalid set</checking-error>\n" + "  <checking-error>Unknown</checking-error>\n"
                + "</dependency-checking-results>\n", new XmlResultRenderer().render(c1));
    }
}
