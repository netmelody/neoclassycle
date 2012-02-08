/*
 * Copyright (c) 2012 Tom Denley
 * This file incorporates work covered by the following copyright and
 * permission notice:
 *
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
package org.netmelody.neoclassycle.renderer;

import java.text.MessageFormat;

import org.netmelody.neoclassycle.graph.GraphAttributes;
import org.netmelody.neoclassycle.graph.NameAttributes;
import org.netmelody.neoclassycle.graph.StrongComponent;
import org.netmelody.neoclassycle.graph.Vertex;

/**
 * XML renderer of a {@link StrongComponent}.
 *
 * @author Franz-Josef Elmer
 */
public class XMLStrongComponentRenderer extends AbstractStrongComponentRenderer {
    
    private final int _minimumSize;
    private final String strongComponentElementName;
    private final String nodesElementName;
    private final String nodeElementName;
    private final String centerNodesElementName;
    private final String bestFragmentersElementName;

    /**
     * Creates an instance for the specified minimum number vertices.
     *
     * @param minimumSize
     *            Minimum number of vertices the {@link StrongComponent} should
     *            have to be rendered.
     */
    public XMLStrongComponentRenderer(final int minimumSize) {
        this(minimumSize, "cycle", "classes", "classRef", "centerClasses");
    }
    
    protected XMLStrongComponentRenderer(int minimumSize, String strongComponentElementName, String nodesElementName,
                                         String nodeElementName, String centerNodesElementName) {
        this._minimumSize = minimumSize;
        this.strongComponentElementName = strongComponentElementName;
        this.nodesElementName = nodesElementName;
        this.nodeElementName = nodeElementName;
        this.centerNodesElementName = centerNodesElementName;
        this.bestFragmentersElementName = "bestFragmenters";
    }

    private MessageFormat getStrongComponentElementTemplate() {
        return new MessageFormat("    <" + strongComponentElementName + " name=\"{0}\" size=\"{1}\" longestWalk=\"{2}\""
                + " girth=\"{3}\" radius=\"{4}\" diameter=\"{5}\"" + " bestFragmentSize=\"{6}\">\n");
    }

    private MessageFormat getNodeElementTemplate() {
        return new MessageFormat("        <" + nodeElementName + " name=\"{0}\"/>\n");
    }

    private MessageFormat getNodeElementTemplateWithEccentricity() {
        return new MessageFormat("        <" + nodeElementName + " name=\"{0}\" eccentricity=\"{1}\""
                + " maximumFragmentSize=\"{2}\"/>\n");
    }

    @Override
    public String render(final StrongComponent component) {
        final StringBuffer result = new StringBuffer();
        if (component.getNumberOfVertices() >= _minimumSize) {
            final String[] values = new String[7];
            values[0] = createName(component);
            values[1] = Integer.toString(component.getNumberOfVertices());
            values[2] = Integer.toString(component.getLongestWalk());
            final GraphAttributes attributes = (GraphAttributes) component.getAttributes();
            values[3] = Integer.toString(attributes.getGirth());
            values[4] = Integer.toString(attributes.getRadius());
            values[5] = Integer.toString(attributes.getDiameter());
            values[6] = Integer.toString(attributes.getBestFragmentSize());
            getStrongComponentElementTemplate().format(values, result, null);

            renderClasses(component, result);
            renderVertices(attributes.getCenterVertices(), result, centerNodesElementName);
            renderVertices(attributes.getBestFragmenters(), result, bestFragmentersElementName);
            result.append("    </").append(strongComponentElementName).append(">\n");
        }
        return new String(result);
    }

    private void renderClasses(final StrongComponent component, final StringBuffer result) {
        result.append("      <").append(nodesElementName).append(">\n");
        final int[] eccentricities = ((GraphAttributes) component.getAttributes()).getEccentricities();
        final int[] maximumFragmentSizes = ((GraphAttributes) component.getAttributes()).getMaximumFragmentSizes();
        final String[] values = new String[3];
        final MessageFormat template = getNodeElementTemplateWithEccentricity();
        for (int i = 0, n = component.getNumberOfVertices(); i < n; i++) {
            values[0] = ((NameAttributes) component.getVertex(i).getAttributes()).getName();
            values[1] = Integer.toString(eccentricities[i]);
            values[2] = Integer.toString(maximumFragmentSizes[i]);
            template.format(values, result, null);
        }
        result.append("      </").append(nodesElementName).append(">\n");
    }

    private void renderVertices(final Vertex[] vertices, final StringBuffer result, final String tagName) {
        result.append("      <").append(tagName).append(">\n");
        final String[] values = new String[1];
        final MessageFormat template = getNodeElementTemplate();
        for (final Vertex vertice : vertices) {
            values[0] = ((NameAttributes) vertice.getAttributes()).getName();
            template.format(values, result, null);
        }
        result.append("      </").append(tagName).append(">\n");
    }
}