/*
 * Copyright (c) 2003, Franz-Josef Elmer, All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 * - Redistributions of source code must retain the above copyright notice, 
 *   this list of conditions and the following disclaimer.
 * - Redistributions in binary form must reproduce the above copyright notice, 
 *   this list of conditions and the following disclaimer in the documentation 
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED 
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR 
 * OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package classycle;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;

import classycle.graph.AtomicVertex;
import classycle.graph.LongestWalkProcessor;
import classycle.graph.StrongComponent;
import classycle.graph.StrongComponentProcessor;
import classycle.renderer.AtomicVertexRenderer;
import classycle.renderer.PlainStrongComponentRenderer;
import classycle.renderer.StrongComponentRenderer;
import classycle.renderer.TemplateBasedClassRenderer;
import classycle.renderer.XMLClassRenderer;
import classycle.renderer.XMLStrongComponentRenderer;

/**
 * Main class of the Classycle tool. Runs on the command line and
 * produces a report.
 * 
 * @author Franz-Josef Elmer
 */
public class Analyser {
  private static final String VERSION = "0.91";
  private static final String INNER_CLASS = " and innerclasses";
  
  private final String[] _classFiles;
  private AtomicVertex[] _graph;
  private StrongComponent[] _components;
  private HashMap _layerMap;
  
  public Analyser(String[] classFiles) {
    _classFiles = classFiles;
  }
  
  public long createGraph() throws IOException {
    long time = System.currentTimeMillis();
    _graph = Parser.readClassFiles(_classFiles);
    return System.currentTimeMillis() - time;
  }
  
  public AtomicVertex[] getGraph() throws IOException {
    if (_graph == null) {
      createGraph();
    }
    return _graph;
  }
  
  public int getNumberOfExternalClasses() throws IOException {
    AtomicVertex[] graph = getGraph();
    HashSet usedClasses = new HashSet();
    int result = 0;
    for (int i = 0; i < graph.length; i++) {
      AtomicVertex vertex = graph[i];
      for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) {
        ClassAttributes attributes =
            (ClassAttributes) vertex.getHeadVertex(j).getAttributes();
        if (attributes.getType() == ClassAttributes.UNKNOWN) {
          if (!usedClasses.contains(attributes.getName())) {
            result++;
            usedClasses.add(attributes.getName());
          }
        }
      }
    }
    return result;
  }

  public long condenseGraph() throws IOException {
    long time = System.currentTimeMillis();
    StrongComponentProcessor processor = new StrongComponentProcessor();
    processor.deepSearchFirst(getGraph());
    _components = processor.getStrongComponents();
    return System.currentTimeMillis() - time;
  }

  public StrongComponent[] getCondensedGraph() throws IOException {
    if (_components == null) {
      condenseGraph();
    }
    return _components;
  }

  public long calculateLayerMap() throws IOException {
    long time = System.currentTimeMillis();
    StrongComponent[] components = getCondensedGraph();
    new LongestWalkProcessor().deepSearchFirst(components);
    _layerMap = new HashMap();
    for (int i = 0; i < components.length; i++) {
      StrongComponent component = components[i];
      Integer layer = new Integer(component.getLongestWalk());
      for (int j = 0, n = component.getNumberOfVertices(); j < n; j++) {
        _layerMap.put(component.getVertex(j), layer);
      }
    }
    return System.currentTimeMillis() - time;
  }

  public HashMap getLayerMap() throws IOException {
    if (_layerMap == null) {
      calculateLayerMap();
    }
    return _layerMap;
  }

  public static void main(String[] args) throws Exception {
    AnalyserCommandLine commandLine = new AnalyserCommandLine(args);
    if (!commandLine.isValid()) {
      System.out.println("Usage: java classycle.Analyser " 
                         + commandLine.getUsage());
      System.exit(0);
    }
    
    Analyser analyser = new Analyser(commandLine.getClassFiles());

    // Read and analyse class files
    System.out.println("============= Classycle V" + VERSION 
                       + " ============");
    System.out.println("========== by Franz-Josef Elmer ==========");
    System.out.print("read class files and create graph ... ");
    long duration = analyser.createGraph();
    System.out.println("done after " + duration + " ms: " 
                       + analyser.getGraph().length + " classes analysed.");

    // Condense graph
    System.out.print("condense graph ... ");
    duration = analyser.condenseGraph();
    System.out.println("done after " + duration + " ms: " 
                       + analyser.getCondensedGraph().length 
                       + " strong components found.");

    // Calculate layer 
    System.out.print("calculate layer indices ... ");
    duration = analyser.calculateLayerMap();
    System.out.println("done after " + duration + " ms.");

    // Create report(s)
    if (commandLine.getXmlFile() != null) {
      printXML(analyser, commandLine.getTitle(),
               new PrintWriter(new FileWriter(commandLine.getXmlFile())));
    }
    if (commandLine.getCsvFile() != null) {
      printCSV(analyser, 
               new PrintWriter(new FileWriter(commandLine.getCsvFile())));
    }
    if (commandLine.isRaw()) {
      printRaw(analyser);
    }
    if (commandLine.isCycles() || commandLine.isStrong()) {
      printComponents(analyser, commandLine.isCycles() ? 2 : 1);
    }
  }

  
  private static void printRaw(Analyser analyser) throws IOException {
    AtomicVertex[] graph = analyser.getGraph();
    for (int i = 0; i < graph.length; i++) {
      AtomicVertex vertex = graph[i];
      System.out.println(vertex.getAttributes());
      for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) {
        System.out.println("    " + vertex.getHeadVertex(j).getAttributes());
      }
    }
  }

  private static void printComponents(Analyser analyser, int minSize) 
                                                      throws IOException {
    StrongComponent[] components = analyser.getCondensedGraph();
    StrongComponentRenderer renderer = new PlainStrongComponentRenderer();
    for (int i = 0; i < components.length; i++) {
      StrongComponent component = components[i];
      if (component.getNumberOfVertices() >= minSize) {
        System.out.println(renderer.render(component));
      }
    }
  }

  private static void printXML(Analyser analyser, String title,
                               PrintWriter writer) throws IOException {
    StrongComponent[] components = analyser.getCondensedGraph();
    writer.println("<?xml version='1.0' encoding='UTF-8'?>");
    writer.println(
        "<?xml-stylesheet type='text/xsl' href='reportXMLtoHTML.xsl'?>");
    writer.println("<classycle title='" + title + "'>");
    writer.println("  <cycles>");
    StrongComponentRenderer sRenderer
        = new XMLStrongComponentRenderer(2);
    for (int i = 0; i < components.length; i++) {
      writer.print(sRenderer.render(components[i]));
    }
    writer.println("  </cycles>");
    writer.println("  <classes numberOfExternalClasses=\"" 
                   + analyser.getNumberOfExternalClasses() + "\">");
    render(analyser, new XMLClassRenderer(), writer);
    writer.println("  </classes>");
    writer.println("</classycle>");
    writer.close();
  }

  private static void render(Analyser analyser, AtomicVertexRenderer renderer, 
                             PrintWriter writer) throws IOException {
    AtomicVertex[] graph = analyser.getGraph();
    HashMap layerMap = analyser.getLayerMap();
    for (int i = 0; i < graph.length; i++) {
      Integer layerIndex = (Integer) layerMap.get(graph[i]);
      writer.print(renderer.render(graph[i], 
          layerIndex == null ? -1 : layerIndex.intValue()));
    }
  }

  private static void printCSV(Analyser analyser, PrintWriter writer) 
                                                      throws IOException {
    writer.print("class name,type,inner class,size,used by,");
    writer.println("uses internal classes,uses external classes");
    render(analyser, new TemplateBasedClassRenderer(CSV_TEMPLATE), writer);
    writer.close();
  }
  private static final String CSV_TEMPLATE  = "{0},{1},{3},{2},{4},{5},{6}";
} //class
