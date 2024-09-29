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


package org.pentaho.reporting.designer.core.actions.report;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pentaho.reporting.designer.core.ReportDesignerContext;
import org.pentaho.reporting.designer.core.actions.ActionMessages;
import org.pentaho.reporting.designer.core.editor.ReportDocumentContext;
import org.pentaho.reporting.designer.core.util.exceptions.UncaughtExceptionsModel;
import org.pentaho.reporting.engine.classic.core.modules.parser.bundle.writer.BundleWriter;
import org.pentaho.reporting.libraries.base.util.FilesystemFilter;
import org.pentaho.reporting.libraries.base.util.IOUtils;
import org.pentaho.reporting.libraries.designtime.swing.filechooser.CommonFileChooser;
import org.pentaho.reporting.libraries.designtime.swing.filechooser.FileChooserService;
import org.pentaho.reporting.libraries.resourceloader.ResourceKey;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Utilitiy methods used in the process of saving reports
 */
public final class SaveReportUtilities {
  private static final Log logger = LogFactory.getLog( SaveReportUtilities.class );

  private static final String DEFAULT_EXTENSION = ".prpt";

  private SaveReportUtilities() {
  }

  /**
   * Prompts the user for the name of the report file which should be created
   *
   * @param parent      the parent component of which the file chooser dialog will be a child
   * @param defaultFile the initially selected file.
   * @return The <code>File</code> which the report should be saved into, or <code>null</code> if the user does not want
   * to continue with the save operation
   */
  public static File promptReportFilename( final Component parent, final File defaultFile ) {
    final FileFilter filter = new FilesystemFilter
      ( new String[] { DEFAULT_EXTENSION },
        ActionMessages.getString( "ReportBundleFileExtension.Description" ), true );

    final CommonFileChooser fileChooser = FileChooserService.getInstance().getFileChooser( "report" );
    fileChooser.setSelectedFile( defaultFile );
    fileChooser.setFilters( new FileFilter[] { filter } );
    logger.debug( "Prompting for save filename" ); // NON-NLS
    if ( fileChooser.showDialog( parent, JFileChooser.SAVE_DIALOG ) == false ) {
      logger.debug( "Save filename - cancel option selected" );// NON-NLS
      return null;
    }


    final File selectedFile = validateFileExtension( fileChooser.getSelectedFile(), parent );
    if ( selectedFile == null ) {
      // Cancel on another dialog
      return null;
    }

    // Once the filename has stabelized, check for overwrite
    if ( selectedFile.exists() ) {
      logger.debug( "Selected file exists [" + selectedFile.getName() + "] - prompting for overwrite..." );// NON-NLS
      final int overwrite = JOptionPane.showConfirmDialog( parent,
        ActionMessages.getString( "SaveReportUtilities.OverwriteDialog.Message",
          selectedFile.getAbsolutePath() ), ActionMessages.getString( "SaveReportUtilities.OverwriteDialog.Title" ),
        JOptionPane.YES_NO_OPTION );
      if ( overwrite == JOptionPane.NO_OPTION ) {
        return null;
      }
    }

    return selectedFile;
  }

  /**
   * Validates that the extension of the filename is prpt, and prompts the user if it is not.
   *
   * @param proposedFile the target file to validate
   * @param parent       the parent component in case we need to display a dialog
   * @return the filename based on the validation and optional prompting, or <code>null</code> if the user decided to
   * cancel the operaion
   */
  public static File validateFileExtension( final File proposedFile, final Component parent ) {
    if ( proposedFile == null ) {
      return null;
    }

    // See if we need to change the file extension
    final String s = proposedFile.getName();
    if ( s.endsWith( DEFAULT_EXTENSION ) || s.endsWith( ".prpti" ) ) {
      return proposedFile;
    }

    final String extension = IOUtils.getInstance().getFileExtension( s );
    if ( "".equals( extension ) ) {
      final File parentFile = proposedFile.getParentFile();
      if ( parentFile == null ) {
        return new File( IOUtils.getInstance().stripFileExtension( s ) + DEFAULT_EXTENSION );
      } else {
        return new File( parentFile, IOUtils.getInstance().stripFileExtension( s ) + DEFAULT_EXTENSION );
      }
    }

    logger.debug( "The selected filename does not have the standard extension - " +// NON-NLS
      "prompting the user to see if they want to change the extension" );// NON-NLS
    final int result = JOptionPane.showConfirmDialog( parent,
      ActionMessages.getString( "SaveReportUtilities.VerifyFileExtension.Message",
        proposedFile.getAbsolutePath() ), ActionMessages.getString( "SaveReportUtilities.VerifyFileExtension.Title" ),
      JOptionPane.YES_NO_CANCEL_OPTION );
    if ( result == JOptionPane.CANCEL_OPTION ) {
      return null;
    }
    if ( result == JOptionPane.NO_OPTION ) {
      return proposedFile;
    }

    final File validatedFile =
      new File( proposedFile.getParent(), IOUtils.getInstance().stripFileExtension( s ) + DEFAULT_EXTENSION );
    logger
      .debug( "User has selected YES - the filename has been changed to [" + validatedFile.getName() + "]" );// NON-NLS
    return validatedFile;
  }

  /**
   * Performs the action of saving the report to the specified target file
   *
   * @param context       the report designer context
   * @param activeContext the active render context
   * @param target        the target file to which the report will be saved
   * @return true, if saving was successful, false otherwise.
   */
  public static boolean saveReport( final ReportDesignerContext context,
                                    final ReportDocumentContext activeContext,
                                    final File target ) {
    if ( context == null ) {
      throw new IllegalArgumentException();
    }
    if ( activeContext == null ) {
      throw new IllegalArgumentException();
    }
    if ( target == null ) {
      throw new IllegalArgumentException();
    }

    try {
      // Save the report to the specified file
      logger.debug( "Saving report in filename [" + target.getAbsolutePath() + "]" );// NON-NLS
      BundleWriter.writeReportToZipFile( activeContext.getContextRoot(), target );
      context.getRecentFilesModel().addFile( target );
      return true;
    } catch ( Exception e1 ) {
      UncaughtExceptionsModel.getInstance().addException
        ( new IOException( ActionMessages.getString( "SaveReportUtilities.SaveFailed.Message" ), e1 ) );// NON-NLS
      logger.error( "Failed to save report", e1 );// NON-NLS
      return false;
    } finally {
      activeContext.resetChangeTracker();
    }
  }

  /**
   * Extracts the current definition source (if any) from the current report
   *
   * @param definitionSource the resource key used to determine the current filename
   * @return the current definition souurce, or <code>null</code> if there is none set
   */
  public static File getCurrentFile( ResourceKey definitionSource ) {
    while ( definitionSource != null ) {
      final Object identifier = definitionSource.getIdentifier();
      if ( identifier instanceof File ) {
        return (File) identifier;
      }
      definitionSource = definitionSource.getParent();
    }
    return null;
  }
}
