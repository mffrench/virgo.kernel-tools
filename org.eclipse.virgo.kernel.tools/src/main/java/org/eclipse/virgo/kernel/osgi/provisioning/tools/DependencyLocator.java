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

package org.eclipse.virgo.kernel.osgi.provisioning.tools;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.FrameworkUtil;

import org.eclipse.virgo.kernel.repository.BundleDefinition;
import org.eclipse.virgo.kernel.repository.LibraryDefinition;
import org.eclipse.virgo.kernel.repository.internal.ArtifactDescriptorBundleDefinition;
import org.eclipse.virgo.kernel.repository.internal.ArtifactDescriptorLibraryDefinition;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;

/**
 * A helper class for locating a bundle's dependencies.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * The class is <strong>thread-safe</strong>
 * 
 */
public final class DependencyLocator {
	
	private final org.eclipse.virgo.kernel.tools.DependencyLocator delegate;

    /**
     * Creates a new <code>DependencyLocator</code> that will search for dependencies within the Server instance located
     * at the supplied <code>serverHomePath</code>. To improve search performance, artifacts locations, and the
     * artifacts' metadata is indexed. The index files will be written to the directory identified by the supplied
     * <code>indexDirectoryPath</code>.
     * 
     * @param serverHomePath The path to the server installation from within which dependencies are to be located
     * @param indexDirectoryPath The path of the directory to which the index files should be written
     * @param eventLogger for logging
     * 
     * @throws IOException if a problem occurs loading and parsing the configuration of the Server instance.
     */
    public DependencyLocator(String serverHomePath, String indexDirectoryPath, EventLogger eventLogger) throws IOException {
        this(serverHomePath, null, indexDirectoryPath, eventLogger);
    }

    /**
     * Creates a new <code>DependencyLocator</code> that will search for dependencies within the Server instance located
     * at the supplied <code>serverHomePath</code>. The supplied <code>additionalSearchPaths</code> will also be
     * included in the search. Each search path is used to locate artifacts. To improve search performance, artifacts
     * locations, and the artifacts' metadata is indexed. The index files will be written to the directory identified by
     * the supplied <code>indexDirectoryPath</code>.
     * 
     * @param serverHomePath The path to the server installation from within which dependencies are to be located
     * @param additionalSearchPaths The additional search paths to use to locate the artifacts that can satisfy
     *        dependencies
     * @param indexDirectoryPath The path of the directory to which index files should be written
     * @param eventLogger for logging
     * 
     * @throws IOException if a problem occurs loading and parsing the configuration of the Server instance.
     */
    public DependencyLocator(String serverHomePath, String[] additionalSearchPaths, String indexDirectoryPath, EventLogger eventLogger)
        throws IOException {
    	
    	this.delegate = new org.eclipse.virgo.kernel.tools.DependencyLocator(serverHomePath, additionalSearchPaths, indexDirectoryPath, eventLogger, FrameworkUtil.getBundle(getClass()).getBundleContext());
    }

    /**
     * Locates all of the dependencies defined in the supplied manifest. Dependencies are identified from the manifest's
     * <code>Import-Package</code>, <code>Import-Bundle</code> and <code>Import-Library</code> headers. The dependencies
     * are returned in the form of a <code>Map</code>. Each key in the <code>Map</code> is a <code>File</code> which
     * points to the location of a bundle that satisfies one or more of the manifest's dependencies. Each
     * <code>List&lt;String&gt;</code> contains all of the packages within a particular bundle which are visible, i.e.
     * they are imported by the supplied manifest. If a dependency is found to be satisfied by the system bundle a
     * <code>null</code> key may be included in the returned <code>Map</code> if the location of the satisfying jar
     * could not be determined, e.g. because the dependency is upon a standard JRE package.
     * <p>
     * Processing of the <code>Import-Package</code> header will, for each package, search for a bundle that exports the
     * package at the specified version and include its location in the returned <code>Map</code> along with an entry in
     * the associated <code>List</code> for the package. If no such bundle is found and the import does not have a
     * resolution of optional it will be added to a list of those which cannot be satisfied and included in a
     * <code>DependencyLocationException</code> thrown once processing is complete. If more than one such bundle is
     * found they will all be added to the Map.
     * <p>
     * Processing of the <code>Import-Library</code> header will, for each library, search for a library with the
     * required name and version. The location of each bundle that is in the library will be included in the returned
     * <code>Map</code> along with entries in the associated <code>Lists</code> for every package that is exported from
     * the library's bundles. If no library with the required name and version can be found, and the import does not
     * have a resolution of optional, the import of the library will be added to a list of those which cannot be
     * satisfied and included in a <code>DependencyLocationException</code> thrown once processing is complete.
     * <p>
     * If a single bundle satisfies more than one import its location will only be included once in the returned
     * <code>Map</code>.
     * <p>
     * If the manifest has no dependencies an empty <code>Map</code> is returned.
     * 
     * @param manifest the supplied manifest
     * @return the locations of all of the given manifest's dependencies
     * @throws DependencyLocationException if any of the manifest's dependencies cannot be located
     */
    public Map<File, List<String>> locateDependencies(BundleManifest manifest) throws DependencyLocationException { 
    	try {
    		return this.delegate.locateDependencies(manifest);
    	} catch (org.eclipse.virgo.kernel.tools.DependencyLocationException foreignDle) {
    		DependencyLocationException dle = translateDependencyLocationException(foreignDle);
    		throw dle;
    	}
    }
    
    private static DependencyLocationException translateDependencyLocationException(org.eclipse.virgo.kernel.tools.DependencyLocationException foreignDle) {
    	
    	ImportDescriptor[] unsatisfiableBundleImports = translateImportDescriptors(foreignDle.getUnsatisfiableBundleImports());
    	ImportDescriptor[] unsatisfiableLibraryImports = translateImportDescriptors(foreignDle.getUnsatisfiableLibraryImports());
    	ImportDescriptor[] unsatisfiablePackageImports = translateImportDescriptors(foreignDle.getUnsatisfiablePackageImports());
    	ImportDescriptor[] unsatisfiableRequireBundle = translateImportDescriptors(foreignDle.getUnsatisfiableRequireBundle());
    	
    	Map<File, List<String>> satisfiedDependencies = foreignDle.getSatisfiedDependencies();
    	
    	return new DependencyLocationException(unsatisfiablePackageImports, unsatisfiableBundleImports, unsatisfiableLibraryImports, unsatisfiableRequireBundle, satisfiedDependencies);
	}

	private static ImportDescriptor[] translateImportDescriptors(org.eclipse.virgo.kernel.tools.ImportDescriptor[] foreignDescriptors) {
		ImportDescriptor[] descriptors = new ImportDescriptor[foreignDescriptors.length];
		for (int i = 0; i < foreignDescriptors.length; i++) {
			descriptors[i] = new ImportDescriptor(foreignDescriptors[i].getName(), foreignDescriptors[i].getVersion(), foreignDescriptors[i].getParseVersion());
		}
		return descriptors;
	}

	public Set<? extends BundleDefinition> getBundles() {
    	Set<? extends ArtifactDescriptor> bundleDescriptors = this.delegate.getBundles();
    	Set<BundleDefinition> bundleDefinitions = new HashSet<BundleDefinition>();
    	
    	for (ArtifactDescriptor bundleDescriptor: bundleDescriptors) {
    		bundleDefinitions.add(new ArtifactDescriptorBundleDefinition(bundleDescriptor));
    	}
    	
    	return bundleDefinitions;
    }

    public Set<? extends LibraryDefinition> getLibraries() {
    	Set<? extends ArtifactDescriptor> libraryDescriptors = this.delegate.getLibraries();
    	Set<LibraryDefinition> libraryDefinitions = new HashSet<LibraryDefinition>();
    	
    	for (ArtifactDescriptor libraryDescriptor : libraryDescriptors) {
    		libraryDefinitions.add(new ArtifactDescriptorLibraryDefinition(libraryDescriptor));
    	}
    	
    	return libraryDefinitions;
    }
    
    public void shutdown() {
    	this.delegate.shutdown();
    }
}
