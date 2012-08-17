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

package org.eclipse.virgo.kernel.osgi.provisioning.tools.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocationException;
import org.eclipse.virgo.kernel.osgi.provisioning.tools.DependencyLocator;
import org.eclipse.virgo.kernel.repository.BundleDefinition;
import org.eclipse.virgo.kernel.repository.LibraryDefinition;
import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;
import org.eclipse.virgo.test.framework.OsgiTestRunner;
import org.eclipse.virgo.util.osgi.manifest.BundleManifest;
import org.eclipse.virgo.util.osgi.manifest.BundleManifestFactory;

/**
 */
@RunWith(OsgiTestRunner.class)
public class DependencyLocatorTests {

    private final String[] searchPaths = new String[] { new File(
        "./src/test/resources/dependency-locator/manifests/{library}.libd").getAbsolutePath() };
    
    private DependencyLocator locator;
    
    @Before
    public void createDependencyLocator() throws IOException {
        this.locator = new DependencyLocator(new File("src/test/resources/dependency-locator/").getAbsolutePath(), searchPaths,
        "target/temp", new NoOpEventLogger());
    }
    
    private static final class NoOpEventLogger implements EventLogger {

        public void log(LogEvent logEvent, Object... inserts) {
        }

        public void log(String code, Level level, Object... inserts) {
        }

        public void log(LogEvent logEvent, Throwable throwable, Object... inserts) {
        }

        public void log(String code, Level level, Throwable throwable, Object... inserts) {
        }
    }

    @Before
    public void clearTempDirectory() {
        File tempDir = new File("target/temp/");
        File[] files = tempDir.listFiles();
        if (files != null) {
            for (File file : files) {
                file.delete();
            }
        }
        tempDir.delete();
        tempDir.mkdirs();
    }

    @Test
    public void emptyManifest() {
    

        BundleManifest manifest = BundleManifestFactory.createBundleManifest();
        Map<File, List<String>> dependencies = locator.locateDependencies(manifest);
        assertTrue(dependencies.isEmpty());
    }

    @Test
    public void importPackage() throws IOException {

        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/IMPORTPACKAGE.MF")));
        Map<File, List<String>> dependencies = locator.locateDependencies(manifest);
        assertTrue(dependencies.size() == 2);
        for (File dependencyLocation : dependencies.keySet()) {
            List<String> packageNames = dependencies.get(dependencyLocation);
            if (packageNames.contains("org.springframework.beans")) {
                assertTrue(packageNames.contains("org.springframework.beans.factory"));
                assertTrue(packageNames.size() == 2);
            } else {
                assertTrue(packageNames.contains("org.springframework.context"));
                assertTrue(packageNames.size() == 1);
            }
        }
    }

    @Test
    public void unsatisfiableImportPackage() throws IOException {        

        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/UNSATISFIABLEIMPORTPACKAGE.MF")));
        try {
            locator.locateDependencies(manifest);
            fail();
        } catch (DependencyLocationException dle) {
            assertTrue(dle.getUnsatisfiableLibraryImports().length == 0);
            assertTrue(dle.getUnsatisfiableRequireBundle().length == 0);
            assertTrue(dle.getUnsatisfiablePackageImports().length == 1);
            assertTrue(dle.getUnsatisfiableBundleImports().length == 0);
            assertTrue("com.foo.bar".equals(dle.getUnsatisfiablePackageImports()[0].getName()));
        }
    }

    @Test
    public void importLibrary() throws IOException {

        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/IMPORTLIBRARY.MF")));
        Map<File, List<String>> dependencies = locator.locateDependencies(manifest);
        assertTrue(dependencies.size() + " dependencies were located.", dependencies.size() == 3);

        for (File dependencyLocation : dependencies.keySet()) {

            List<String> packageNames = dependencies.get(dependencyLocation);
            assertFalse(packageNames.isEmpty());

            if (dependencyLocation.getName().contains("context")) {
                assertTrue(packageNames.size() == 59);
            } else if (dependencyLocation.getName().contains("core")) {
                assertTrue(packageNames.size() == 17);
            } else if (dependencyLocation.getName().contains("beans")) {
                assertTrue(packageNames.size() == 14);
            }
        }
    }

    @Test
    public void importBundle() throws IOException {

        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/IMPORTBUNDLE.MF")));
        Map<File, List<String>> dependencies = locator.locateDependencies(manifest);
        assertTrue(dependencies.size() + " dependencies were located.", dependencies.size() == 3);

        for (File dependencyLocation : dependencies.keySet()) {

            List<String> packageNames = dependencies.get(dependencyLocation);
            assertFalse(packageNames.isEmpty());

            if (dependencyLocation.getName().contains("context")) {
                assertTrue(packageNames.size() == 59);
            } else if (dependencyLocation.getName().contains("core")) {
                assertTrue(packageNames.size() == 17);
            } else if (dependencyLocation.getName().contains("beans")) {
                assertTrue(packageNames.size() == 14);
            }
        }
    }

    @Test
    public void unsatisfiableImportBundle() throws IOException {        

        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/UNSATISFIABLEIMPORTBUNDLE.MF")));

        try {
            locator.locateDependencies(manifest);
            fail();
        } catch (DependencyLocationException dle) {
            assertTrue(dle.getUnsatisfiablePackageImports().length == 0);
            assertTrue(dle.getUnsatisfiableRequireBundle().length == 0);
            assertTrue(dle.getUnsatisfiableLibraryImports().length == 0);
            assertTrue(dle.getUnsatisfiableBundleImports().length == 2);
            assertTrue("com.foo".equals(dle.getUnsatisfiableBundleImports()[0].getName()));
            assertTrue("com.bar".equals(dle.getUnsatisfiableBundleImports()[1].getName()));
        }
    }

    @Test
    public void unsatisfiableImportLibrary() throws IOException {


        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/UNSATISFIABLEIMPORTLIBRARY.MF")));
        try {
            locator.locateDependencies(manifest);
            fail();
        } catch (DependencyLocationException dle) {
            assertTrue(dle.getUnsatisfiablePackageImports().length == 0);
            assertTrue(dle.getUnsatisfiableRequireBundle().length == 0);
            assertTrue(dle.getUnsatisfiableLibraryImports().length == 1);
            assertTrue(dle.getUnsatisfiableBundleImports().length == 0);
            assertTrue("com.foo.bar".equals(dle.getUnsatisfiableLibraryImports()[0].getName()));
        }
    }

    @Test
    public void requireBundle() throws IOException {


        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/REQUIREBUNDLE.MF")));
        Map<File, List<String>> dependencies = locator.locateDependencies(manifest);

        assertTrue(dependencies.size() == 1);
        List<String> packageNames = dependencies.entrySet().iterator().next().getValue();
        assertTrue(packageNames.size() == 17);
        assertTrue(packageNames.get(0).equals("org.springframework.core"));
    }

    @Test
    public void unsatisfiableRequireBundle() throws IOException {


        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/UNSATISFIABLEREQUIREBUNDLE.MF")));
        try {
            locator.locateDependencies(manifest);
            fail();
        } catch (DependencyLocationException dle) {
            assertTrue(dle.getUnsatisfiablePackageImports().length == 0);
            assertTrue(dle.getUnsatisfiableLibraryImports().length == 0);
            assertTrue(dle.getUnsatisfiableRequireBundle().length == 1);
            assertTrue(dle.getUnsatisfiableBundleImports().length == 0);
            assertTrue("com.foo.bar".equals(dle.getUnsatisfiableRequireBundle()[0].getName()));
        }
    }

    @Test
    public void testSatisfiableAndUnsatisfiable() throws IOException {


        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/SATISFIABLEANDUNSATISFIABLE.MF")));

        try {
            locator.locateDependencies(manifest);
            fail();
        } catch (DependencyLocationException dle) {
            Map<File, List<String>> dependencies = dle.getSatisfiedDependencies();
            assertTrue(dependencies.size() + " dependencies were located.", dependencies.size() == 3);
            for (File dependencyLocation : dependencies.keySet()) {

                List<String> packageNames = dependencies.get(dependencyLocation);
                assertFalse(packageNames.isEmpty());

                if (dependencyLocation.getName().contains("context")) {
                    assertTrue(packageNames.size() == 59);
                } else if (dependencyLocation.getName().contains("core")) {
                    assertTrue(packageNames.size() == 17);
                } else if (dependencyLocation.getName().contains("beans")) {
                    assertTrue(packageNames.size() == 14);
                }
            }
        }
    }

    @Test
    public void testImportOfSystemPackagesWithJava6Profile() throws IOException {
        BundleManifest manifest = BundleManifestFactory.createBundleManifest(new FileReader(new File(
            "src/test/resources/dependency-locator/manifests/IMPORTSYSTEMPACKAGESJAVA6.MF")));
        Map<File, List<String>> dependencies = locator.locateDependencies(manifest);

        Set<File> keys = dependencies.keySet();
        assertTrue(keys.size() == 2);
        
        Iterator<File> iterator = keys.iterator();
        File key1 = iterator.next();
        File key2 = iterator.next();
        if (key1 == null && key2 != null) {
        	checkSetKeys(dependencies, key1, key2);
        } else if (key1 != null && key2 == null) {
        	checkSetKeys(dependencies, key2, key1);
        } else {
        	fail("Expected one null valued key and one pointing to true bundle location. Got different key configuration: " + dependencies.toString());
        }
    }

    private void checkSetKeys(Map<File, List<String>> dependencies, File nullKey, File nonNullKey) {
		assertNull(nullKey);
        List<String> packages = dependencies.get(nullKey);
        assertNotNull(packages);
        assertEquals(1, packages.size());
        assertEquals("javax.xml.soap", packages.get(0));

        assertEquals(new File("src/test/resources/dependency-locator/plugins/org.eclipse.osgi-3.4.0.v20080529-1200.jar").getAbsoluteFile(), nonNullKey);
        packages = dependencies.get(nonNullKey);
        assertNotNull(packages);
        assertEquals(1, packages.size());
        assertEquals("org.osgi.framework", packages.get(0));
	}

    @Test
    public void getBundles() {

        Set<? extends BundleDefinition> bundles = locator.getBundles();
        assertEquals(4, bundles.size());
    }

    @Test
    public void getLibraries() {

        Set<? extends LibraryDefinition> libraries = locator.getLibraries();
        assertEquals(1, libraries.size());
    }	
}
