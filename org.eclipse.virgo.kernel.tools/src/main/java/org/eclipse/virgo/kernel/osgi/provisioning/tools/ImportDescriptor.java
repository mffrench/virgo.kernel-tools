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

/**
 * A descriptor of an import in a bundle manifest. An import can be of either a package or a library.
 * 
 * <strong>Concurrent Semantics</strong><br />
 * This class is <strong>thread-safe</strong>.
 * 
 */
public class ImportDescriptor {

    private final String name;

    private final String version;

    private final String parseVersion;

    ImportDescriptor(String name, String version, String parseVersion) {
        this.name = name;
        this.version = version;
        this.parseVersion = parseVersion;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getParseVersion() {
        return parseVersion;
    }
}
