/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/

package org.eclipse.virgo.kernel.tools;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * A <code>DependencyLocationException</code> is thrown when one or more problems occurs when attempting to locate
 * all of a bundle's dependencies.
 * <p>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * This class is <code>thread-safe</code>
 * 
 */
public class DependencyLocationException extends RuntimeException {

    private static final long serialVersionUID = 7598582916642377631L;

    private final ImportDescriptor[] unsatisfiablePackageImports;

    private final ImportDescriptor[] unsatisfiableBundleImports;

    private final ImportDescriptor[] unsatisfiableLibraryImports;

    private final ImportDescriptor[] unsatisfiableRequireBundle;

    private final Map<File, List<String>> satisfiedDependencies;

    DependencyLocationException(ImportDescriptor[] unsatisfiablePackageImports, ImportDescriptor[] unsatisfiableBundleImports,
        ImportDescriptor[] unsatisfiableLibraryImports, ImportDescriptor[] unsatisfiableRequireBundle,
        Map<File, List<String>> satisfiedDependencies) {
        super(createMessage(unsatisfiablePackageImports, unsatisfiableBundleImports, unsatisfiableLibraryImports, unsatisfiableRequireBundle));
        this.unsatisfiableLibraryImports = unsatisfiableLibraryImports;
        this.unsatisfiablePackageImports = unsatisfiablePackageImports;
        this.satisfiedDependencies = satisfiedDependencies;
        this.unsatisfiableRequireBundle = unsatisfiableRequireBundle;
        this.unsatisfiableBundleImports = unsatisfiableBundleImports;
    }

    private static String createMessage(ImportDescriptor[] unsatisfiablePackageImports, ImportDescriptor[] unsatisfiableBundleImports,
        ImportDescriptor[] unsatisfiableLibraryImports, ImportDescriptor[] unsatisfiableRequireBundle) {
        StringBuffer buffer = new StringBuffer("Dependency location failed. The following problems occurred:\n");
        buffer.append("    Unsatisfiable package imports:\n");
        appendImportDescriptors(unsatisfiablePackageImports, buffer);
        buffer.append("    Unsatisfiable bundle imports:\n");
        appendImportDescriptors(unsatisfiableBundleImports, buffer);
        buffer.append("    Unsatisfiable library imports:\n");
        appendImportDescriptors(unsatisfiableLibraryImports, buffer);
        buffer.append("    Unsatisfiable required bundles:\n");
        appendImportDescriptors(unsatisfiableRequireBundle, buffer);
        return buffer.toString();
    }

    private static void appendImportDescriptors(ImportDescriptor[] descriptors, StringBuffer buffer) {
        if (descriptors.length == 0) {
            buffer.append("        None.\n");
        } else {
            for (ImportDescriptor descriptor : descriptors) {
                buffer.append("        ").append(descriptor.getName()).append(" with version ").append(descriptor.getVersion()).append("\n");
            }
        }
    }

    /**
     * Returns an array of all of the imported packages that could not be satisfied.
     * 
     * @return the manifest's unsatisfiable package imports
     */
    public ImportDescriptor[] getUnsatisfiablePackageImports() {
        return this.unsatisfiablePackageImports.clone();
    }

    /**
     * Returns an array of all of the imported libraries that could not be satisfied.
     * 
     * @return the manifest's unsatisfiable library imports
     */
    public ImportDescriptor[] getUnsatisfiableLibraryImports() {
        return this.unsatisfiableLibraryImports.clone();
    }

    /**
     * Returns an array of all of the required bundles that could not be satisfied.
     * 
     * @return the manifest's unsatisfiable required bundles
     */
    public ImportDescriptor[] getUnsatisfiableRequireBundle() {
        return unsatisfiableRequireBundle.clone();
    }

    public ImportDescriptor[] getUnsatisfiableBundleImports() {
        return unsatisfiableBundleImports.clone();
    }

    /**
     * Returns all of the dependencies that could be satisfied.
     * 
     * @return the manifest's satisfiable dependencies
     */
    public Map<File, List<String>> getSatisfiedDependencies() {
        return Collections.unmodifiableMap(this.satisfiedDependencies);
    }
}
