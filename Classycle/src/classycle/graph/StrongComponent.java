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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 *  A strong component is a subgraph of a directed graph where every two
 *  vertices are mutually reachable.
 *
 *  @author Franz-Josef Elmer
 */
public class StrongComponent extends Vertex {
  private static class GeometryAttributes implements GraphAttributes {
    private int _girth;
    private int _radius;
    private int _diameter;
    private ArrayList _centerVertices = new ArrayList();
    private int[] _eccentricities;
    private int[] _maximumFragmentSizes;

    public GeometryAttributes() {
    }

    public int getGirth() {
      return _girth;
    }

    void setGirth(int girth) {
      _girth = girth;
    }

    public int getRadius() {
      return _radius;
    }

    void setRadius(int radius) {
      _radius = radius;
    }

    public int getDiameter() {
      return _diameter;
    }

    void setDiameter(int diameter) {
      _diameter = diameter;
    }

    public Vertex[] getCenterVertices() {
      return (Vertex[]) _centerVertices.toArray(
        new Vertex[_centerVertices.size()]);
    }

    void addVertex(Vertex vertex) {
      _centerVertices.add(vertex);
    }
    
    public int[] getEccentricities() {
      return _eccentricities;
    }
    
    void setEccentricities(int[] eccentricities) {
      _eccentricities = eccentricities;
    }
    
    public int[] getMaximumFragmentSizes() {
      return _maximumFragmentSizes;
    }
    
    void setMaximumFragmentSizes(int[] maximumFragmentSizes) {
      _maximumFragmentSizes = maximumFragmentSizes;
    }
  }

  private final Vector _vertices = new Vector();
  private boolean _active;
  private int _longestWalk;

  /**
   * Default constructor. The {@link Attributes} of a strong component will
   * a <tt>null</tt> pointer.
   */
  public StrongComponent() {
    super(new GeometryAttributes());
  }

  /** Returns the number of vertices building this strong component. */
  public int getNumberOfVertices() {
    return _vertices.size();
  }

  /** Returns the vertex of the specified index. */
  public AtomicVertex getVertex(int index) {
    return (AtomicVertex) _vertices.elementAt(index);
  }

  /**
   *  Adds the specified vertex to this strong component. Note, that added
   *  vertices are inserted at index 0 of the list of vertices.
   */
  public void addVertex(AtomicVertex vertex) {
    _vertices.insertElementAt(vertex, 0);
  }

  /**
   *  Calculates all graph properties of this component.
   *  These properties can be obtained from <tt>getAttributes</tt> casted as
   *  {@link GraphAttributes}.
   */
  public void calculateAttributes() {
    HashMap indexMap = calculateIndexMap();
    int[][] distances = calculateDistances(indexMap);

    // Calculate girth and eccentricity
    GeometryAttributes attributes = (GeometryAttributes) getAttributes();
    int girth = Integer.MAX_VALUE;
    int[] eccentricities = new int[distances.length];
    for (int i = 0; i < distances.length; i++) {
      girth = Math.min(girth, distances[i][i]);
      eccentricities[i] = 0;
      for (int j = 0; j < distances.length; j++) {
        if (i != j) {
          eccentricities[i] = Math.max(eccentricities[i], distances[i][j]);
        }
      }
    }
    attributes.setEccentricities(eccentricities);
    attributes.setGirth(girth);

    // Calculate radius and diameter
    int radius = Integer.MAX_VALUE;
    int diameter = 0;
    for (int i = 0; i < distances.length; i++) {
      radius = Math.min(radius, eccentricities[i]);
      diameter = Math.max(diameter, eccentricities[i]);
    }
    attributes.setRadius(radius);
    attributes.setDiameter(diameter);

    // Obtain center vertices
    for (int i = 0; i < distances.length; i++) {
      if (eccentricities[i] == radius) {
        attributes.addVertex(getVertex(i));
      }
    }
    
    attributes.setMaximumFragmentSizes(
                            calculateMaximumFragmentSizes(indexMap));
  }
  
  private int[][] calculateDistances(HashMap indexMap) {
    // Calculate the adjacency matrix
    int n = getNumberOfVertices();
    int[][] distances = new int[n][n];
    for (int i = 0; i < n; i++) {
      int[] row = distances[i];
      AtomicVertex vertex = getVertex(i);
      for (int j = 0; j < n; j++) {
        row[j] = Integer.MAX_VALUE / 2;
      }
      for (int j = 0, m = vertex.getNumberOfOutgoingArcs(); j < m; j++) {
        Integer index = (Integer) indexMap.get(vertex.getHeadVertex(j));
        if (index != null) {
          row[index.intValue()] = 1;
        }
      }
    }

    // Floyd-Warshall algorithm for the distances
    for (int k = 0; k < n; k++) {
      for (int i = 0; i < n; i++) {
        for (int j = 0; j < n; j++) {
          if (distances[i][k] + distances[k][j] < distances[i][j]) {
            distances[i][j] = distances[i][k] + distances[k][j];
          }
        }
      }
    }
    
    return distances;
  }

  private HashMap calculateIndexMap() {
    HashMap result = new HashMap();
    for (int i = 0, n = getNumberOfVertices(); i < n; i++) {
      result.put(getVertex(i), new Integer(i));
    }
    return result;
  }
  
  private int[] calculateMaximumFragmentSizes(HashMap indexMap) {
    // clone graph defining this strong component
    AtomicVertex[] graph = new AtomicVertex[getNumberOfVertices()];
    for (int i = 0; i < graph.length; i++) {
      graph[i] = new AtomicVertex(null);
    }
    for (int i = 0; i < graph.length; i++) {
      AtomicVertex vertex = getVertex(i);
      for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) {
        Integer index = (Integer) indexMap.get(vertex.getHeadVertex(j));
        if (index != null) {
          graph[i].addOutgoingArcTo(graph[index.intValue()]);
        }
      }
    }
    
    StrongComponentProcessor processor = new StrongComponentProcessor(false);
    int[] maximumFragmentSizes = new int[getNumberOfVertices()];
    for (int i = 0; i < maximumFragmentSizes.length; i++) {
      graph[i].setDefaultValueOfGraphVertexFlag(false);
      processor.deepSearchFirst(graph);
      StrongComponent[] fragments = processor.getStrongComponents();
      maximumFragmentSizes[i] = 0;
      for (int j = 0; j < fragments.length; j++) {
        maximumFragmentSizes[i] = Math.max(maximumFragmentSizes[i], 
                                           fragments[j].getNumberOfVertices());
      }
      graph[i].setDefaultValueOfGraphVertexFlag(true);
    }
    return maximumFragmentSizes;
  }

  /**
   * Reset this component. Calls reset of the superclass. Sets the activity
   * flag to false and the longest walk to -1.
   */
  public void reset() {
    super.reset();
    _active = false;
    _longestWalk = -1;
  }

  public boolean isActive() {
    return _active;
  }

  public void setActive(boolean active) {
    _active = active;
  }

  public int getLongestWalk() {
    return _longestWalk;
  }

  public void setLongestWalk(int longestWalk) {
    _longestWalk = longestWalk;
  }

  public String toString() {
    StringBuffer result = new StringBuffer("Strong component with ");
    int n = getNumberOfVertices();
    result.append(n).append(n > 1 ? " vertices." : " vertex.");
    result.append(" Longest walk: ").append(getLongestWalk());
    for (int i = 0; i < n; i++) {
      result.append("\n    ").append(getVertex(i));
    }
    return new String(result);
  }
} //class