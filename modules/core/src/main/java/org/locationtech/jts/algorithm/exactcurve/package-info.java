/*
 * Copyright (c) 2026 Jeroen Bloemscheer.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
/*
 * AI Disclosure: This file was largely AI-generated.
 * The AI-generated portions are made available under CC0-1.0 and not subject to the project's licence.
 * The human contributor has reviewed and verified that the code is correct.
 * SPDX-License-Identifier: EPL-2.0 OR EDL-1.0 and CC0-1.0
 * Assisted-by: Cursor Agent
 * Assisted-by: xAI Grok
 */

/**
 * Thin exact-curve primitives used internally by SQL/MM curve geometries.
 * <p>
 * Year-1 contains only {@link org.locationtech.jts.algorithm.exactcurve.ExactCircularArc}.
 * {@link org.locationtech.jts.algorithm.exactcurve.ExactCurve} is the six-method
 * protocol ({@code getStart}, {@code getEnd}, {@code length}, {@code pointAt},
 * {@code toLinear}, {@code isExact}). This package is not a noding API
 * and is not a rich curve-type zoo.
 */
package org.locationtech.jts.algorithm.exactcurve;
