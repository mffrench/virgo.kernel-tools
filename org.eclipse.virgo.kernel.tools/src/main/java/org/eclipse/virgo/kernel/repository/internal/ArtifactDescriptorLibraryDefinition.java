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
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osgi.framework.Version;

import org.eclipse.virgo.kernel.repository.LibraryDefinition;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;

public final class ArtifactDescriptorLibraryDefinition implements LibraryDefinition {

    private final String description;

    private final String name;

    private final URI location;

    private final Version version;

    private final String symbolicName;
    
    private final List<ImportedBundle> importedBundles;
    
    private static final String LIBRARY_VERSION = "Library-Version";

    private static final String LIBRARY_SYMBOLICNAME = "Library-SymbolicName";

    private static final String IMPORT_BUNDLE = "Import-Bundle";

    private static final String LIBRARY_NAME = "Library-Name";

    private static final String LIBRARY_DESCRIPTION = "Library-Description";

    private static final String RAW_HEADER_PREFIX = "RAW_HEADER:";

    public ArtifactDescriptorLibraryDefinition(ArtifactDescriptor artifactDescriptor) {
        Set<Attribute> descriptionSet = artifactDescriptor.getAttribute(LIBRARY_DESCRIPTION);
        if (!descriptionSet.isEmpty()) {
            this.description = descriptionSet.iterator().next().getValue();
        } else {
            this.description = null;
        }

        Set<Attribute> nameSet = artifactDescriptor.getAttribute(LIBRARY_NAME);
        if (!nameSet.isEmpty()) {
            this.name = nameSet.iterator().next().getValue();
        } else {
            this.name = null;
        }

        this.location = artifactDescriptor.getUri();

        Set<Attribute> versionSet = artifactDescriptor.getAttribute(LIBRARY_VERSION);
        this.version = new Version(versionSet.iterator().next().getValue());

        Set<Attribute> symbolicNameSet = artifactDescriptor.getAttribute(LIBRARY_SYMBOLICNAME);
        Attribute symbolicNameAttribute = symbolicNameSet.iterator().next();

        this.symbolicName = symbolicNameAttribute.getValue();

        String importBundleHeader = artifactDescriptor.getAttribute(RAW_HEADER_PREFIX + IMPORT_BUNDLE).iterator().next().getValue();

        this.importedBundles = parseImportBundle(importBundleHeader);
    }

    public String getDescription() {
        return this.description;
    }
    
    public static List<ImportedBundle> parseImportBundle(String importBundleString) {
        Dictionary<String, String> headers = new Hashtable<String, String>();
        headers.put(IMPORT_BUNDLE, importBundleString);
        BundleManifest manifest = BundleManifestFactory.createBundleManifest(headers);
        return manifest.getImportBundle().getImportedBundles();
    }

    public List<ImportedBundle> getLibraryBundles() {
        return this.importedBundles;
    }

    public String getName() {
        return this.name;
    }

    public String getSymbolicName() {
        return this.symbolicName;
    }

    public Version getVersion() {
        return this.version;
    }

    public URI getLocation() {
        return this.location;
    }
}
