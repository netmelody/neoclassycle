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
import java.io.PrintWriter;

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

public class Analyser
{
  private static final String INNER_CLASS = " and innerclasses";

  public static void main(String[] args) throws Exception
  {
    if (args.length == 0)
    {
      System.out.println(
          "Usage: java classycle.Analyser [-raw] [-cycles|-strong] "
          + "[-xmlFile=<file>] [-csvFile=<file>] [-title=<title>]"
          + "<class files, jar files, zip files, or folders>");
    }
    boolean raw = false;
    boolean cycles = false;
    boolean strong = false;
    String title = null;
    String xmlFile = null;
    String csvFile = null;
    int index = 0;
    for (;index < args.length && args[index].charAt(0) == '-'; index++)
    {
      if (args[index].equals("-raw"))
      {
        raw = true;
      }
      else if (args[index].equals("-cycles"))
      {
        cycles = true;
      }
      else if (args[index].equals("-strong"))
      {
        strong = true;
      }
      else if (args[index].startsWith("-title="))
      {
        title = args[index].substring("-title=".length());
      }
      else if (args[index].startsWith("-xmlFile="))
      {
        xmlFile = args[index].substring("-xmlFile=".length());
      }
      else if (args[index].startsWith("-csvFile="))
      {
        csvFile = args[index].substring("-csvFile=".length());
      }
    }
    String[] classFiles = new String[args.length - index];
    System.arraycopy(args, index, classFiles, 0, classFiles.length);
    if (title == null && classFiles.length > 0)
    {
      title = classFiles[0];
    }

    // Read and analyse class files
    System.out.println("============= Classycle V0.1 =============");
    System.out.println("========== by Franz-Josef Elmer ==========");
    System.out.print("read and analyse class files ... ");
    long time = System.currentTimeMillis();
    AtomicVertex[] graph = Parser.readClassFiles(classFiles);
    System.out.println("done after " + (System.currentTimeMillis() - time)
                       + " ms: " + graph.length + " classes analysed.");

    // Extract strong components
    System.out.print("extract strong components ... ");
    time = System.currentTimeMillis();
    StrongComponentProcessor processor = new StrongComponentProcessor();
    processor.deepSearchFirst(graph);
    StrongComponent[] components = processor.getStrongComponents();
    System.out.println("done after " + (System.currentTimeMillis() - time)
                       + " ms: " + components.length + " components found.");

    // Find longest walks
    System.out.print("find longest walks ... ");
    time = System.currentTimeMillis();
    new LongestWalkProcessor().deepSearchFirst(components);
    System.out.println("done after " + (System.currentTimeMillis() - time)
                       + " ms.");

    if (xmlFile != null)
    {
      printXML(graph, components, title,
               new PrintWriter(new FileWriter(xmlFile)));
    }
    if (csvFile != null)
    {
      printCSV(graph, new PrintWriter(new FileWriter(csvFile)));
    }

    if (raw)
    {
      printRaw(graph);
    }
    if (cycles || strong)
    {
      printComponents(components, cycles ? 2 : 1);
    }
  }


  private static void printRaw(AtomicVertex[] graph)
  {
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


  private static void printComponents(StrongComponent[] components,
                                      int minSize)
  {
    StrongComponentRenderer renderer = new PlainStrongComponentRenderer();
    for (int i = 0; i < components.length; i++)
    {
      StrongComponent component = components[i];
      if (component.getNumberOfVertices() >= minSize)
      {
        System.out.println(renderer.render(component));
      }
      /*
      // cyclic component
      System.out.println("strong component " + i + " is cyclic: "
                         + component.getNumberOfVertices() + " vertices");
      for (int j = 0, n = component.getNumberOfVertices(); j < n; j++)
      {
        System.out.println("  " + component.getVertex(j).getAttributes());
      }
      */
    }
  }

  private static void printXML(AtomicVertex[] graph,
                               StrongComponent[] components, String title,
                               PrintWriter writer)
  {
    writer.println("<?xml version='1.0' encoding='UTF-8'?>");
    writer.println("<classycle title='" + title + "'>");
    writer.println("  <cycles>");
    StrongComponentRenderer sRenderer
        = new XMLStrongComponentRenderer(2);
    for (int i = 0; i < components.length; i++)
    {
      writer.print(sRenderer.render(components[i]));
    }
    writer.println("  </cycles>");
    writer.println("  <classes>");
    AtomicVertexRenderer aRenderer = new XMLClassRenderer();
    for (int i = 0; i < graph.length; i++)
    {
      writer.print(aRenderer.render(graph[i]));
    }
    writer.println("  </classes>");
    writer.println("</classycle>");
    writer.close();
  }
  private static final String XML_TEMPLATE_CLASSES
      = "    <class name=\"{0}\" type=\"{1}\" innerClass=\"{3}\" size=\"{2}\" "
        + "usedBy=\"{4}\" usesInternal=\"{5}\" usesExternal=\"{6}\"/>\n";


  private static void printCSV(AtomicVertex[] graph, PrintWriter writer)
  {
    writer.print("class name,type,inner class,size,used by,");
    writer.println("uses internal classes,uses external classes");
    AtomicVertexRenderer renderer
        = new TemplateBasedClassRenderer(CSV_TEMPLATE);
    for (int i = 0; i < graph.length; i++)
    {
     writer.println(renderer.render(graph[i]));
    }
    writer.close();
  }
  private static final String CSV_TEMPLATE  = "{0},{1},{3},{2},{4},{5},{6}";
} //class