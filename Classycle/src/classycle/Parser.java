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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import classycle.classfile.ClassConstant;
import classycle.classfile.Constant;
import classycle.classfile.UTF8Constant;
import classycle.graph.AtomicVertex;

/**
 *  Utility methods for parsing class files and creating directed graphs.
 *  The nodes of the graph a classes. The initial vertex of an edge is the
 *  class which uses the class specified by the terminal vertex.
 *
 *  @author Franz-Josef Elmer
 */
public class Parser
{

  private static final int ACC_INTERFACE = 0x200,
                           ACC_ABSTRACT = 0x400;
  private static class UnresolvedNode implements Comparable
  {
    ClassAttributes attributes;
    ArrayList nodes = new ArrayList();

    public int compareTo(Object o)
    {
      return attributes.getName().compareTo(
                              ((UnresolvedNode) o).attributes.getName());
    }
  }

  private Parser() {}

  /**
   *  Reads the specified class files and creates a directed graph where each
   *  vertex represents a class. The head vertex of an arc is a class which is
   *  used by the tail vertex of the arc.
   *  The elements of <tt>classFiles</tt> are file names (relative to the
   *  working directory) which are interpreted depending on its file type as
   *  <ul>
   *    <li>name of a class file (file type <tt>.class</tt>)
   *    <li>name of a folder containing class files
   *    <li>name of a zip file containing class file (file type <tt>.zip</tt>)
   *    <li>name of a jar file (file type <tt>.jar</tt>)
   *  </ul>
   *  Folders and zip/jar files are searched recursively for class files.
   *  @param classFiles Array of file names.
   *  @return directed graph.
   */
  public static AtomicVertex[] readClassFiles(String[] classFiles)
                                                        throws IOException
  {
    ArrayList unresolvedNodes = new ArrayList();
    for (int i = 0; i < classFiles.length; i++)
    {
      File file = new File(classFiles[i]);
      if (file.isDirectory() || file.getName().endsWith(".class"))
      {
        analyseClassFile(file, unresolvedNodes);
      }
      else if (file.getName().endsWith(".zip")
               || file.getName().endsWith(".jar"))
      {
        analyseClassFiles(new ZipFile(file.getAbsoluteFile()),
                          unresolvedNodes);
      }
      else
      {
        throw new IOException(classFiles[i] + " is an invalid file.");
      }
    }
    UnresolvedNode[] nodes = new UnresolvedNode[unresolvedNodes.size()];
    nodes = (UnresolvedNode[]) unresolvedNodes.toArray(nodes);
    Arrays.sort(nodes);
    return createGraph(nodes);
  }

  private static void analyseClassFile(File file, ArrayList unresolvedNodes)
                                                            throws IOException
  {
    if (file.isDirectory())
    {
      String[] files = file.list();
      for (int i = 0; i < files.length; i++)
      {
        File child = new File(file, files[i]);
        if (child.isDirectory() || files[i].endsWith(".class"))
        {
          analyseClassFile(child, unresolvedNodes);
        }
      }
    }
    else
    {
      unresolvedNodes.add(extractNode(file));
    }
  }

  private static UnresolvedNode extractNode(File file) throws IOException
  {
    InputStream stream = null;
    UnresolvedNode result = null;
    try
    {
      stream = new FileInputStream(file);
      result = Parser.createNode(stream, file.length());
    }
    catch (IOException e)
    {
      throw e;
    }
    finally
    {
      try
      {
        stream.close();
      }
      catch (IOException e) {}
    }
    return result;
  }

  private static void analyseClassFiles(ZipFile zipFile,
                                        ArrayList unresolvedNodes)
                                                            throws IOException
  {
    Enumeration entries = zipFile.entries();
    while (entries.hasMoreElements())
    {
      ZipEntry entry = (ZipEntry) entries.nextElement();
      if (!entry.isDirectory() && entry.getName().endsWith(".class"))
      {
        InputStream stream = zipFile.getInputStream(entry);
        unresolvedNodes.add(Parser.createNode(stream, entry.getSize()));
      }
    }
  }

  /**
   *  Creates a new node with unresolved references.
   *  @param stream A just opended byte stream of a class file.
   *         If this method finishes succefully the internal pointer of the
   *         stream will point onto the superclass index.
   *  @param size Number of bytes of the class file.
   *  @return a node with unresolved link of all classes used by the analysed
   *         class.
   */
  private static UnresolvedNode createNode(InputStream stream, long size)
                                                          throws IOException
  {
    // Reads constant pool, accessFlags, and class name
    DataInputStream dataStream = new DataInputStream(stream);
    Constant[] pool = Constant.extractConstantPool(dataStream);
    int accessFlags = dataStream.readUnsignedShort();
    String name
        = ((ClassConstant) pool[dataStream.readUnsignedShort()]).getName();
    ClassAttributes attributes = null;
    if ((accessFlags & ACC_INTERFACE) != 0)
    {
      attributes = ClassAttributes.createInterface(name, size);
    }
    else
    {
      if ((accessFlags & ACC_ABSTRACT) != 0)
      {
        attributes = ClassAttributes.createAbstractClass(name, size);
      }
      else
      {
        attributes = ClassAttributes.createClass(name, size);
      }
    }

    // Creates a new node with unresolved references
    UnresolvedNode node = new UnresolvedNode();
    node.attributes = attributes;
    for (int i = 0; i < pool.length; i++)
    {
      if (pool[i] instanceof ClassConstant)
      {
        ClassConstant cc = (ClassConstant) pool[i];
        if (!cc.getName().equals(name))
        {
          node.nodes.add(cc.getName());
        }
      }
      else if (pool[i] instanceof UTF8Constant)
      {
        String str = ((UTF8Constant) pool[i]).getString();
        while (str.startsWith("["))
        {
          str = str.substring(1);
        }
        if (str.startsWith("L") && str.endsWith(";"))
        {
          str = str.substring(1, str.length() - 1).replace('/', '.');
          if (!str.equals(name))
          {
            node.nodes.add(str);
          }
        }
      }
    }
    return node;
  }

  /**
   *  Creates a graph from the bunch of unresolved nodes.
   *  @param unresolvedNodes All nodes with unresolved references.
   *  @return an array of length <tt>unresolvedNodes.size()</tt> with all
   *          unresolved nodes transformed into <tt>Node</tt> objects
   *          with appropriated links. External nodes are created and linked
   *          but not added to the result array.
   */
  private static AtomicVertex[] createGraph(UnresolvedNode[] unresolvedNodes)
  {
    // Creates an array with all vertices having the right attributes
    // but initially no arcs.
    AtomicVertex[] result = new AtomicVertex[unresolvedNodes.length];
    Hashtable map = new Hashtable();
    for (int i = 0; i < result.length; i++)
    {
      ClassAttributes attributes = unresolvedNodes[i].attributes;
      AtomicVertex vertex = new AtomicVertex(attributes);
      map.put(attributes.getName(), vertex);
      result[i] = vertex;
    }

    // Adds the arcs to the vertices
    for (int i = 0; i < result.length; i++)
    {
      UnresolvedNode node = unresolvedNodes[i];
      AtomicVertex vertex = result[i];
      for (int j = 0, m = node.nodes.size(); j < m; j++)
      {
        String name = (String) node.nodes.get(j);
        AtomicVertex head  = (AtomicVertex) map.get(name);
        if (head == null)
        {
          // external node created and added to the hash map
          head = new AtomicVertex(ClassAttributes.createUnknownClass(name, 0));
          map.put(name, head);
        }
        vertex.addOutgoingArcTo(head);
      }
    }

    return result;
  }


} //class