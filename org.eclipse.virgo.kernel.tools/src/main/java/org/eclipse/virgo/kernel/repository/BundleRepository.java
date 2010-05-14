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

package org.eclipse.virgo.kernel.repository;

import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.Version;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.util.osgi.VersionRange;

/**
 * Maintains a registry of known {@link Bundle bundles} and libraries.<p/>
 * 
 * <code>Bundles</code> can be queried by symbolic name, exported package, or fragment host. In the case of symbolic
 * name and exported package a {@link VersionRange} can be supplied and in the case of fragment host a {@link Version}
 * can be supplied to constrain the set of matched locations.<p/>
 * 
 * <strong>Concurrent Semantics</strong><br/>
 * 
 * Implementations <strong>must</strong> be thread safe.
 * 
 */
public interface BundleRepository {

    /**
     * Find a {@link BundleDefinition} with the supplied <code>Bundle</code> symbolic name and a bundle version that
     * is in the supplied {@link VersionRange}. If multiple bundles match, the one with the highest version is
     * returned.
     * 
     * @param symbolicName the <code>Bundle</code> symbolic name to match against.
     * @param versionRange the range of <code>Bundle</code> versions to match against.
     * @return the matching <code>BundleDefinition</code>
     */
    // TODO Return multiple bundles?
    // TODO Consider removing version range parameter
    BundleDefinition findBySymbolicName(String symbolicName, VersionRange versionRange);

    /**
     * Find all the {@link BundleDefinition BundleDefinitions} that have a <code>Fragment-Host</code> for the
     * specified bundle symbolic name within the specified {@link VersionRange}. The order in which matching
     * <code>BundleDefinitions</code> are found should be reflected in the ordering of the entries in the returned
     * <code>Set</code>.
     * 
     * @param bundleSymbolicName the host <code>Bundle</code> symbolic name to match against.
     * @param version the range of <code>Bundle</code> versions to match against.
     * @return the matching <code>BundleDefinitions</code>
     */
    Set<? extends BundleDefinition> findByFragmentHost(String bundleSymbolicName, Version version);

    /**
     * Find all the {@link BundleDefinition BundleDefinitions} that export the specified package within the specified
     * {@link VersionRange}. The order in which matching <code>BundleDefinitions</code> are found should be reflected
     * in the ordering of the entries in the returned <code>Set</code>.
     * 
     * @param packageName the package name to match against.
     * @param versionRange the range of package versions to match against.
     * @return the matching <code>BundleDefinitions</code>
     */
    Set<? extends BundleDefinition> findByExportedPackage(String packageName, VersionRange versionRange);

    /**
     * Finds the {@link LibraryDefinition} with the supplied <code>libraryName</code> and a
     * {@link LibraryDefinition#getVersion() version} that is in the supplied <code>versionRange</code>. If multiple
     * libraries match, the one with the highest version is returned.
     * 
     * @param libraryName the name of the library.
     * @param versionRange the range of versions to search.
     * @return the matched {@link LibraryDefinition}, or <code>null</code> if no match is found.
     */
    // TODO Return multiple libraries?
    // TODO Consider removing version range parameter
    LibraryDefinition findLibrary(String libraryName, VersionRange versionRange);
    
    ArtifactDescriptor findSubsystem(String subsystemName);

    /**
     * Instructs this repository to refresh its registry of libraries and bundles.
     */
    void refresh();

    /**
     * Returns all of the {@link BundleDefinition BundleDefinitions} that are known to this repository.
     * 
     * @return this repository's <code>BundleDefinitions</code>.
     */
    Set<? extends BundleDefinition> getBundles();

    /**
     * Returns all of the {@link LibraryDefinition LibraryDefinitions} that are known to this repository.
     * 
     * @return this repository's <code>LibraryDefinitions</code>.
     */
    Set<? extends LibraryDefinition> getLibraries();
}
