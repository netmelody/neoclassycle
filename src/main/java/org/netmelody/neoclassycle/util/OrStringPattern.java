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
package org.netmelody.neoclassycle.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Logical OR Operation on a sequence of {@link StringPattern StringPatterns}.
 *
 * @author Franz-Josef Elmer
 */
public final class OrStringPattern implements StringPattern {

    protected final List<StringPattern> _patterns = new ArrayList<StringPattern>();

    /**
     * Creates instance with specified patterns.
     */
    public OrStringPattern(final StringPattern... pattern) {
        _patterns.addAll(Arrays.asList(pattern));
    }

    /**
     * Appends the specified pattern.
     */
    public void appendPattern(final StringPattern pattern) {
        _patterns.add(pattern);
    }

    @Override
    public String toString() {
        final StringBuffer buffer = new StringBuffer();
        final int size = _patterns.size();
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                buffer.append(" ");
            }
            buffer.append(_patterns.get(i));
        }
        return buffer.toString();
    }

    /**
     * Return <code>true</code> if a pattern in the sequence returns
     * <code>true</code>. Otherwise <code>false</code> is returned.
     */
    @Override
    public boolean matches(final String string) {
        boolean result = false;
        for (int i = 0, n = _patterns.size(); i < n; i++) {
            if (_patterns.get(i).matches(string)) {
                result = true;
                break;
            }
        }
        return result;
    }
}
