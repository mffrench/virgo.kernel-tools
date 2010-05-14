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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.osgi.framework.Constants;
import org.osgi.framework.Version;

import org.eclipse.virgo.util.osgi.manifest.parse.DummyParserLogger;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderDeclaration;
import org.eclipse.virgo.util.osgi.manifest.parse.HeaderParserFactory;

class EquinoxOsgiProfileParser {
    
    private static final String SYSYEM_PACKAGES_PROPERTY = "org.osgi.framework.system.packages";
    
    static Map<String, Version> parseProfileForExportedPackages(String serverProfilePath) throws IOException {
        
        Map<String, Version> systemPackages = new HashMap<String, Version>();
        
        if (serverProfilePath != null) {
            Properties properties = new Properties();
            FileInputStream propertiesStream = new FileInputStream(new File(serverProfilePath));
            try {
                properties.load(propertiesStream);
            } finally {
                if (propertiesStream != null) {
                    propertiesStream.close();
                }
            }

            String systemPackagesString = properties.getProperty(SYSYEM_PACKAGES_PROPERTY);

            List<HeaderDeclaration> exportHeaders = HeaderParserFactory.newHeaderParser(new DummyParserLogger()).parsePackageHeader(
                systemPackagesString, "Export-Package");

            for (HeaderDeclaration exportHeader : exportHeaders) {
                String versionString = getVersionAttribute(exportHeader.getAttributes());
                Version version = versionString != null ? new Version(versionString) : Version.emptyVersion;
                for (String packageName : exportHeader.getNames()) {
                    systemPackages.put(packageName, version);
                }
            }
        }
        
        return systemPackages;
    }
    
    private static String getVersionAttribute(Map<String, String> map) {
        return map.get(Constants.VERSION_ATTRIBUTE);
    }
}
