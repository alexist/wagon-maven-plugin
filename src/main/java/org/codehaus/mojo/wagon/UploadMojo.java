package org.codehaus.mojo.wagon;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.wagon.Wagon;
import org.apache.maven.wagon.WagonException;
import org.codehaus.plexus.util.StringUtils;

/**
 * Upload multiple sets of files.
 * 
 * @author Sherali Karimov
 * @author Dan T. Tran
 * @goal upload
 * @requiresProject true
 */
public class UploadMojo
    extends AbstractWagonMojo
{

    /**
     * A single FileSet to upload.
     *
     * @parameter
     * @since 1.0-alpha-1
     */
    private Fileset fileSet;

    /**
     * Multiple FileSets to upload
     * 
     * @parameter
     * @since 1.0-alpha-1
     */
    private List fileSets = new ArrayList( 0 );

    protected void execute( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        this.uploadFileSets( wagon );
    }

    private void uploadFileSets( Wagon wagon )
        throws MojoExecutionException, WagonException
    {
        if ( fileSet != null )
        {
            fileSets.add( fileSet );
        }
        
        if ( fileSets.isEmpty() )
        {
            this.getLog().info( "No file to upload." );
            return;
        }

        for ( int i = 0; i < fileSets.size(); ++i )
        {
            Fileset oneFileset = (Fileset) fileSets.get( i );

            if ( StringUtils.isBlank( oneFileset.getDirectory() ) )
            {
                oneFileset.setDirectory( this.project.getBasedir().getAbsolutePath() );
            }

            this.wagonHelpers.upload( wagon, oneFileset, this.getLog() );
        }
    }
}