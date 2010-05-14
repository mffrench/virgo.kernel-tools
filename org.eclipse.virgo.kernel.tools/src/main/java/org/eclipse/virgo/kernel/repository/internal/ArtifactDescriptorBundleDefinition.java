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

package org.eclipse.virgo.kernel.repository.internal;

import java.net.URI;
import java.util.Dictionary;

import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.repository.BundleDefinition;
import org.eclipse.virgo.kernel.tools.internal.SystemPackageFilteringRepository.SystemBundleDescriptor;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;

public final class ArtifactDescriptorBundleDefinition implements BundleDefinition {
	
	private final URI location;
	
	private final BundleManifest bundleManifest;
		
	public ArtifactDescriptorBundleDefinition(ArtifactDescriptor artifactDescriptor) {
		this.location = artifactDescriptor.getUri();
		this.bundleManifest = getBundleManifest(artifactDescriptor); 
	}

	public BundleManifest getManifest() {
		return this.bundleManifest;
	}

	public URI getLocation() {
		return this.location;
	}
	
	private static BundleManifest getBundleManifest(ArtifactDescriptor descriptor) {
		if (descriptor instanceof SystemBundleDescriptor) {
			return ((SystemBundleDescriptor)descriptor).getBundleManifest();
		} else {
			Dictionary<String, String> dictionary = BundleBridge.convertToDictionary(descriptor);
			return BundleManifestFactory.createBundleManifest(dictionary);
		}
    }
}
