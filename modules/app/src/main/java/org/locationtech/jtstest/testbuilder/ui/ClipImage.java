/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v20.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */

package org.locationtech.jtstest.testbuilder.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;

public class ClipImage implements Transferable, ClipboardOwner {

	private byte[] image;

	public ClipImage(byte[] im)
	{
		image = im;
	}

	@Override
	public DataFlavor[] getTransferDataFlavors()
	{
		return new DataFlavor[] { DataFlavor.imageFlavor };
	}

	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor)
	{
		return DataFlavor.imageFlavor.equals(flavor);
	}

	@Override
	public Object getTransferData(DataFlavor flavor) throws
	UnsupportedFlavorException
	{
		if (!isDataFlavorSupported(flavor))
			throw new UnsupportedFlavorException(flavor);
		return Toolkit.getDefaultToolkit().createImage(image);
	}

	@Override
	public void lostOwnership(java.awt.datatransfer.Clipboard clip,
	java.awt.datatransfer.Transferable tr)

	{
		return;
	}
}
