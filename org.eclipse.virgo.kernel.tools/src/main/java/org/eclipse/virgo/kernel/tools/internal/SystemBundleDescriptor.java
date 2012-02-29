package org.eclipse.virgo.kernel.tools.internal;

import java.util.Set;

import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.repository.Attribute;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.osgi.framework.Version;

public final class SystemBundleDescriptor implements ArtifactDescriptor {

	private static final String TYPE_BUNDLE = "bundle";
	
	private final BundleManifest bundleManifest;

	public SystemBundleDescriptor(BundleManifest bundleManifest) {
		this.bundleManifest = bundleManifest;
	}

	public Set<Attribute> getAttribute(String name) {
		return null;
	}

	public Set<Attribute> getAttributes() {
		return null;
	}

	public String getFilename() {
		return null;
	}

	public String getName() {
		return null;
	}

	public String getType() {
		return TYPE_BUNDLE;
	}

	public java.net.URI getUri() {
		return null;
	}

	public Version getVersion() {
		return null;
	}

	public BundleManifest getBundleManifest() {
		return this.bundleManifest;
	}		
}