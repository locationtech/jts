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
package org.locationtech.jtstest.testbuilder;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Comparator;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.border.Border;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import org.locationtech.jts.geom.Geometry;


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

		@Override
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
			@Override
			public void mouseClicked(MouseEvent e) {
			  Geometry geom = getSelectedGeometry();
			  if (geom == null) return;
			  
        if (e.getClickCount() == 2) {
          JTSTestBuilderFrame.getGeometryEditPanel().zoom(geom.getEnvelopeInternal());
        }
        // would be nice to flash as well as zoom, but zooming drawing is too slow
        if (e.getClickCount() == 1) {
          JTSTestBuilder.controller().flash(geom);
        }
			}
		});
		tree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				//GeometryFunction fun = getFunction();
				//if (fun != null)
					//fireFunctionSelected(new GeometryFunctionEvent(fun));
			}
		});
	}
	/**
	 * Gets currently selected geometry, if any.
	 * 
	 * @return selected geometry, or null if none selected
	 */
  public Geometry getSelectedGeometry() {
    return getGeometryFromNode(tree.getLastSelectedPathComponent());
  }
  public void moveToNextNode(int direction) {
    direction = (int) Math.signum(direction);
    TreePath path = tree.getSelectionPath();
    if (path == null)
      return;
    
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
    tree.setModel(new GeometryTreeModel(geom, source, null));
  }

  public void populate(Geometry geom, int source, Comparator comp) {
    tree.setModel(new GeometryTreeModel(geom, source, comp));
  }

  //Required by TreeWillExpandListener interface.
  @Override
  public void treeWillExpand(TreeExpansionEvent e)
              throws ExpandVetoException {
  	TreePath path = e.getPath();
  	Object lastComp = path.getLastPathComponent(); 
  }

  //Required by TreeWillExpandListener interface.
  @Override
  public void treeWillCollapse(TreeExpansionEvent e) {
    // take no action
  }

}
