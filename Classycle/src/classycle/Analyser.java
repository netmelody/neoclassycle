/*
 * Copyright (c) 2003-2004, Franz-Josef Elmer, All rights reserved.
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
import classycle.graph.StrongComponent;
import classycle.graph.StrongComponentAnalyser;
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
public class Analyser 
{
  private static final String VERSION = "0.9999";
  
  private final String[] _classFiles;
  private StrongComponentAnalyser _classAnalyser;
  private StrongComponentAnalyser _packageAnalyser;
  
  public Analyser(String[] classFiles) 
  {
    _classFiles = classFiles;
  }
  
  public long createClassGraph() throws IOException 
  {
    long time = System.currentTimeMillis();
    _classAnalyser 
        = new StrongComponentAnalyser(Parser.readClassFiles(_classFiles));
    return System.currentTimeMillis() - time;
  }
  
  public AtomicVertex[] getClassGraph() 
  {
    return _classAnalyser.getGraph();
  }
  
  public int getNumberOfExternalClasses()
  {
    AtomicVertex[] graph = getClassGraph();
    HashSet usedClasses = new HashSet();
    int result = 0;
    for (int i = 0; i < graph.length; i++) 
    {
      AtomicVertex vertex = graph[i];
      for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) 
      {
        ClassAttributes attributes =
            (ClassAttributes) vertex.getHeadVertex(j).getAttributes();
        if (attributes.getType() == ClassAttributes.UNKNOWN) 
        {
          if (!usedClasses.contains(attributes.getName())) 
          {
            result++;
            usedClasses.add(attributes.getName());
          }
        }
      }
    }
    return result;
  }

  private long condenseClassGraph() 
  {
    long time = System.currentTimeMillis();
    _classAnalyser.getCondensedGraph();
    return System.currentTimeMillis() - time;
  }

  public StrongComponent[] getCondensedClassGraph() 
  {
    return _classAnalyser.getCondensedGraph();
  }

  private long calculateClassLayerMap()
  {
    long time = System.currentTimeMillis();
    _classAnalyser.getLayerMap();
    return System.currentTimeMillis() - time;
  }

  public HashMap getClassLayerMap()
  {
    return _classAnalyser.getLayerMap();
  }

  private long createPackageGraph() 
  {
    long time = System.currentTimeMillis();
    PackageProcessor processor = new PackageProcessor();
    processor.deepSearchFirst(_classAnalyser.getGraph());
    _packageAnalyser = new StrongComponentAnalyser(processor.getGraph());
    return System.currentTimeMillis() - time;
  }
  
  public AtomicVertex[] getPackageGraph() 
  {
    return _packageAnalyser.getGraph();
  }
  
  private long condensePackageGraph() 
  {
    long time = System.currentTimeMillis();
    _packageAnalyser.getCondensedGraph();
    return System.currentTimeMillis() - time;
  }

  public StrongComponent[] getCondensedPackageGraph() 
  {
    return _packageAnalyser.getCondensedGraph();
  }

  private long calculatePackageLayerMap()
  {
    long time = System.currentTimeMillis();
    _packageAnalyser.getLayerMap();
    return System.currentTimeMillis() - time;
  }

  public HashMap getPackageLayerMap()
  {
    return _packageAnalyser.getLayerMap();
  }

  public static void main(String[] args) throws Exception {
    AnalyserCommandLine commandLine = new AnalyserCommandLine(args);
    if (!commandLine.isValid()) {
      System.out.println("Usage: java classycle.Analyser " 
                         + commandLine.getUsage());
      System.exit(0);
    }
    
    Analyser analyser = new Analyser(commandLine.getClassFiles());
    readAndAnalyse(commandLine, analyser);

    // Create report(s)
    if (commandLine.getXmlFile() != null) 
    {
      printXML(analyser, commandLine.getTitle(), commandLine.isPackagesOnly(),
               new PrintWriter(new FileWriter(commandLine.getXmlFile())));
    }
    if (commandLine.getCsvFile() != null) 
    {
      printCSV(analyser, 
               new PrintWriter(new FileWriter(commandLine.getCsvFile())));
    }
    if (commandLine.isRaw()) 
    {
      printRaw(analyser);
    }
    if (commandLine.isCycles() || commandLine.isStrong()) 
    {
      printComponents(analyser, commandLine.isCycles() ? 2 : 1);
    }
  }

  private static void readAndAnalyse(AnalyserCommandLine commandLine,
                                     Analyser analyser) throws IOException
  {
    System.out.println("============= Classycle V" + VERSION 
                       + " ============");
    System.out.println("=========== by Franz-Josef Elmer ===========");
    System.out.print("read class files and create class graph ... ");
    long duration = analyser.createClassGraph();
    System.out.println("done after " + duration + " ms: " 
                       + analyser.getClassGraph().length + " classes analysed.");
    
    if (!commandLine.isPackagesOnly())
    {
      // Condense class graph
      System.out.print("condense class graph ... ");
      duration = analyser.condenseClassGraph();
      System.out.println("done after " + duration + " ms: " 
                         + analyser.getCondensedClassGraph().length 
                         + " strong components found.");
    
      // Calculate class layer 
      System.out.print("calculate class layer indices ... ");
      duration = analyser.calculateClassLayerMap();
      System.out.println("done after " + duration + " ms.");
    }
    System.out.print("create package graph ... ");
    duration = analyser.createPackageGraph();
    System.out.println("done after " + duration + " ms: " 
                       + analyser.getPackageGraph().length + " packages.");
    // Condense package graph
    System.out.print("condense package graph ... ");
    duration = analyser.condensePackageGraph();
    System.out.println("done after " + duration + " ms: " 
                       + analyser.getCondensedPackageGraph().length 
                       + " strong components found.");
    // Calculate package layer 
    System.out.print("calculate package layer indices ... ");
    duration = analyser.calculatePackageLayerMap();
    System.out.println("done after " + duration + " ms.");
  }

  
  private static void printRaw(Analyser analyser)
  {
    AtomicVertex[] graph = analyser.getClassGraph();
    for (int i = 0; i < graph.length; i++) 
    {
      AtomicVertex vertex = graph[i];
      System.out.println(vertex.getAttributes());
      for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) 
      {
        System.out.println("    " + vertex.getHeadVertex(j).getAttributes());
      }
    }
  }

  private static void printComponents(Analyser analyser, int minSize)
  {
    StrongComponent[] components = analyser.getCondensedClassGraph();
    StrongComponentRenderer renderer = new PlainStrongComponentRenderer();
    for (int i = 0; i < components.length; i++) 
    {
      StrongComponent component = components[i];
      if (component.getNumberOfVertices() >= minSize) 
      {
        System.out.println(renderer.render(component));
      }
    }
  }

  private static void printXML(Analyser analyser, String title,
                               boolean packagesOnly, PrintWriter writer) 
                      throws IOException
  {
    writer.println("<?xml version='1.0' encoding='UTF-8'?>");
    writer.println(
        "<?xml-stylesheet type='text/xsl' href='reportXMLtoHTML.xsl'?>");
    writer.println("<classycle title='" + title + "'>");
    if (!packagesOnly)
    {
      StrongComponent[] components = analyser.getCondensedClassGraph();
      writer.println("  <cycles>");
      StrongComponentRenderer sRenderer
          = new XMLStrongComponentRenderer(2);
      for (int i = 0; i < components.length; i++) 
      {
        writer.print(sRenderer.render(components[i]));
      }
      writer.println("  </cycles>");
      writer.println("  <classes numberOfExternalClasses=\"" 
                     + analyser.getNumberOfExternalClasses() + "\">");
      AtomicVertex[] graph = analyser.getClassGraph();
      HashMap layerMap = analyser.getClassLayerMap();
      render(graph, layerMap, new XMLClassRenderer(), writer);
      writer.println("  </classes>");
    }
    StrongComponent[] components = analyser.getCondensedPackageGraph();
    writer.println("  <packageCycles>");
    StrongComponentRenderer sRenderer
        = new XMLPackageStrongComponentRenderer(2);
    for (int i = 0; i < components.length; i++) 
    {
      writer.print(sRenderer.render(components[i]));
    }
    writer.println("  </packageCycles>");
    writer.println("  <packages>");
    AtomicVertex[] graph = analyser.getPackageGraph();
    HashMap layerMap = analyser.getPackageLayerMap();
    render(graph, layerMap, new XMLPackageRenderer(), writer);
    writer.println("  </packages>");
    
    writer.println("</classycle>");
    writer.close();
  }

  private static void render(AtomicVertex[] graph, HashMap layerMap,
                             AtomicVertexRenderer renderer, 
                             PrintWriter writer) throws IOException 
 {
    for (int i = 0; i < graph.length; i++) 
    {
      Integer layerIndex = (Integer) layerMap.get(graph[i]);
      writer.print(renderer.render(graph[i], 
          layerIndex == null ? -1 : layerIndex.intValue()));
    }
  }

  private static void printCSV(Analyser analyser, PrintWriter writer) 
                                                      throws IOException 
  {
    writer.println("class name,type,inner class,size,used by,"
        + "uses internal classes,uses external classes,layer index");
    render(analyser.getClassGraph(), analyser.getClassLayerMap(),
           new TemplateBasedClassRenderer(CSV_TEMPLATE), writer);
    writer.close();
  }
  private static final String CSV_TEMPLATE  = "{0},{1},{3},{2},{4},{5},{6},{7}\n";
} //class
