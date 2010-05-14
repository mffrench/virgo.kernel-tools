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

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.repository.internal.ArtifactDescriptorBundleDefinition;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Repository;
import org.eclipse.virgo.repository.RepositoryAwareArtifactDescriptor;
import org.eclipse.virgo.util.osgi.VersionRange;

/**
 * A {@link BundleRepository} implementation that is backed by a {@link Repository}.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.

 */
public class RepositoryBackedBundleRepository implements BundleRepository {
 
    protected final Repository repository;

    private static final String TYPE_ATTRIBUTE_KEY = "type";

    public RepositoryBackedBundleRepository(Repository repository) {
        this.repository = repository;
    }

    public Set<? extends BundleDefinition> findByExportedPackage(String packageName, VersionRange versionRange) {
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.repository.createQuery(Constants.EXPORT_PACKAGE, packageName).run();
        return createBundleDefinitions(artefacts);
    }

    public Set<? extends BundleDefinition> findByFragmentHost(String bundleSymbolicName, Version version) {
        Set<RepositoryAwareArtifactDescriptor> artefacts = this.repository.createQuery(Constants.FRAGMENT_HOST, bundleSymbolicName).run();
        return createBundleDefinitions(artefacts);
    }

    public BundleDefinition findBySymbolicName(String symbolicName, VersionRange versionRange) {
        
        ArtifactDescriptor artifactDescriptor = this.repository.get(BundleBridge.BRIDGE_TYPE, symbolicName, versionRange);
        
        if (artifactDescriptor != null) {
            return new ArtifactDescriptorBundleDefinition(artifactDescriptor);
        }
        
        return null;
    }

    public LibraryDefinition findLibrary(String libraryName, VersionRange versionRange) {
        ArtifactDescriptor artefact = this.repository.get("library", libraryName, versionRange);
        
        if (artefact != null) {
            return new org.eclipse.virgo.kernel.repository.internal.ArtifactDescriptorLibraryDefinition(artefact);
        }
        
        return null;                
    }
    
    public ArtifactDescriptor findSubsystem(String subsystemName) {
    	throw new UnsupportedOperationException();        
    }

    LibraryDefinition pickLibraryDefinition(Set<ArtifactDescriptor> artefacts, VersionRange versionRange) {
        LibraryDefinition bestCandidate = null;
        for (ArtifactDescriptor artefact : artefacts) {
            LibraryDefinition current = new org.eclipse.virgo.kernel.repository.internal.ArtifactDescriptorLibraryDefinition(artefact);
            if (versionRange.includes(current.getVersion())) {
                if (bestCandidate == null || bestCandidate.getVersion().compareTo(current.getVersion()) == -1) {
                    bestCandidate = current;
                }
            }
        }
        return bestCandidate;
    }

    public Set<? extends BundleDefinition> getBundles() {
        Set<RepositoryAwareArtifactDescriptor> bundleArtifactDescriptors = this.repository.createQuery(TYPE_ATTRIBUTE_KEY, BundleBridge.BRIDGE_TYPE).run();

        Set<BundleDefinition> bundleDefinitions = new HashSet<BundleDefinition>();

        for (ArtifactDescriptor bundleArtifactDescriptor : bundleArtifactDescriptors) {
            bundleDefinitions.add(new ArtifactDescriptorBundleDefinition(bundleArtifactDescriptor));
        }
        return bundleDefinitions;
    }

    public Set<? extends LibraryDefinition> getLibraries() {
        Set<RepositoryAwareArtifactDescriptor> libraryArtifactDescriptors = this.repository.createQuery(TYPE_ATTRIBUTE_KEY, "library").run();

        Set<LibraryDefinition> libraryDefinitions = new HashSet<LibraryDefinition>();

        for (ArtifactDescriptor libraryArtifactDescriptor : libraryArtifactDescriptors) {
            libraryDefinitions.add(new org.eclipse.virgo.kernel.repository.internal.ArtifactDescriptorLibraryDefinition(libraryArtifactDescriptor));
        }
        return libraryDefinitions;
    }

    public void refresh() {
    	throw new UnsupportedOperationException();
    }

    private Set<? extends BundleDefinition> createBundleDefinitions(Set<RepositoryAwareArtifactDescriptor> artefacts) {
        Set<BundleDefinition> bundleDefinitions = new HashSet<BundleDefinition>();

        for (ArtifactDescriptor artefact : artefacts) {
            bundleDefinitions.add(new ArtifactDescriptorBundleDefinition(artefact));
        }

        return bundleDefinitions;
    }
    
    public void shutdown() {
    	this.repository.stop();
    }

}
