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
package org.locationtech.jtstest.testbuilder.event;

import java.util.EventObject;

/**
 * Title:
 * Description:
 * Copyright:    Copyright (c) 2001
 * Company:
 * @author
 * @version 1.7
 */
public class SpatialFunctionPanelEvent extends EventObject {

  private boolean createNew = false;
  
    public SpatialFunctionPanelEvent(Object source) {
        super(source);
    }

    public SpatialFunctionPanelEvent(Object source,
        boolean createNew) {
      super(source);
      this. createNew = createNew;
    }
    
    public boolean isCreateNew() 
    {
      return createNew;
    }
}
