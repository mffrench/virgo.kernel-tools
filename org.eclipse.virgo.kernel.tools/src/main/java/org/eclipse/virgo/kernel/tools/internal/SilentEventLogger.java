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

import org.eclipse.virgo.medic.eventlog.EventLogger;
import org.eclipse.virgo.medic.eventlog.Level;
import org.eclipse.virgo.medic.eventlog.LogEvent;

final class SilentEventLogger implements EventLogger {

    public void log(LogEvent logEvent, Object... inserts) {
    }

    public void log(String code, Level level, Object... inserts) {
    }

    public void log(LogEvent logEvent, Throwable throwable, Object... inserts) {
    }

    public void log(String code, Level level, Throwable throwable, Object... inserts) {
    }
}
