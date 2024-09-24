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

package org.pentaho.reporting.engine.classic.demo.ancient.demo.opensource;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.URL;
import javax.swing.JComponent;
import javax.swing.table.TableModel;

import org.pentaho.reporting.engine.classic.core.ClassicEngineBoot;
import org.pentaho.reporting.engine.classic.core.ElementAlignment;
import org.pentaho.reporting.engine.classic.core.ItemBand;
import org.pentaho.reporting.engine.classic.core.MasterReport;
import org.pentaho.reporting.engine.classic.core.PageFooter;
import org.pentaho.reporting.engine.classic.core.ReportProcessingException;
import org.pentaho.reporting.engine.classic.core.TableDataFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.NumberFieldElementFactory;
import org.pentaho.reporting.engine.classic.core.elementfactory.TextFieldElementFactory;
import org.pentaho.reporting.engine.classic.core.function.PageFunction;
import org.pentaho.reporting.engine.classic.core.modules.output.table.xls.ExcelReportUtil;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleKeys;
import org.pentaho.reporting.engine.classic.core.style.ElementStyleSheet;
import org.pentaho.reporting.engine.classic.core.style.TextStyleKeys;
import org.pentaho.reporting.engine.classic.demo.util.AbstractDemoHandler;
import org.pentaho.reporting.engine.classic.demo.util.ReportDefinitionException;
import org.pentaho.reporting.libraries.base.util.FloatDimension;
import org.pentaho.reporting.libraries.base.util.ObjectUtilities;

/**
 * This demo application replicates the report generated by OpenSourceXMLDemoHandler.java, but creates the report in
 * code rather than using an XML report template.
 *
 * @author David Gilbert
 */
public class OpenSourceAPIDemoHandler extends AbstractDemoHandler
{
  /**
   * The data for the report.
   */
  private TableModel data;

  public OpenSourceAPIDemoHandler()
  {
    data = new OpenSourceProjects();
  }

  public JComponent getPresentationComponent()
  {
    return createDefaultTable(data);
  }

  public URL getDemoDescriptionSource()
  {
    return ObjectUtilities.getResourceRelative
        ("opensource-api.html", OpenSourceAPIDemoHandler.class);
  }

  public String getDemoName()
  {
    return "Open Source Demo (API)";
  }

  public MasterReport createReport() throws ReportDefinitionException
  {
    final MasterReport report = createStaticReport();
    report.setDataFactory(new TableDataFactory("default", data));
    return report;
  }

  /**
   * Creates a report definition in code.
   * <p/>
   * It is more base to read the definition from an XML report template file, but sometimes you might need to create a
   * report dynamically.
   *
   * @return a report.
   */
  public static MasterReport createStaticReport()
  {

    final MasterReport result = new MasterReport();

    // set up the functions...
    final PageFunction f1 = new PageFunction("page_number");
    result.addExpression(f1);

    // set up the item band...
    final ItemBand itemBand = result.getItemBand();
    configureItemBand(itemBand);

    // set up the page footer...
    final PageFooter pageFooter = result.getPageFooter();
    configurePageFooter(pageFooter);

    return result;

  }

  /**
   * Configures a blank item band.
   *
   * @param band the item band to be configured.
   */
  private static void configureItemBand(final ItemBand band)
  {
    final ElementStyleSheet ess = band.getStyle();
    ess.setStyleProperty(TextStyleKeys.FONT, "SansSerif");
    ess.setStyleProperty(TextStyleKeys.FONTSIZE, new Integer(9));

    TextFieldElementFactory factory = new TextFieldElementFactory();
    factory.setName("Name_Field");
    factory.setAbsolutePosition(new Point2D.Float(0, 2));
    factory.setMinimumSize(new FloatDimension(140, 10));
    factory.setColor(Color.black);
    factory.setHorizontalAlignment(ElementAlignment.LEFT);
    factory.setVerticalAlignment(ElementAlignment.BOTTOM);
    factory.setFontName("SansSerif");
    factory.setFontSize(new Integer(10));
    factory.setBold(Boolean.TRUE);
    factory.setNullString("No Name");
    factory.setFieldname("Name");
    band.addElement(factory.createElement());

    factory = new TextFieldElementFactory();
    factory.setName("URL_Field");
    factory.setAbsolutePosition(new Point2D.Float(140, 2));
    factory.setMinimumSize(new FloatDimension(-100, 10));
    factory.setColor(Color.black);
    factory.setHorizontalAlignment(ElementAlignment.RIGHT);
    factory.setVerticalAlignment(ElementAlignment.BOTTOM);
    factory.setFontName("Monospaced");
    factory.setFontSize(new Integer(8));
    factory.setNullString("No URL");
    factory.setFieldname("URL");
    band.addElement(factory.createElement());

    factory = new TextFieldElementFactory();
    factory.setName("Description_Field");
    factory.setAbsolutePosition(new Point2D.Float(0, 20));
    factory.setMinimumSize(new FloatDimension(-100, 10));
    factory.setColor(Color.black);
    factory.setHorizontalAlignment(ElementAlignment.LEFT);
    factory.setVerticalAlignment(ElementAlignment.TOP);
    factory.setNullString("No description available");
    factory.setFieldname("Description");
    factory.setDynamicHeight(Boolean.TRUE);
    band.addElement(factory.createElement());
  }

  /**
   * Configures a blank page footer.
   *
   * @param footer the page footer to be configured.
   */
  private static void configurePageFooter(final PageFooter footer)
  {
    footer.getStyle().setStyleProperty(ElementStyleKeys.MIN_HEIGHT, new Float(20));

    final ElementStyleSheet ess = footer.getStyle();
    ess.setStyleProperty(TextStyleKeys.FONT, "SansSerif");
    ess.setStyleProperty(TextStyleKeys.FONTSIZE, new Integer(9));

    final NumberFieldElementFactory factory = new NumberFieldElementFactory();
    factory.setName("PageNumber_Field");
    factory.setAbsolutePosition(new Point2D.Float(0, 0));
    factory.setMinimumSize(new Dimension(-100, -100));
    factory.setColor(Color.black);
    factory.setHorizontalAlignment(ElementAlignment.RIGHT);
    factory.setNullString("-");
    factory.setFormatString("Page 0");
    factory.setFieldname("page_number");
    factory.setFontName("SansSerif");
    factory.setFontSize(new Integer(10));
    factory.setBold(Boolean.TRUE);
    footer.addElement(factory.createElement());
  }

  public static void main(final String[] args)
      throws ReportDefinitionException, ReportProcessingException, IOException
  {
    ClassicEngineBoot.getInstance().start();

    final OpenSourceAPIDemoHandler handler = new OpenSourceAPIDemoHandler();
    final MasterReport report = handler.createReport();
    ExcelReportUtil.createXLS(report, "/tmp/out.xls");

  }
}
