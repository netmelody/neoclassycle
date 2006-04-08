/*
 * Copyright (c) 2003-2006, Franz-Josef Elmer, All rights reserved.
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
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import classycle.classfile.ClassConstant;
import classycle.classfile.Constant;
import classycle.classfile.StringConstant;
import classycle.classfile.UTF8Constant;
import classycle.graph.AtomicVertex;
import classycle.util.StringPattern;
import classycle.util.TrueStringPattern;

/**
 *  Utility methods for parsing class files and creating directed graphs.
 *  The nodes of the graph are classes. The initial vertex of an edge is the
 *  class which uses the class specified by the terminal vertex.
 *
 *  @author Franz-Josef Elmer
 */
public class Parser 
{
  private static final int ACC_INTERFACE = 0x200, ACC_ABSTRACT = 0x400;
  private static final String[] ZIP_FILE_TYPES 
      = new String[] {".zip", ".jar", ".war", ".ear"}; 

  private static class UnresolvedNode implements Comparable 
  {
    ClassAttributes attributes;
    ArrayList nodes = new ArrayList();

    public UnresolvedNode() {}

    public int compareTo(Object obj) 
    {
      return attributes.getName().compareTo(
                ((UnresolvedNode) obj).attributes.getName());
    }
    
    public boolean isMatchedBy(StringPattern pattern)
    {
      return pattern.matches(attributes.getName());
    }
  }

  /** Private constructor to prohibit instanciation. */
  private Parser() {
  }
  
  /**
   * Reads and parses class files and creates a direct graph. Short-cut of 
   * <tt>readClassFiles(classFiles, new {@link TrueStringPattern}(), 
   * null, false);</tt>
   */
  public static AtomicVertex[] readClassFiles(String[] classFiles)
                               throws IOException 
  {
    return readClassFiles(classFiles, new TrueStringPattern(), null, false);
  }

  /**
   *  Reads the specified class files and creates a directed graph where each
   *  vertex represents a class. The head vertex of an arc is a class which is
   *  used by the tail vertex of the arc.
   *  The elements of <tt>classFiles</tt> are file names (relative to the
   *  working directory) which are interpreted depending on its file type as
   *  <ul>
   *    <li>name of a class file (file type <tt>.class</tt>)
   *    <li>name of a folder containing class files
   *    <li>name of a file of type <code>.zip</code>, <code>.jar</code>,
   *        <code>.war</code>, or <code>.ear</code>
   *        containing class file
   *  </ul>
   *  Folders and zip/jar/war/ear files are searched recursively 
   *  for class files.
   *  @param classFiles Array of file names.
   *  @param pattern Pattern fully qualified class names have to match in order
   *                 to be added to the graph. Otherwise they count as
   *                 'external'.
   *  @param reflectionPattern Pattern ordinary string constants of a class 
   *                 file have to fullfill in order to be handled as
   *                 class references. In addition they have to be 
   *                 syntactically valid fully qualified class names. If
   *                 <tt>null</tt> ordinary string constants will not be 
   *                 checked.
   *  @param mergeInnerClasses If <code>true</code> 
   *                 merge inner classes with its outer class
   *  @return directed graph.
   */
  public static AtomicVertex[] readClassFiles(String[] classFiles, 
                                              StringPattern pattern,
                                              StringPattern reflectionPattern, 
                                              boolean mergeInnerClasses)
                               throws IOException 
  {
    ArrayList unresolvedNodes = new ArrayList();
    for (int i = 0; i < classFiles.length; i++) 
    {
      File file = new File(classFiles[i]);
      if (file.isDirectory() || file.getName().endsWith(".class")) 
      {
        analyseClassFile(file, unresolvedNodes, reflectionPattern);
      } else if (isZipFile(file)) 
      {
        analyseClassFiles(new ZipFile(file.getAbsoluteFile()), 
                          unresolvedNodes, 
                          reflectionPattern);
      } else 
      {
        throw new IOException(classFiles[i] + " is an invalid file.");
      }
    }
    ArrayList filteredNodes = new ArrayList();
    for (int i = 0, n = unresolvedNodes.size(); i < n; i++)
    {
      UnresolvedNode node = (UnresolvedNode) unresolvedNodes.get(i);
      if (node.isMatchedBy(pattern))
      {
        filteredNodes.add(node);
      }
    }
    UnresolvedNode[] nodes = new UnresolvedNode[filteredNodes.size()];
    nodes = (UnresolvedNode[]) filteredNodes.toArray(nodes);
    Arrays.sort(nodes);
    return createGraph(nodes, mergeInnerClasses);
  }

  private static boolean isZipFile(File file) 
  {
    boolean result = false;
    String name = file.getName();
    for (int i = 0; i < ZIP_FILE_TYPES.length; i++) 
    {
      if (name.endsWith(ZIP_FILE_TYPES[i]))
      {
        result = true;
        break;
      }
    }
    return result;
  }

  private static void analyseClassFile(File file, ArrayList unresolvedNodes, 
                                       StringPattern reflectionPattern)
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
          analyseClassFile(child, unresolvedNodes, reflectionPattern);
        }
      }
    } else 
    {
      unresolvedNodes.add(extractNode(file, reflectionPattern));
    }
  }

  private static UnresolvedNode extractNode(File file, 
                                            StringPattern reflectionPattern) 
                                throws IOException 
  {
    InputStream stream = null;
    UnresolvedNode result = null;
    try 
    {
      stream = new FileInputStream(file);
      result = Parser.createNode(stream, (int) file.length(), 
                                 reflectionPattern);
    } finally 
    {
      try 
      {
        stream.close();
      } catch (IOException e) {}
    }
    return result;
  }

  private static void analyseClassFiles(ZipFile zipFile,
                                        ArrayList unresolvedNodes,
                                        StringPattern reflectionPattern)
                      throws IOException 
  {
    Enumeration entries = zipFile.entries();
    while (entries.hasMoreElements()) 
    {
      ZipEntry entry = (ZipEntry) entries.nextElement();
      if (!entry.isDirectory() && entry.getName().endsWith(".class")) 
      {
        InputStream stream = zipFile.getInputStream(entry);
        unresolvedNodes.add(Parser.createNode(stream, (int) entry.getSize(), 
                                              reflectionPattern));
      }
    }
  }

  /**
   *  Creates a new node with unresolved references.
   *  @param stream A just opended byte stream of a class file.
   *         If this method finishes succefully the internal pointer of the
   *         stream will point onto the superclass index.
   *  @param size Number of bytes of the class file.
   *  @param reflectionPattern Pattern used to check whether a
   *         {@link StringConstant} refer to a class. Can be <tt>null</tt>.
   *  @return a node with unresolved link of all classes used by the analysed
   *         class.
   */
  private static UnresolvedNode createNode(InputStream stream, int size,
                                           StringPattern reflectionPattern)
                                throws IOException 
  {
    // Reads constant pool, accessFlags, and class name
    DataInputStream dataStream = new DataInputStream(stream);
    Constant[] pool = Constant.extractConstantPool(dataStream);
    int accessFlags = dataStream.readUnsignedShort();
    String name =
        ((ClassConstant) pool[dataStream.readUnsignedShort()]).getName();
    ClassAttributes attributes = null;
    if ((accessFlags & ACC_INTERFACE) != 0) 
    {
      attributes = ClassAttributes.createInterface(name, size);
    } else 
    {
      if ((accessFlags & ACC_ABSTRACT) != 0) 
      {
        attributes = ClassAttributes.createAbstractClass(name, size);
      } else 
      {
        attributes = ClassAttributes.createClass(name, size);
      }
    }

    // Creates a new node with unresolved references
    UnresolvedNode node = new UnresolvedNode();
    node.attributes = attributes;
    for (int i = 0; i < pool.length; i++) 
    {
      Constant constant = pool[i];
      if (constant instanceof ClassConstant) 
      {
        ClassConstant cc = (ClassConstant) constant;
        if (!cc.getName().startsWith(("[")) && !cc.getName().equals(name)) 
        {
          node.nodes.add(cc.getName());
        }
      } else if (constant instanceof UTF8Constant) 
      {
        parseUTF8Constant((UTF8Constant) constant, node.nodes, name);
      } else if (reflectionPattern != null 
                 && constant instanceof StringConstant) 
      {
        String str = ((StringConstant) constant).getString();
        if (isValid(str) && reflectionPattern.matches(str)) 
        {
          node.nodes.add(str);
        }
      }
    }
    return node;
  }

  /** 
   * Parses an UFT8Constant and picks class names if it has the correct syntax
   * of a field or method descirptor.
   */
  private static void parseUTF8Constant(UTF8Constant constant,
                                        ArrayList nodes, String className) 
  {
    final String str = constant.getString();
    int len = str.length();

    // Gather possible class names and check syntax
    ArrayList classNames = new ArrayList();
    boolean valid = true;
    int[] index = new int[1];
    if (str.startsWith("(")) 
    {
      index[0]++;
      while (valid && index[0] < len - 1 && str.charAt(index[0]) != ')') 
      {
        valid = parseType(str, index, classNames);
      }
      if (valid) 
      {
        valid = index[0] < len - 1 && str.charAt(index[0]++) == ')';
        if (valid) 
        {
          valid = (index[0] == len - 1 && str.charAt(index[0]) == 'V')
                  || (parseType(str, index, classNames) && index[0] == len);
        }
      }
    } else 
    {
      valid = parseType(str, index, classNames) && index[0] == len;
    }

    // If valid syntax add gathered class names to nodes
    if (valid) 
    {
      for (int i = 0, n = classNames.size(); i < n; i++) 
      {
        if (!className.equals(classNames.get(i))) 
        {
          nodes.add(classNames.get(i));
        }
      }
    }
  }

  /**
   * Parses the specified string for a type descriptor and stores the extracted
   * class name in the specified array.
   * @param str String to be parsed.
   * @param i <tt>i[0]</tt> points into <tt>str</tt> on the first character
   *        of the type descriptor. After parsing it will point to the first 
   *        character after the type descriptor.
   * @param classNames Array of class names. If the parsed type descriptor is
   *        an object type with a syntactically valid class name this class 
   *        name will be added.
   * @return <tt>false</tt> if a syntax error has been detected.
   */
  private static boolean parseType(String str, int[] i, ArrayList classNames) 
  {
    boolean validType = false;
    boolean arrayType = false;
    int n = str.length();
    for (; i[0] < n && str.charAt(i[0]) == '['; i[0]++) 
    {
      arrayType = true;
    }
    if (i[0] < n) 
    {
      char c = str.charAt(i[0]++);
      if ("BCDFIJSZ".indexOf(c) >= 0) 
      {
        validType = true;
      } else if (c == 'L') 
      {
        int index = str.indexOf(';', i[0]);
        if (index > i[0]) 
        {
          String className = str.substring(i[0], index).replace('/', '.');
          classNames.add(className);
          i[0] = index + 1;
          validType = isValid(className);
        }
      }
    } else 
    {
      validType = !arrayType;
    }
    return validType;
  }

  /** Returns <tt>true</tt> if <tt>className</tt> is a valid class name. */
  private static boolean isValid(String className) 
  {
    boolean valid = true;
    boolean firstCharacter = true;
    for (int i = 0, n = className.length(); valid && i < n; i++) 
    {
      char c = className.charAt(i);
      if (firstCharacter) 
      {
        firstCharacter = false;
        valid = Character.isJavaIdentifierStart(c);
      } else 
      {
        if (c == '.') 
        {
          firstCharacter = true;
        } else 
        {
          valid = Character.isJavaIdentifierPart(c);
        }
      }
    }
    return valid && !firstCharacter;
  }

  /**
   *  Creates a graph from the bunch of unresolved nodes.
   *  @param unresolvedNodes All nodes with unresolved references.
   * @param mergeInnerClasses TODO
   *  @return an array of length <tt>unresolvedNodes.size()</tt> with all
   *          unresolved nodes transformed into <tt>Node</tt> objects
   *          with appropriated links. External nodes are created and linked
   *          but not added to the result array.
   */
  private static AtomicVertex[] createGraph(UnresolvedNode[] unresolvedNodes, 
                                            boolean mergeInnerClasses) 
  {
    Map vertices = createVertices(unresolvedNodes, mergeInnerClasses);
    AtomicVertex[] result 
        = (AtomicVertex[]) vertices.values().toArray(new AtomicVertex[0]);
    
    // Add arces to vertices
    for (int i = 0; i < unresolvedNodes.length; i++)
    {
      UnresolvedNode node = unresolvedNodes[i];
      String name = normalize(node.attributes.getName(), mergeInnerClasses);
      AtomicVertex vertex = (AtomicVertex) vertices.get(name);
      for (int j = 0, m = node.nodes.size(); j < m; j++) 
      {
        name = normalize((String) node.nodes.get(j), mergeInnerClasses);
        AtomicVertex head = (AtomicVertex) vertices.get(name);
        if (head == null) 
        {
          // external node created and added to the map but not to the result
          head = new AtomicVertex(ClassAttributes.createUnknownClass(name, 0));
          vertices.put(name, head);
        }
        if (vertex != head)
        {
          vertex.addOutgoingArcTo(head);
        }
      }
    }
    
    return result;
  }

  private static Map createVertices(UnresolvedNode[] unresolvedNodes, 
                                    boolean mergeInnerClasses)
  {
    Map vertices = new HashMap();
    for (int i = 0; i < unresolvedNodes.length; i++)
    {
      ClassAttributes attributes = unresolvedNodes[i].attributes;
      String type = attributes.getType();
      String originalName = attributes.getName();
      int size = attributes.getSize();
      String name = normalize(originalName, mergeInnerClasses);
      AtomicVertex vertex = (AtomicVertex) vertices.get(name);
      if (vertex != null)
      {
        ClassAttributes vertexAttributes 
                              = (ClassAttributes) vertex.getAttributes();
        size += vertexAttributes.getSize();
        if (name.equals(originalName) == false)
        {
          type = vertexAttributes.getType();
        }
      }
      attributes = new ClassAttributes(name, type, size);
      vertex = new AtomicVertex(attributes);
      vertices.put(name, vertex);
    }
    return vertices;
  }

  private static String normalize(String name, boolean mergeInnerClasses)
  {
    if (mergeInnerClasses)
    {
      int index = name.indexOf('$');
      if (index >= 0)
      {
        name = name.substring(0, index);
      }
    }
    return name;
  }

} //class