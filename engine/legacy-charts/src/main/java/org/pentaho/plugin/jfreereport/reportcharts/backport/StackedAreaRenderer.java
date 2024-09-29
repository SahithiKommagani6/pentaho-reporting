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


package org.pentaho.plugin.jfreereport.reportcharts.backport;

import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.entity.EntityCollection;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.AreaRenderer;
import org.jfree.chart.renderer.category.CategoryItemRendererState;
import org.jfree.data.DataUtilities;
import org.jfree.data.Range;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.ui.RectangleEdge;
import org.jfree.util.PublicCloneable;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.io.Serializable;


/**
 * This is a backport of the StackedAreaRenderer of JFreeChart 1.0.13; this class will be removed as soon as we are able
 * to upgrade to a more recent version of JFreeChart.
 * <p/>
 * A renderer that draws stacked area charts for a {@link org.jfree.chart.plot.CategoryPlot}. The example shown here is
 * generated by the <code>StackedAreaChartDemo1.java</code> program included in the JFreeChart Demo Collection: <br><br>
 * <img src="../../../../../images/StackedAreaRendererSample.png" alt="StackedAreaRendererSample.png" />
 */
public class StackedAreaRenderer extends AreaRenderer
  implements PublicCloneable, Serializable {

  /**
   * For serialization.
   */
  private static final long serialVersionUID = -3595635038460823663L;

  /**
   * A flag that controls whether the areas display values or percentages.
   */
  private boolean renderAsPercentages;

  /**
   * Creates a new renderer.
   */
  public StackedAreaRenderer() {
    this( false );
  }

  /**
   * Creates a new renderer.
   *
   * @param renderAsPercentages a flag that controls whether the data values are rendered as percentages.
   */
  public StackedAreaRenderer( final boolean renderAsPercentages ) {
    super();
    this.renderAsPercentages = renderAsPercentages;
  }

  /**
   * Returns <code>true</code> if the renderer displays each item value as a percentage (so that the stacked areas add
   * to 100%), and <code>false</code> otherwise.
   *
   * @return A boolean.
   * @since 1.0.3
   */
  public boolean getRenderAsPercentages() {
    return this.renderAsPercentages;
  }

  /**
   * Sets the flag that controls whether the renderer displays each item value as a percentage (so that the stacked
   * areas add to 100%), and sends a {@link org.jfree.chart.event.RendererChangeEvent} to all registered listeners.
   *
   * @param asPercentages the flag.
   * @since 1.0.3
   */
  public void setRenderAsPercentages( final boolean asPercentages ) {
    this.renderAsPercentages = asPercentages;
    fireChangeEvent();
  }

  /**
   * Returns the number of passes (<code>2</code>) required by this renderer. The first pass is used to draw the
   * areas,
   * the second pass is used to draw the item labels (if visible).
   *
   * @return The number of passes required by the renderer.
   */
  public int getPassCount() {
    return 2;
  }

  /**
   * Returns the range of values the renderer requires to display all the items from the specified dataset.
   *
   * @param dataset the dataset (<code>null</code> not permitted).
   * @return The range (or <code>null</code> if the dataset is empty).
   */
  public Range findRangeBounds( final CategoryDataset dataset ) {
    if ( dataset == null ) {
      return null;
    }
    if ( this.renderAsPercentages ) {
      return new Range( 0.0, 1.0 );
    } else {
      return DatasetUtilities.findStackedRangeBounds( dataset );
    }
  }

  /**
   * Draw a single data item.
   *
   * @param g2         the graphics device.
   * @param state      the renderer state.
   * @param dataArea   the data plot area.
   * @param plot       the plot.
   * @param domainAxis the domain axis.
   * @param rangeAxis  the range axis.
   * @param dataset    the data.
   * @param row        the row index (zero-based).
   * @param column     the column index (zero-based).
   * @param pass       the pass index.
   */
  public void drawItem( final Graphics2D g2,
                        final CategoryItemRendererState state,
                        final Rectangle2D dataArea,
                        final CategoryPlot plot,
                        final CategoryAxis domainAxis,
                        final ValueAxis rangeAxis,
                        final CategoryDataset dataset,
                        final int row,
                        final int column,
                        final int pass ) {

    if ( !isSeriesVisible( row ) ) {
      return;
    }

    if ( ( pass == 1 ) && !isItemLabelVisible( row, column ) ) {
      return;
    }

    // setup for collecting optional entity info...
    Shape entityArea = null;
    final EntityCollection entities = state.getEntityCollection();

    double y1 = 0.0;
    Number n = dataset.getValue( row, column );
    if ( n != null ) {
      y1 = n.doubleValue();
      if ( this.renderAsPercentages ) {
        final double total = DataUtilities.calculateColumnTotal( dataset, column );
        y1 = y1 / total;
      }
    }
    final double[] stack1 = getStackValues( dataset, row, column );


    // leave the y values (y1, y0) untranslated as it is going to be be
    // stacked up later by previous series values, after this it will be
    // translated.
    double xx1 = domainAxis.getCategoryMiddle( column, getColumnCount(),
      dataArea, plot.getDomainAxisEdge() );


    // get the previous point and the next point so we can calculate a
    // "hot spot" for the area (used by the chart entity)...
    double y0 = 0.0;
    n = dataset.getValue( row, Math.max( column - 1, 0 ) );
    if ( n != null ) {
      y0 = n.doubleValue();
      if ( this.renderAsPercentages ) {
        final double total = DataUtilities.calculateColumnTotal( dataset, Math.max( column - 1, 0 ) );
        y0 = y0 / total;
      }
    }
    final double[] stack0 = getStackValues( dataset, row, Math.max( column - 1, 0 ) );

    // FIXME: calculate xx0
    double xx0 = domainAxis.getCategoryStart( column, getColumnCount(),
      dataArea, plot.getDomainAxisEdge() );

    final int itemCount = dataset.getColumnCount();
    double y2 = 0.0;
    n = dataset.getValue( row, Math.min( column + 1, itemCount - 1 ) );
    if ( n != null ) {
      y2 = n.doubleValue();
      if ( this.renderAsPercentages ) {
        final double total = DataUtilities.calculateColumnTotal( dataset,
          Math.min( column + 1, itemCount - 1 ) );
        y2 = y2 / total;
      }
    }
    final double[] stack2 = getStackValues( dataset, row, Math.min( column + 1, itemCount - 1 ) );

    double xx2 = domainAxis.getCategoryEnd( column, getColumnCount(),
      dataArea, plot.getDomainAxisEdge() );

    // This gets rid of the white lines between most category values
    // Doug Moran - Hitachi Vantara
    xx0 = Math.round( xx0 );
    xx1 = Math.round( xx1 );
    xx2 = Math.round( xx2 );

    // FIXME: calculate xxLeft and xxRight
    final double xxLeft = xx0;
    final double xxRight = xx2;

    final double[] stackLeft = averageStackValues( stack0, stack1 );
    final double[] stackRight = averageStackValues( stack1, stack2 );
    final double[] adjStackLeft = adjustedStackValues( stack0, stack1 );
    final double[] adjStackRight = adjustedStackValues( stack1, stack2 );

    final float transY1;

    final RectangleEdge edge1 = plot.getRangeAxisEdge();

    final GeneralPath left = new GeneralPath();
    final GeneralPath right = new GeneralPath();
    if ( y1 >= 0.0 ) {  // handle positive value
      transY1 = (float) rangeAxis.valueToJava2D( y1 + stack1[ 1 ], dataArea,
        edge1 );
      final float transStack1 = (float) rangeAxis.valueToJava2D( stack1[ 1 ],
        dataArea, edge1 );
      final float transStackLeft = (float) rangeAxis.valueToJava2D(
        adjStackLeft[ 1 ], dataArea, edge1 );

      // LEFT POLYGON
      if ( y0 >= 0.0 ) {
        final double yleft = ( y0 + y1 ) / 2.0 + stackLeft[ 1 ];
        final float transYLeft
          = (float) rangeAxis.valueToJava2D( yleft, dataArea, edge1 );
        left.moveTo( (float) xx1, transY1 );
        left.lineTo( (float) xx1, transStack1 );
        left.lineTo( (float) xxLeft, transStackLeft );
        left.lineTo( (float) xxLeft, transYLeft );
        left.closePath();
      } else {
        left.moveTo( (float) xx1, transStack1 );
        left.lineTo( (float) xx1, transY1 );
        left.lineTo( (float) xxLeft, transStackLeft );
        left.closePath();
      }

      final float transStackRight = (float) rangeAxis.valueToJava2D(
        adjStackRight[ 1 ], dataArea, edge1 );
      // RIGHT POLYGON
      if ( y2 >= 0.0 ) {
        final double yright = ( y1 + y2 ) / 2.0 + stackRight[ 1 ];
        final float transYRight
          = (float) rangeAxis.valueToJava2D( yright, dataArea, edge1 );
        right.moveTo( (float) xx1, transStack1 );
        right.lineTo( (float) xx1, transY1 );
        right.lineTo( (float) xxRight, transYRight );
        right.lineTo( (float) xxRight, transStackRight );
        right.closePath();
      } else {
        right.moveTo( (float) xx1, transStack1 );
        right.lineTo( (float) xx1, transY1 );
        right.lineTo( (float) xxRight, transStackRight );
        right.closePath();
      }
    } else {  // handle negative value
      transY1 = (float) rangeAxis.valueToJava2D( y1 + stack1[ 0 ], dataArea,
        edge1 );
      final float transStack1 = (float) rangeAxis.valueToJava2D( stack1[ 0 ],
        dataArea, edge1 );
      final float transStackLeft = (float) rangeAxis.valueToJava2D(
        adjStackLeft[ 0 ], dataArea, edge1 );

      // LEFT POLYGON
      if ( y0 >= 0.0 ) {
        left.moveTo( (float) xx1, transStack1 );
        left.lineTo( (float) xx1, transY1 );
        left.lineTo( (float) xxLeft, transStackLeft );
        left.clone();
      } else {
        final double yleft = ( y0 + y1 ) / 2.0 + stackLeft[ 0 ];
        final float transYLeft = (float) rangeAxis.valueToJava2D( yleft,
          dataArea, edge1 );
        left.moveTo( (float) xx1, transY1 );
        left.lineTo( (float) xx1, transStack1 );
        left.lineTo( (float) xxLeft, transStackLeft );
        left.lineTo( (float) xxLeft, transYLeft );
        left.closePath();
      }
      final float transStackRight = (float) rangeAxis.valueToJava2D(
        adjStackRight[ 0 ], dataArea, edge1 );

      // RIGHT POLYGON
      if ( y2 >= 0.0 ) {
        right.moveTo( (float) xx1, transStack1 );
        right.lineTo( (float) xx1, transY1 );
        right.lineTo( (float) xxRight, transStackRight );
        right.closePath();
      } else {
        final double yright = ( y1 + y2 ) / 2.0 + stackRight[ 0 ];
        final float transYRight = (float) rangeAxis.valueToJava2D( yright,
          dataArea, edge1 );
        right.moveTo( (float) xx1, transStack1 );
        right.lineTo( (float) xx1, transY1 );
        right.lineTo( (float) xxRight, transYRight );
        right.lineTo( (float) xxRight, transStackRight );
        right.closePath();
      }
    }

    if ( pass == 0 ) {
      final Paint itemPaint = getItemPaint( row, column );
      g2.setPaint( itemPaint );
      g2.fill( left );
      g2.fill( right );

      // add an entity for the item...
      if ( entities != null ) {
        final GeneralPath gp = new GeneralPath( left );
        gp.append( right, false );
        entityArea = gp;
        addItemEntity( entities, dataset, row, column, entityArea );
      }
    } else if ( pass == 1 ) {
      drawItemLabel( g2, plot.getOrientation(), dataset, row, column,
        xx1, transY1, y1 < 0.0 );
    }

  }

  /**
   * Calculates the stacked values (one positive and one negative) of all series up to, but not including,
   * <code>series</code> for the specified item. It returns [0.0, 0.0] if <code>series</code> is the first series.
   *
   * @param dataset the dataset (<code>null</code> not permitted).
   * @param series  the series index.
   * @param index   the item index.
   * @return An array containing the cumulative negative and positive values for all series values up to but excluding
   * <code>series</code> for <code>index</code>.
   */
  protected double[] getStackValues( final CategoryDataset dataset,
                                     final int series, final int index ) {
    final double[] result = new double[ 2 ];
    double total = 0.0;
    if ( this.renderAsPercentages ) {
      total = DataUtilities.calculateColumnTotal( dataset, index );
    }
    for ( int i = 0; i < series; i++ ) {
      if ( isSeriesVisible( i ) ) {
        double v = 0.0;
        final Number n = dataset.getValue( i, index );
        if ( n != null ) {
          v = n.doubleValue();
          if ( this.renderAsPercentages ) {
            v = v / total;
          }
        }
        if ( !Double.isNaN( v ) ) {
          if ( v >= 0.0 ) {
            result[ 1 ] += v;
          } else {
            result[ 0 ] += v;
          }
        }
      }
    }
    return result;
  }

  /**
   * Returns a pair of "stack" values calculated as the mean of the two specified stack value pairs.
   *
   * @param stack1 the first stack pair.
   * @param stack2 the second stack pair.
   * @return A pair of average stack values.
   */
  private double[] averageStackValues( final double[] stack1, final double[] stack2 ) {
    final double[] result = new double[ 2 ];
    result[ 0 ] = ( stack1[ 0 ] + stack2[ 0 ] ) / 2.0;
    result[ 1 ] = ( stack1[ 1 ] + stack2[ 1 ] ) / 2.0;
    return result;
  }

  /**
   * Calculates adjusted stack values from the supplied values.  The value is the mean of the supplied values, unless
   * either of the supplied values is zero, in which case the adjusted value is zero also.
   *
   * @param stack1 the first stack pair.
   * @param stack2 the second stack pair.
   * @return A pair of average stack values.
   */
  private double[] adjustedStackValues( final double[] stack1, final double[] stack2 ) {
    final double[] result = new double[ 2 ];
    if ( stack1[ 0 ] == 0.0 || stack2[ 0 ] == 0.0 ) {
      result[ 0 ] = 0.0;
    } else {
      result[ 0 ] = ( stack1[ 0 ] + stack2[ 0 ] ) / 2.0;
    }
    if ( stack1[ 1 ] == 0.0 || stack2[ 1 ] == 0.0 ) {
      result[ 1 ] = 0.0;
    } else {
      result[ 1 ] = ( stack1[ 1 ] + stack2[ 1 ] ) / 2.0;
    }
    return result;
  }

  /**
   * Checks this instance for equality with an arbitrary object.
   *
   * @param obj the object (<code>null</code> not permitted).
   * @return A boolean.
   */
  public boolean equals( final Object obj ) {
    if ( obj == this ) {
      return true;
    }
    if ( !( obj instanceof StackedAreaRenderer ) ) {
      return false;
    }
    final StackedAreaRenderer that = (StackedAreaRenderer) obj;
    if ( this.renderAsPercentages != that.renderAsPercentages ) {
      return false;
    }
    return super.equals( obj );
  }

  /**
   * Calculates the stacked value of the all series up to, but not including <code>series</code> for the specified
   * category, <code>category</code>. It returns 0.0 if <code>series</code> is the first series, i.e. 0.
   *
   * @param dataset  the dataset (<code>null</code> not permitted).
   * @param series   the series.
   * @param category the category.
   * @return double returns a cumulative value for all series' values up to but excluding <code>series</code> for Object
   * <code>category</code>.
   * @deprecated As of 1.0.13, as the method is never used internally.
   */
  protected double getPreviousHeight( final CategoryDataset dataset,
                                      final int series, final int category ) {

    double result = 0.0;
    Number n;
    double total = 0.0;
    if ( this.renderAsPercentages ) {
      total = DataUtilities.calculateColumnTotal( dataset, category );
    }
    for ( int i = 0; i < series; i++ ) {
      n = dataset.getValue( i, category );
      if ( n != null ) {
        double v = n.doubleValue();
        if ( this.renderAsPercentages ) {
          v = v / total;
        }
        result += v;
      }
    }
    return result;

  }

}
