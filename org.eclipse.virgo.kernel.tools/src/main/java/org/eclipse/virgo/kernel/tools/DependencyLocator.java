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
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;

import org.eclipse.virgo.kernel.artifact.bundle.BundleBridge;
import org.eclipse.virgo.kernel.artifact.library.LibraryBridge;
import org.eclipse.virgo.kernel.artifact.library.LibraryDefinition;
import org.eclipse.virgo.kernel.tools.internal.BundleManifestUtils;
import org.eclipse.virgo.kernel.tools.internal.SystemPackageFilteringRepository;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.repository.ArtifactDescriptor;
import org.eclipse.virgo.util.osgi.manifest.VersionRange;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.ExportedPackage;
import org.eclipse.virgo.util.osgi.manifest.ImportedBundle;
import org.eclipse.virgo.util.osgi.manifest.ImportedLibrary;
import org.eclipse.virgo.util.osgi.manifest.ImportedPackage;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle;
import org.eclipse.virgo.util.osgi.manifest.Resolution;
import org.eclipse.virgo.util.osgi.manifest.RequiredBundle.Visibility;

/**
 * A helper class for locating a bundle's dependencies.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * The class is <strong>thread-safe</strong>
 * 
 */
public final class DependencyLocator {

    private final SystemPackageFilteringRepository repository;

    /**
     * Creates a new <code>DependencyLocator</code> that will search for dependencies within the kernel instance located
     * at the supplied <code>kernelHomePath</code>. To improve search performance, artifacts locations, and the
     * artifacts' metadata is indexed. The index files will be written to the directory identified by the supplied
     * <code>indexDirectoryPath</code>.
     * 
     * @param kernelHomePath The path to the kernel installation from within which dependencies are to be located
     * @param indexDirectoryPath The path of the directory to which the index files should be written
     * @param eventLogger The <code>EventLogger</code> to use to log events during dependency location
     * @param bundleContext The <code>BundleContext</code> to be used for service lookups
     * 
     * @throws IOException if a problem occurs loading and parsing the configuration of the Server instance.
     */
    public DependencyLocator(String kernelHomePath, String indexDirectoryPath, EventLogger eventLogger, BundleContext bundleContext) throws IOException {
        this(kernelHomePath, null, indexDirectoryPath, eventLogger, bundleContext);
    }

    /**
     * Creates a new <code>DependencyLocator</code> that will search for dependencies within the Kernel instance located
     * at the supplied <code>kernelHomePath</code>. The supplied <code>additionalSearchPaths</code> will also be
     * included in the search. Each search path is used to locate artifacts. To improve search performance, artifacts
     * locations, and the artifacts' metadata is indexed. The index files will be written to the directory identified by
     * the supplied <code>indexDirectoryPath</code>.
     * 
     * @param kernelHomePath The path to the kernel installation from within which dependencies are to be located
     * @param additionalSearchPaths The additional search paths to use to locate the artifacts that can satisfy
     *        dependencies
     * @param indexDirectoryPath The path of the directory to which index files should be written
     * @param eventLogger The <code>EventLogger</code> to use to log events during dependency location
     * @param bundleContext The <code>BundleContext</code> to be used for service lookups 
     * 
     * @throws IOException if a problem occurs loading and parsing the configuration of the Server instance.
     */
    public DependencyLocator(String kernelHomePath, String[] additionalSearchPaths, String indexDirectoryPath, EventLogger eventLogger, BundleContext bundleContext)
        throws IOException {
        this.repository = new SystemPackageFilteringRepository(kernelHomePath, additionalSearchPaths, indexDirectoryPath,
            eventLogger, bundleContext);
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
     * @param manifest supplied bundle manifest
     * @return the locations of all of the given manifest's dependencies
     * @throws DependencyLocationException if any of the manifest's dependencies cannot be located
     */
    public Map<File, List<String>> locateDependencies(BundleManifest manifest) throws DependencyLocationException {

        List<ImportDescriptor> unsatisfiablePackageImports = new ArrayList<ImportDescriptor>();
        List<ImportDescriptor> unsatisfiableLibraryImports = new ArrayList<ImportDescriptor>();
        List<ImportDescriptor> unsatisfiableBundleImports = new ArrayList<ImportDescriptor>();
        List<ImportDescriptor> unsatisfiableRequireBundles = new ArrayList<ImportDescriptor>();

        Map<File, List<String>> dependencyLocations = new HashMap<File, List<String>>();

        processImportedPackages(manifest.getImportPackage().getImportedPackages(), dependencyLocations, unsatisfiablePackageImports);

        processImportedLibraries(manifest.getImportLibrary().getImportedLibraries(), dependencyLocations, unsatisfiableLibraryImports);

        processImportedBundles(manifest.getImportBundle().getImportedBundles(), dependencyLocations, unsatisfiableBundleImports);

        List<String> packageNames = createListOfAllPackagesThatHaveAlreadyBeenSatisfied(dependencyLocations);
        processRequiredBundles(manifest.getRequireBundle().getRequiredBundles(), dependencyLocations, unsatisfiableRequireBundles, packageNames, true);

        throwDependencyLocationExceptionIfNecessary(unsatisfiablePackageImports, unsatisfiableBundleImports, unsatisfiableLibraryImports,
            unsatisfiableRequireBundles, dependencyLocations);

        return dependencyLocations;
    }
    
    public Set<? extends ArtifactDescriptor> getBundles() {
    	return this.repository.getBundles();
    }
    
    public Set<? extends ArtifactDescriptor> getLibraries() {
    	return this.repository.getLibraries();
    }

    public void shutdown() {
        this.repository.shutdown();
    }

    private static List<String> createListOfAllPackagesThatHaveAlreadyBeenSatisfied(Map<File, List<String>> dependencyLocations) {
        List<String> packagesThatHaveBeenSatisfied = new ArrayList<String>();
        for (Entry<File, List<String>> dependencyLocation : dependencyLocations.entrySet()) {
            packagesThatHaveBeenSatisfied.addAll(dependencyLocation.getValue());

        }
        return packagesThatHaveBeenSatisfied;
    }

    private void processImportedBundles(List<ImportedBundle> importedBundles, Map<File, List<String>> dependencyLocations,
        List<ImportDescriptor> unsatisfiableBundleImports) {
        for (ImportedBundle importedBundle : importedBundles) {
            processImportedBundle(importedBundle, dependencyLocations, unsatisfiableBundleImports);
        }
    }

    private void processImportedBundle(ImportedBundle importedBundle, Map<File, List<String>> dependencyLocations,
        List<ImportDescriptor> unsatisfiableBundleImports) {
        String symbolicName = importedBundle.getBundleSymbolicName();
        VersionRange bundleVersionRange = importedBundle.getVersion();
        ArtifactDescriptor bundleDescriptor = findBundle(symbolicName, bundleVersionRange);
        
        if (bundleDescriptor == null) {
            unsatisfiableBundleImports.add(new ImportDescriptor(symbolicName, bundleVersionRange.toString(), bundleVersionRange.toParseString()));
        } else {
            registerDependencyLocationAndPackageNameForEveryExportedPackage(dependencyLocations, BundleManifestUtils.createBundleManifest(bundleDescriptor), null, bundleDescriptor.getUri());
        }
    }

    private void processImportedLibraries(List<ImportedLibrary> importedLibraries, Map<File, List<String>> dependencyLocations,
        List<ImportDescriptor> unsatisfiableLibraryImports) {
        for (ImportedLibrary importedLibrary : importedLibraries) {
            String libraryName = importedLibrary.getLibrarySymbolicName();
            VersionRange versionRange = importedLibrary.getVersion();
            ArtifactDescriptor libraryDescriptor = findLibrary(libraryName, versionRange);            

            if (libraryDescriptor != null) {
            	LibraryDefinition libraryDefinition = LibraryBridge.createLibraryDefinition(libraryDescriptor);
                List<ImportedBundle> importedBundles = libraryDefinition.getLibraryBundles();

                for (ImportedBundle libraryBundle : importedBundles) {
                    String symbolicName = libraryBundle.getBundleSymbolicName();
                    VersionRange bundleVersionRange = libraryBundle.getVersion();
                    
                    ArtifactDescriptor bundleDescriptor = findBundle(symbolicName, bundleVersionRange);

                    if (bundleDescriptor == null) {
                        unsatisfiableLibraryImports.add(new ImportDescriptor(libraryName, versionRange.toString(), versionRange.toParseString()));
                    } else {
                        registerDependencyLocationAndPackageNameForEveryExportedPackage(dependencyLocations, BundleManifestUtils.createBundleManifest(bundleDescriptor), null, bundleDescriptor.getUri());
                    }
                }
            } else if (Resolution.MANDATORY.equals(importedLibrary.getResolution())) {
                unsatisfiableLibraryImports.add(new ImportDescriptor(libraryName, versionRange.toString(), versionRange.toParseString()));
            }
        }
    }

    private void registerDependencyLocationAndPackageNameForEveryExportedPackage(Map<File, List<String>> dependencyLocations,
        BundleManifest manifest, List<String> packagesThatHaveAlreadyBeenSatisfied, URI location) {        
        for (ExportedPackage exportedPackage : manifest.getExportPackage().getExportedPackages()) {
            if (packagesThatHaveAlreadyBeenSatisfied == null || !packagesThatHaveAlreadyBeenSatisfied.contains(exportedPackage.getPackageName())) {
                registerPackageNameAgainstDependencyLocation(location, exportedPackage.getPackageName(), dependencyLocations);
            }
        }
    }

    private void processImportedPackages(List<ImportedPackage> importedPackages, Map<File, List<String>> dependencyLocations,
        List<ImportDescriptor> unsatisfiablePackageImports) {
        for (ImportedPackage importedPackage : importedPackages) {

            VersionRange versionRange = importedPackage.getVersion();
            String packageName = importedPackage.getPackageName();
            Set<ArtifactDescriptor> bundleDescriptors = this.repository.findByExportedPackage(packageName, versionRange);
            if (bundleDescriptors.size() > 0) {
                for (ArtifactDescriptor bundleDescriptor : bundleDescriptors) {
                    registerPackageNameAgainstDependencyLocation(bundleDescriptor.getUri(), packageName, dependencyLocations);
                }
            } else if (Resolution.MANDATORY.equals(importedPackage.getResolution())) {
                unsatisfiablePackageImports.add(new ImportDescriptor(packageName, versionRange.toString(), versionRange.toParseString()));
            }

        }
    }

    private void processRequiredBundles(List<RequiredBundle> requiredBundles, Map<File, List<String>> dependencyLocations,
        List<ImportDescriptor> unsatisfiableRequireBundles, List<String> packagesThatHaveAlreadyBeenSatisfied, boolean root) {

        for (RequiredBundle requiredBundle : requiredBundles) {
            if (root || Visibility.REEXPORT.equals(requiredBundle.getVisibility())) {
                String bundleSymbolicName = requiredBundle.getBundleSymbolicName();
                String bundleVersion = requiredBundle.getAttributes().get(Constants.BUNDLE_VERSION_ATTRIBUTE);
                VersionRange versionRange;
                if (bundleVersion != null) {
                    versionRange = new VersionRange(bundleVersion);
                } else {
                    versionRange = VersionRange.NATURAL_NUMBER_RANGE;
                }
                ArtifactDescriptor artifactDescriptor = findBundle(bundleSymbolicName, versionRange);
                if (artifactDescriptor != null) {
                	BundleManifest bundleManifest = BundleManifestUtils.createBundleManifest(artifactDescriptor);
                    registerDependencyLocationAndPackageNameForEveryExportedPackage(dependencyLocations, bundleManifest,
                        packagesThatHaveAlreadyBeenSatisfied, artifactDescriptor.getUri());
                    List<RequiredBundle> dependencysRequiredBundles = bundleManifest.getRequireBundle().getRequiredBundles();
                    processRequiredBundles(dependencysRequiredBundles, dependencyLocations, unsatisfiableRequireBundles,
                        packagesThatHaveAlreadyBeenSatisfied, false);
                } else if (Resolution.MANDATORY.equals(requiredBundle.getResolution())) {
                    unsatisfiableRequireBundles.add(new ImportDescriptor(requiredBundle.getBundleSymbolicName(), versionRange.toString(),
                        versionRange.toParseString()));
                }
            }
        }
    }
    
    private ArtifactDescriptor findBundle(String symbolicName, VersionRange versionRange) {
    	return this.repository.get(BundleBridge.BRIDGE_TYPE, symbolicName, versionRange);
    }
    
    private ArtifactDescriptor findLibrary(String symbolicName, VersionRange versionRange) {
    	return this.repository.get(LibraryDefinition.LIBRARY_TYPE, symbolicName, versionRange);
    }
    
    private void registerPackageNameAgainstDependencyLocation(URI location, String packageName, Map<File, List<String>> dependencyLocations) {
        File fileLocation;
        if (location != null) {
            fileLocation = new File(location);
        } else {
            fileLocation = null;
        }
        List<String> existingPackageNames = dependencyLocations.get(fileLocation);
        if (existingPackageNames == null) {
            existingPackageNames = new ArrayList<String>();
            dependencyLocations.put(fileLocation, existingPackageNames);
        }
        existingPackageNames.add(packageName);
    }

    private void throwDependencyLocationExceptionIfNecessary(List<ImportDescriptor> unsatisfiablePackageImports,
        List<ImportDescriptor> unsatisfiableBundleImports, List<ImportDescriptor> unsatisfiableLibraryImports,
        List<ImportDescriptor> unsatisfiableRequireBundles, Map<File, List<String>> satisfiedDependencies) {

        if (!unsatisfiableLibraryImports.isEmpty() || !unsatisfiablePackageImports.isEmpty() || !unsatisfiableRequireBundles.isEmpty()
            || !unsatisfiableBundleImports.isEmpty()) {
            throw new DependencyLocationException(toArray(unsatisfiablePackageImports), toArray(unsatisfiableBundleImports),
                toArray(unsatisfiableLibraryImports), toArray(unsatisfiableRequireBundles), satisfiedDependencies);
        }
    }

    private static ImportDescriptor[] toArray(List<ImportDescriptor> importDescriptors) {
        return importDescriptors.toArray(new ImportDescriptor[importDescriptors.size()]);
    }
}
