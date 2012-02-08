/*
 * Copyright (c) 2003-2008, Franz-Josef Elmer, All rights reserved.
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
package org.netmelody.neoclassycle.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * A strong component is a subgraph of a directed graph where every two vertices
 * are mutually reachable.
 *
 * @author Franz-Josef Elmer
 */
public final class StrongComponent extends Vertex {
    private static class GeometryAttributes implements GraphAttributes {
        private int _girth;
        private int _radius;
        private int _diameter;
        private final ArrayList<Vertex> _centerVertices = new ArrayList<Vertex>();
        private int[] _eccentricities;
        private int[] _maximumFragmentSizes;
        private int _bestFragmentSize;
        private final ArrayList<Vertex> _bestFragmenters = new ArrayList<Vertex>();

        public GeometryAttributes() {
        }

        @Override
        public int getGirth() {
            return _girth;
        }

        void setGirth(final int girth) {
            _girth = girth;
        }

        @Override
        public int getRadius() {
            return _radius;
        }

        @Override
        public int getDiameter() {
            return _diameter;
        }

        @Override
        public int getBestFragmentSize() {
            return _bestFragmentSize;
        }

        @Override
        public Vertex[] getCenterVertices() {
            return _centerVertices.toArray(new Vertex[_centerVertices.size()]);
        }

        void addVertex(final Vertex vertex) {
            _centerVertices.add(vertex);
        }

        @Override
        public Vertex[] getBestFragmenters() {
            return _bestFragmenters.toArray(new Vertex[_bestFragmenters.size()]);
        }

        void addFragmenter(final Vertex vertex) {
            _bestFragmenters.add(vertex);
        }

        @Override
        public int[] getEccentricities() {
            return _eccentricities;
        }

        void setEccentricities(final int[] eccentricities) {
            _eccentricities = eccentricities;

            // Calculate radius and diameter
            _radius = Integer.MAX_VALUE;
            _diameter = 0;
            for (final int eccentricitie : eccentricities) {
                _radius = Math.min(_radius, eccentricitie);
                _diameter = Math.max(_diameter, eccentricitie);
            }
        }

        @Override
        public int[] getMaximumFragmentSizes() {
            return _maximumFragmentSizes;
        }

        void setMaximumFragmentSizes(final int[] maximumFragmentSizes) {
            _maximumFragmentSizes = maximumFragmentSizes;

            _bestFragmentSize = Integer.MAX_VALUE;
            for (final int maximumFragmentSize : maximumFragmentSizes) {
                _bestFragmentSize = Math.min(_bestFragmentSize, maximumFragmentSize);
            }
        }

        @Override
        public int compareTo(final Attributes object) {
            int result = 1;
            if (object instanceof GeometryAttributes && _bestFragmenters.size() > 0) {
                final ArrayList<Vertex> list = ((GeometryAttributes) object)._bestFragmenters;
                if (list.size() > 0) {
                    final Attributes attributes = _bestFragmenters.get(0).getAttributes();
                    final Attributes objectAttributes = list.get(0).getAttributes();
                    result = attributes.compareTo(objectAttributes);
                }
            }
            return result;
        }

    }

    private final Vector<Vertex> _vertices = new Vector<Vertex>();
    private boolean _active;
    private int _longestWalk;

    /**
     * Default constructor. The {@link Attributes} of a strong component will a
     * <tt>null</tt> pointer.
     */
    public StrongComponent() {
        super(new GeometryAttributes());
    }

    /** Returns the number of vertices building this strong component. */
    public int getNumberOfVertices() {
        return _vertices.size();
    }

    /** Returns the vertex of the specified index. */
    public AtomicVertex getVertex(final int index) {
        return (AtomicVertex) _vertices.elementAt(index);
    }

    /**
     * Adds the specified vertex to this strong component. Note, that added
     * vertices are inserted at index 0 of the list of vertices.
     */
    public void addVertex(final AtomicVertex vertex) {
        _vertices.insertElementAt(vertex, 0);
    }

    /**
     * Calculates all graph properties of this component. These properties can
     * be obtained from <tt>getAttributes</tt> casted as {@link GraphAttributes}
     * .
     */
    public void calculateAttributes() {
        final HashMap<Vertex, Integer> indexMap = calculateIndexMap();
        final int[][] distances = calculateDistances(indexMap);

        // Calculate girth and eccentricity
        final GeometryAttributes attributes = (GeometryAttributes) getAttributes();
        int girth = Integer.MAX_VALUE;
        final int[] eccentricities = new int[distances.length];
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
        attributes.setMaximumFragmentSizes(calculateMaximumFragmentSizes(indexMap));

        // Obtain center vertices and best fragmenters
        for (int i = 0, r = attributes.getRadius(), s = attributes.getBestFragmentSize(); i < distances.length; i++) {
            if (eccentricities[i] == r) {
                attributes.addVertex(getVertex(i));
            }
            if (attributes.getMaximumFragmentSizes()[i] == s) {
                attributes.addFragmenter(getVertex(i));
            }
        }

    }

    private int[][] calculateDistances(final HashMap<Vertex, Integer> indexMap) {
        // Calculate the adjacency matrix
        final int n = getNumberOfVertices();
        final int[][] distances = new int[n][n];
        for (int i = 0; i < n; i++) {
            final int[] row = distances[i];
            final AtomicVertex vertex = getVertex(i);
            for (int j = 0; j < n; j++) {
                row[j] = Integer.MAX_VALUE / 2;
            }
            for (int j = 0, m = vertex.getNumberOfOutgoingArcs(); j < m; j++) {
                final Integer index = indexMap.get(vertex.getHeadVertex(j));
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

    private HashMap<Vertex, Integer> calculateIndexMap() {
        final HashMap<Vertex, Integer> result = new HashMap<Vertex, Integer>();
        for (int i = 0, n = getNumberOfVertices(); i < n; i++) {
            result.put(getVertex(i), new Integer(i));
        }
        return result;
    }

    private int[] calculateMaximumFragmentSizes(final HashMap<Vertex, Integer> indexMap) {
        // clone graph defining this strong component
        final AtomicVertex[] graph = new AtomicVertex[getNumberOfVertices()];
        for (int i = 0; i < graph.length; i++) {
            graph[i] = new AtomicVertex(null);
        }
        for (int i = 0; i < graph.length; i++) {
            final AtomicVertex vertex = getVertex(i);
            for (int j = 0, n = vertex.getNumberOfOutgoingArcs(); j < n; j++) {
                final Integer index = indexMap.get(vertex.getHeadVertex(j));
                if (index != null) {
                    graph[i].addOutgoingArcTo(graph[index.intValue()]);
                }
            }
        }

        final StrongComponentProcessor processor = new StrongComponentProcessor(false);
        final int[] maximumFragmentSizes = new int[getNumberOfVertices()];
        for (int i = 0; i < maximumFragmentSizes.length; i++) {
            graph[i].setDefaultValueOfGraphVertexFlag(false);
            processor.deepSearchFirst(graph);
            final StrongComponent[] fragments = processor.getStrongComponents();
            maximumFragmentSizes[i] = 0;
            for (final StrongComponent fragment : fragments) {
                maximumFragmentSizes[i] = Math.max(maximumFragmentSizes[i], fragment.getNumberOfVertices());
            }
            graph[i].setDefaultValueOfGraphVertexFlag(true);
        }
        return maximumFragmentSizes;
    }

    /**
     * Reset this component. Calls reset of the superclass. Sets the activity
     * flag to false and the longest walk to -1.
     */
    @Override
    public void reset() {
        super.reset();
        _active = false;
        _longestWalk = -1;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(final boolean active) {
        _active = active;
    }

    public int getLongestWalk() {
        return _longestWalk;
    }

    public void setLongestWalk(final int longestWalk) {
        _longestWalk = longestWalk;
    }

    @Override
    public String toString() {
        final StringBuffer result = new StringBuffer("Strong component with ");
        final int n = getNumberOfVertices();
        result.append(n).append(n > 1 ? " vertices." : " vertex.");
        result.append(" Longest walk: ").append(getLongestWalk());
        for (int i = 0; i < n; i++) {
            result.append("\n    ").append(getVertex(i));
        }
        return new String(result);
    }
}