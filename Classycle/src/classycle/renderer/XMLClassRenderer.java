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
package classycle.renderer;

import java.text.MessageFormat;

import classycle.ClassAttributes;
import classycle.graph.AtomicVertex;

public class XMLClassRenderer implements AtomicVertexRenderer
{
  public static final String CLASS_ELEMENT = "class",
                             CLASS_REF_ELEMENT = "classRef";
  private static final MessageFormat CLASS_START_FORMAT
      = new MessageFormat("    <" + CLASS_ELEMENT
                          + " name=\"{0}\" type=\"{1}\" innerClass=\"{3}\""
                          + " size=\"{2}\" usedBy=\"{4}\""
                          + " usesInternal=\"{5}\" usesExternal=\"{6}\">\n");
  private static final String CLASS_END_TEMPLATE
      = "    </" + CLASS_ELEMENT + ">\n";
  private static final MessageFormat CLASS_REF_FORMAT
      = new MessageFormat("      <" + CLASS_REF_ELEMENT + " name=\"{0}\""
                          + " type=\"{1}\"/>\n");

  public String render(AtomicVertex vertex)
  {
    StringBuffer result = new StringBuffer();
    String[] values = new String[7];
    ClassAttributes attributes = (ClassAttributes) vertex.getAttributes();
    values[0] = attributes.getName();
    values[1] = attributes.getType();
    values[2] = Long.toString(attributes.getSize());
    values[3] = attributes.isInnerClass() ? "true" : "false";
    values[4] = Integer.toString(vertex.getNumberOfIncomingArcs());
    int usesInternal = 0;
    int usesExternal = 0;
    for (int i = 0, n = vertex.getNumberOfOutgoingArcs(); i < n; i++)
    {
      if (((AtomicVertex) vertex.getHeadVertex(i)).isGraphVertex())
      {
        usesInternal++;
      }
      else
      {
        usesExternal++;
      }
    }
    values[5] = Integer.toString(usesInternal);
    values[6] = Integer.toString(usesExternal);
    CLASS_START_FORMAT.format(values, result, null);

    for (int i = 0, n = vertex.getNumberOfIncomingArcs(); i < n; i++)
    {
      values[0] = ((ClassAttributes) vertex.getTailVertex(i)
                                                .getAttributes()).getName();
      values[1] = "usedBy";
      CLASS_REF_FORMAT.format(values, result, null);
    }
    for (int i = 0, n = vertex.getNumberOfOutgoingArcs(); i < n; i++)
    {
      values[0] = ((ClassAttributes) vertex.getHeadVertex(i)
                                                .getAttributes()).getName();
      values[1] = ((AtomicVertex) vertex.getHeadVertex(i)).isGraphVertex()
          ? "usesInternal" : "usesExternal";
      CLASS_REF_FORMAT.format(values, result, null);
    }
    result.append(CLASS_END_TEMPLATE);
    return new String(result);
  }
} //class