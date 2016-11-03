/*
 * Copyright (c) 2016 Vivid Solutions.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v. 1.0 which accompanies this distribution.
 * The Eclipse Public License is available at http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *
 * http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.locationtech.jtstest.testbuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.border.*;
import javax.swing.tree.*;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jtstest.function.*;
import org.locationtech.jtstest.util.*;


/**
 * @version 1.7
 */
public class GeometryTreePanel extends JPanel implements TreeWillExpandListener 
{
  JScrollPane jScrollPane = new JScrollPane();
	JTree tree = new JTree();
	BorderLayout borderLayout = new BorderLayout();
	Border border1;

	private class GeometryTreeCellRenderer extends DefaultTreeCellRenderer {
		public GeometryTreeCellRenderer() {
		}

		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
					hasFocus);
			if (! (value instanceof GeometricObjectNode))
				return this;
			
			GeometricObjectNode o = (GeometricObjectNode) value;
			setText(o.getText());
			setIcon(o.getIcon());
			setToolTipText("geometry"); 
			return this;
		}
	}

	public GeometryTreePanel() {
	  // default empty model
	  tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode("No geometry shown")));
	  //((DefaultMutableTreeNode)  (tree.getRoot())).removeAllChildren();
		try {
			initUI();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void initUI() throws Exception {
		setSize(200, 250);
		border1 = BorderFactory.createEmptyBorder(4, 4, 4, 4);
		setLayout(borderLayout);
		setBorder(border1);
		add(jScrollPane, BorderLayout.CENTER);
		jScrollPane.getViewport().add(tree, null);

		tree.setRootVisible(true);
		tree.setShowsRootHandles(true);
		tree.setCellRenderer(new GeometryTreeCellRenderer());
		tree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		// stop expansion with double-click
		tree.setToggleClickCount(0);


		tree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
			  Geometry geom = getSelectedGeometry();
			  if (geom == null) return;
			  
        if (e.getClickCount() == 2) {
          JTSTestBuilderFrame.getGeometryEditPanel().zoom(geom.getEnvelopeInternal());
        }
        // would be nice to flash as well as zoom, but zooming drawing is too slow
        if (e.getClickCount() == 1) {
          JTSTestBuilderFrame.getGeometryEditPanel().flash(geom);
        }
			}
		});
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				//GeometryFunction fun = getFunction();
				//if (fun != null)
					//fireFunctionSelected(new GeometryFunctionEvent(fun));
			}
		});
	}
  public Geometry getSelectedGeometry() {
    return getGeometryFromNode(tree.getLastSelectedPathComponent());
  }
  public void moveToNextNode(int direction) {
    direction = (int) Math.signum(direction);
    TreePath path = tree.getSelectionPath();
    
    TreePath nextPath2 = nextPath(path, 2 * direction);
    tree.scrollPathToVisible(nextPath2);
    
    TreePath nextPath = nextPath(path, direction);
    tree.setSelectionPath(nextPath);
  }

  private TreePath nextPath(TreePath path, int offset) {
    GeometricObjectNode node = (GeometricObjectNode) path.getLastPathComponent();
    TreePath parentPath = path.getParentPath();
    GeometricObjectNode parent = (GeometricObjectNode) parentPath.getLastPathComponent();
    int index = parent.getIndexOfChild(node);
    int nextIndex = index + offset;
    if (nextIndex < 0) {
      nextIndex = 0;
    }
    else if (nextIndex >= parent.getChildCount() ) {
      nextIndex = parent.getChildCount() - 1;
    }
    GeometricObjectNode nextNode = parent.getChildAt(nextIndex);
    TreePath nextPath = parentPath.pathByAddingChild(nextNode);
    return nextPath;
  }

  private static Geometry getGeometryFromNode(Object value) {
    if (value == null) 
      return null;
    return ((GeometricObjectNode) value).getGeometry();
  }

	public void populate(Geometry geom, int source) {
		tree.setModel(new GeometryTreeModel(geom, source));
	}

  //Required by TreeWillExpandListener interface.
  public void treeWillExpand(TreeExpansionEvent e) 
              throws ExpandVetoException {
  	TreePath path = e.getPath();
  	Object lastComp = path.getLastPathComponent(); 
  }

  //Required by TreeWillExpandListener interface.
  public void treeWillCollapse(TreeExpansionEvent e) {
    // take no action
  }

}
