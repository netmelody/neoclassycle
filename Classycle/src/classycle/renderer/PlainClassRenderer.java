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

import classycle.ClassAttributes;
import classycle.graph.AtomicVertex;

public class PlainClassRenderer implements AtomicVertexRenderer
{
  public String render(AtomicVertex vertex)
  {
    if (vertex.getAttributes() instanceof ClassAttributes)
    {
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
      StringBuffer result = new StringBuffer();
      result.append("Used by ").append(vertex.getNumberOfIncomingArcs());
      result.append(" classes. Uses ").append(usesInternal).append('/');
      result.append(usesExternal).append(" internal/external classes: ");
      result.append(vertex.getAttributes());
      return new String(result);
    }
    else
    {
      throw new IllegalArgumentException("Missing class attributes in vertex "
                                         + vertex);
    }
  }
} //class