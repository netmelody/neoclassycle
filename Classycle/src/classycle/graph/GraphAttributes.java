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
package classycle.graph;

/**
 * Attributes of a graph. The following properties
 * can be accessed with this interface:
 * <dl><dt>Girth:</dt><dd>The length of the shortest cycle.</dd>
 *     <dt>Diameter:</dt>
 *     <dd>The largest <em>distance</em> in the graph. 
 *         The <em>distance</em> between vertex A and B is defined as the
 *         shortest path from A to B. The distance is infinite if there is
 *         no path from A to B.
 *     </dd>
 *     <dt>Radius:</dt>
 *     <dd>The smallest <em>eccentricity</em> in the graph. The
 *         <em>eccentricity</em> of a vertex A is the maximum over
 *         all distances form A to any other vertex.
 *     </dd>
 *     <dt>Center:</dt>
 *     <dd>The set of vertices of the graph with the
 *         smallest eccentricities.
 *     </dd>
 *     <dt>Maximum fragment sizes:</dt>
 *     <dd>The size of the largest strong component of the graph after 
 *         removing of the vertex specified by the index.
 *     </dd>
 * </dl>
 * 
 * @author Franz-Josef Elmer
 */
public interface GraphAttributes extends Attributes {
  /** Returns the girth. */
  public int getGirth();
  
  /** Returns the radius. */
  public int getRadius();
  
  /** Returns the diameter. */
  public int getDiameter();
  
  /** Returns the vertices of the center. */
  public Vertex[] getCenterVertices();
  
  /** 
   * Returns the eccentricies of all vertices of a {@link StrongComponent}. 
   */
  public int[] getEccentricities();
  
  /**
   * Returns the maximum fragment sizes of all vertices of 
   * a {@link StrongComponent}.
   */ 
  public int[] getMaximumFragmentSizes();
} //interface