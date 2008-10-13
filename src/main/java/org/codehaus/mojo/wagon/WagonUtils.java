package org.codehaus.mojo.wagon;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author Dan T. Tran
 * 
 * @plexus.component role="org.codehaus.mojo.wagon.WagonHelpers" role-hint="default"
 */

public class WagonUtils
    implements WagonHelpers
{
    public List getFileList( Wagon wagon, String remotePath, boolean recursive, Log logger )
        throws WagonException
    {
        WagonDirectoryScan dirScan = new WagonDirectoryScan();
        dirScan.setWagon( wagon );
        if ( ! recursive )
        {
            String [] excludes = new String[1];
            excludes[0] = "**/*";
            dirScan.setExcludes( excludes );
        }
        
        dirScan.scan();
        
        return dirScan.getFilesIncluded();
        
        /*
        logger.debug( "Listing " + wagon.getRepository().getUrl() + " ..." );

        ArrayList fileList = new ArrayList();

        scanRemoteRepo( wagon, remotePath, fileList, recursive, logger );

        Collections.sort( fileList );

        return fileList;
        */
    }

    public void download( Wagon wagon, RemoteFileSet remoteFileSet, Log logger )
        throws WagonException
    {
        String remotePath = remoteFileSet.getRemotePath();
        File downloadDirectory = remoteFileSet.getDownloadDirectory();
        boolean recursive = remoteFileSet.isRecursive();

        List fileList = this.getFileList( wagon, remotePath, recursive, logger );

        String url = wagon.getRepository().getUrl() + "/";

        for ( Iterator iterator = fileList.iterator(); iterator.hasNext(); )
        {
            String remoteFile = (String) iterator.next();

            File destination = new File( downloadDirectory + "/" + remoteFile );

            logger.info( "Downloading " + url + remoteFile + " to " + destination + " ..." );

            wagon.get( remoteFile, destination );
        }
    }

    public void upload( Wagon wagon, FileSet fileset, Log logger )
        throws WagonException
    {
        logger.info( "Uploading " + fileset );

        FileSetManager fileSetManager = new FileSetManager( logger, logger.isDebugEnabled() );

        String[] files = fileSetManager.getIncludedFiles( fileset );

        String url = wagon.getRepository().getUrl() + "/";

        for ( int i = 0; i < files.length; ++i )
        {
            String relativeDestPath = StringUtils.replace( files[i], "\\", "/" );

            if ( !StringUtils.isBlank( fileset.getOutputDirectory() ) )
            {
                relativeDestPath = fileset.getOutputDirectory() + "/" + relativeDestPath;
            }

            File source = new File( fileset.getDirectory(), files[i] );

            logger.info( "Uploading " + source + " to " + url + relativeDestPath + " ..." );

            wagon.put( source, relativeDestPath );
        }

    }

    ///////////////////////////////////////////////////////////////////////////

    private void scanRemoteRepo( Wagon wagon, String basePath, List collected, boolean recursive, Log logger )
        throws WagonException
    {
        logger.debug( "Scanning " + basePath + " ..." );

        List files = wagon.getFileList( basePath );

        if ( files.isEmpty() )
        {
            logger.debug( "Found empty directory: " + basePath );
            return;
        }

        for ( Iterator iterator = files.iterator(); iterator.hasNext(); )
        {
            String filePath = (String) iterator.next();

            if ( filePath.endsWith( "." ) ) //including ".."
            {
                continue;
            }

            if ( !StringUtils.isBlank( basePath ) )
            {
                if ( basePath.endsWith( "/" ) )
                {
                    filePath = basePath + filePath;
                }
                else
                {
                    filePath = basePath + "/" + filePath; // no separator ??? 
                }
            }

            if ( this.isDirectory( wagon, filePath ) )
            {
                if ( recursive )
                {
                    this.scanRemoteRepo( wagon, filePath, collected, recursive, logger );
                }
            }
            else
            {
                logger.debug( "Found file " + filePath );
                collected.add( filePath );
            }
        }
    }

    private boolean isFile( Wagon wagon, String remotePath )
        throws WagonException
    {
        if ( wagon.resourceExists( remotePath ) )
        {
            if ( !wagon.resourceExists( remotePath + "/" ) )
            {
                //yeah, it is a file
                return true;
            }
        }

        //not exists or a directory
        return false;
    }

    private boolean isDirectory( Wagon wagon, String existedRemotePath )
        throws WagonException
    {
        if ( existedRemotePath.endsWith( "/" ) )
        {
            return true;
        }

        return wagon.resourceExists( existedRemotePath + "/" );
    }

}
