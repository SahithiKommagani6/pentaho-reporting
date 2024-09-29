/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package org.pentaho.reporting.engine.classic.core;

import javax.swing.table.TableModel;

public interface DataFactoryDesignTimeSupport extends DataFactory {
  public static final String DESIGN_TIME = "::org.pentaho.reporting::design-time";

  public TableModel queryDesignTimeStructure( String query, DataRow parameter ) throws ReportDataFactoryException;
}
