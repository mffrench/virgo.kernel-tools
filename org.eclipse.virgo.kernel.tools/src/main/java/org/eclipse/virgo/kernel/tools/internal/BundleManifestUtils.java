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

package org.eclipse.virgo.kernel.tools.internal;

import java.util.Dictionary;

import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;


/**
 * <code>BundleManifestUtils</code> provides a number of utility methods for
 * working with {@link BundleManifest BundleManifests}.
 * <p />
 *
 * <strong>Concurrent Semantics</strong><br />
 * Thread-safe.
 *
 */
public final class BundleManifestUtils {

    /**
     * Creates a {@link BundleManifest} from the supplied {@link ArtifactDescriptor}.
     * @param descriptor The <code>ArtifactDescriptor> from which the manifest is created
     * @return the created manifest
     */
    public static BundleManifest createBundleManifest(ArtifactDescriptor descriptor) {
    	Dictionary<String, String> dictionary = BundleBridge.convertToDictionary(descriptor);
    	return BundleManifestFactory.createBundleManifest(dictionary);
    }
}
