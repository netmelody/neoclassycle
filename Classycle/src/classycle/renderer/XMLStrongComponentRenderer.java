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
import classycle.graph.GraphAttributes;
import classycle.graph.StrongComponent;
import classycle.graph.Vertex;

/**
 * XML renderer of a {@link StrongComponent}.
 * 
 * @author Franz-Josef Elmer
 */
public class XMLStrongComponentRenderer 
                          extends AbstractStrongComponentRenderer {
  public static final String CYCLE_ELEMENT = "cycle",
                             CLASS_REF_ELEMENT = "classRef",
                             CLASSES_ELEMENT = "classes",
                             CENTER_CLASSES_ELEMENT = "centerClasses",
                             BEST_FRAGMENTERS_ELEMENT = "bestFragmenters";
  private static final MessageFormat CYCLES_START_FORMAT
      = new MessageFormat("    <" + CYCLE_ELEMENT
                          + " name=\"{0}\" size=\"{1}\" longestWalk=\"{2}\""
                          + " girth=\"{3}\" radius=\"{4}\" diameter=\"{5}\""
                          + " bestFragmentSize=\"{6}\">\n");
  private static final String CYCLES_END_TEMPLATE
      = "    </" + CYCLE_ELEMENT + ">\n";
  private static final String CLASSES_START_TEMPLATE
      = "      <" + CLASSES_ELEMENT + ">\n";
  private static final String CLASSES_END_TEMPLATE
      = "      </" + CLASSES_ELEMENT + ">\n";
  private static final MessageFormat CLASS_REF_FORMAT_WITH_ECCENTRICITY
      = new MessageFormat("        <" + CLASS_REF_ELEMENT
                          + " name=\"{0}\" eccentricity=\"{1}\""
                          + " maximumFragmentSize=\"{2}\"/>\n");
  private static final MessageFormat CLASS_REF_FORMAT
      = new MessageFormat("        <" + CLASS_REF_ELEMENT
                          + " name=\"{0}\"/>\n");

  private final int _minimumSize;

  /**
   * Creates an instance for the specified minimum number vertices. 
   * @param minimumSize Minimum number of vertices the {@link StrongComponent}
   *        should have to be rendered.
   */
  public XMLStrongComponentRenderer(int minimumSize) {
    _minimumSize = minimumSize;
  }

  public String render(StrongComponent component) {
    StringBuffer result = new StringBuffer();
    if (component.getNumberOfVertices() >= _minimumSize) {
      String[] values = new String[7];
      values[0] = createName(component);
      values[1] = Integer.toString(component.getNumberOfVertices());
      values[2] = Integer.toString(component.getLongestWalk());
      GraphAttributes attributes = (GraphAttributes) component.getAttributes();
      values[3] = Integer.toString(attributes.getGirth());
      values[4] = Integer.toString(attributes.getRadius());
      values[5] = Integer.toString(attributes.getDiameter());
      values[6] = Integer.toString(attributes.getBestFragmentSize());
      CYCLES_START_FORMAT.format(values, result, null);

      renderClasses(component, result);
      renderVertices(attributes.getCenterVertices(), result, 
                     CENTER_CLASSES_ELEMENT);
      renderVertices(attributes.getBestFragmenters(), result, 
                     BEST_FRAGMENTERS_ELEMENT);
      result.append(CYCLES_END_TEMPLATE);
    }
    return new String(result);
  }

  private void renderClasses(StrongComponent component, StringBuffer result) {
    result.append(CLASSES_START_TEMPLATE);
    int[] eccentricities 
        = ((GraphAttributes) component.getAttributes()).getEccentricities();
    int[] maximumFragmentSizes 
        = ((GraphAttributes) component.getAttributes())
                                                  .getMaximumFragmentSizes();
    String[] values = new String[3];
    for (int i = 0, n = component.getNumberOfVertices(); i < n; i++) {
      values[0] = ((ClassAttributes) component.getVertex(i).getAttributes())
                                                                  .getName();
      values[1] = Integer.toString(eccentricities[i]);
      values[2] = Integer.toString(maximumFragmentSizes[i]);
      CLASS_REF_FORMAT_WITH_ECCENTRICITY.format(values, result, null);
    }
    result.append(CLASSES_END_TEMPLATE);
  }
  
  private void renderVertices(Vertex[] vertices, StringBuffer result, 
                              String tagName) {
    result.append("      <").append(tagName).append(">\n");
    String[] values = new String[1];
    for (int i = 0; i < vertices.length; i++) {
      values[0] = ((ClassAttributes) vertices[i].getAttributes()).getName();
      CLASS_REF_FORMAT.format(values, result, null);
    }
    result.append("      </").append(tagName).append(">\n");
  }
} //class