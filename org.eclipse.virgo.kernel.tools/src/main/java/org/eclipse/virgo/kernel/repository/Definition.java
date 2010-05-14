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

import java.net.URI;

/**
 * Describes an entity (library or bundle) that can be installed into the OSGi framework.
 * <p />
 * 
 * <strong>Concurrent Semantics</strong><br />
 * Implementations <strong>must</strong> be thread-safe.
 * 
 */
public interface Definition {

    /**
     * Returns the location of the library or bundle which this <code>Definition</code> represents.
     * 
     * @return the location of the library or bundle that is being defined.
     */
    URI getLocation();
}
