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
package org.netmelody.neoclassycle.renderer;

import java.text.MessageFormat;

import org.netmelody.neoclassycle.graph.StrongComponent;

/**
 * Renderer of a {@link StrongComponent}. The renderer is based on a
 * <tt>java.text.MessageFormat</tt> template.
 *
 * @author Franz-Josef Elmer
 */
public final class TemplateBasedStrongComponentRenderer extends AbstractStrongComponentRenderer {
    private final MessageFormat _format;
    private final int _minimumNumber;

    /**
     * Creates an instance for the specified template.
     *
     * @param template
     *            The template.
     * @param minimumSize
     *            Minimum number of vertices the {@link StrongComponent} should
     *            have to be rendered.
     */
    public TemplateBasedStrongComponentRenderer(final String template, final int minimumNumber) {
        _format = new MessageFormat(template);
        _minimumNumber = minimumNumber;
    }

    @Override
    public String render(final StrongComponent component) {
        String result = "";
        if (component.getNumberOfVertices() >= _minimumNumber) {
            final String[] values = new String[3];
            values[0] = createName(component);
            values[1] = Integer.toString(component.getNumberOfVertices());
            values[2] = Integer.toString(component.getLongestWalk());
            result = _format.format(values);
        }
        return result;
    }
}