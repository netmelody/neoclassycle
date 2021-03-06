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
package org.netmelody.neoclassycle.dependency;

import java.io.File;
import java.io.IOException;

import org.netmelody.neoclassycle.CommandLine;
import org.netmelody.neoclassycle.util.Text;

/**
 * @author Franz-Josef Elmer
 */
public final class DependencyCheckerCommandLine extends CommandLine {
    private static final String DEPENDENCIES = "-dependencies=", RENDERER = "-renderer=";

    private String _dependencyDefinition;
    private ResultRenderer _renderer;

    public DependencyCheckerCommandLine(final String[] args) {
        super(args);
    }

    @Override
    protected void handleOption(final String argument) {
        if (argument.startsWith(DEPENDENCIES)) {
            handleDependenciesOption(argument.substring(DEPENDENCIES.length()));
        }
        else if (argument.startsWith(RENDERER)) {
            handleRenderer(argument.substring(RENDERER.length()));
        }
        else {
            super.handleOption(argument);
        }
    }

    /** Returns the usage of correct command line arguments and options. */
    @Override
    public String specificUsage() {
        return DEPENDENCIES + "<description>|@<description file> " + "[" + RENDERER + "<fully qualified class name of a ResultRenderer>] ";
    }

    public String getDependencyDefinition() {
        return _dependencyDefinition;
    }

    public ResultRenderer getRenderer() {
        return _renderer == null ? new DefaultResultRenderer() : _renderer;
    }

    private void handleDependenciesOption(String option) {
        if (option.startsWith("@")) {
            try {
                _dependencyDefinition = Text.readTextFile(new File(option.substring(1)));
            }
            catch (final IOException e) {
                System.err.println("Error in reading dependencies description file: " + e);
                _dependencyDefinition = "";
            }
        }
        if (_dependencyDefinition.length() == 0) {
            _valid = false;
        }
    }

    private void handleRenderer(final String className) {
        try {
            _renderer = (ResultRenderer) Class.forName(className).newInstance();
        }
        catch (final Exception e) {
            System.err.println("Error in creating ResultRenderer " + className + ": " + e);
            _valid = false;
        }
    }

    @Override
    public boolean isValid() {
        return super.isValid() && _dependencyDefinition != null;
    }
}
