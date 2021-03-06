package org.apache.maven.plugin.surefire;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.lang.reflect.Constructor;

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.plugin.surefire.report.DefaultReporterFactory;
import org.apache.maven.surefire.booter.SurefireReflector;
import org.apache.maven.surefire.util.SurefireReflectionException;

import javax.annotation.Nonnull;

import static org.apache.maven.surefire.util.ReflectionUtils.getConstructor;
import static org.apache.maven.surefire.util.ReflectionUtils.instantiateObject;
import static org.apache.maven.surefire.util.ReflectionUtils.newInstance;

/**
 * @author Kristian Rosenvold
 */
public class CommonReflector
{
    private final Class<?> startupReportConfiguration;
    private final Class<?> consoleLogger;
    private final ClassLoader surefireClassLoader;

    public CommonReflector( @Nonnull ClassLoader surefireClassLoader )
    {
        this.surefireClassLoader = surefireClassLoader;

        try
        {
            startupReportConfiguration = surefireClassLoader.loadClass( StartupReportConfiguration.class.getName() );
            consoleLogger = surefireClassLoader.loadClass( ConsoleLogger.class.getName() );
        }
        catch ( ClassNotFoundException e )
        {
            throw new SurefireReflectionException( e );
        }
    }

    public Object createReportingReporterFactory( @Nonnull StartupReportConfiguration startupReportConfiguration,
                                                  @Nonnull ConsoleLogger consoleLogger )
    {
        Class<?>[] args = { this.startupReportConfiguration, this.consoleLogger };
        Object src = createStartupReportConfiguration( startupReportConfiguration );
        Object logger = SurefireReflector.createConsoleLogger( consoleLogger, surefireClassLoader );
        Object[] params = { src, logger };
        return instantiateObject( DefaultReporterFactory.class.getName(), args, params, surefireClassLoader );
    }

    private Object createStartupReportConfiguration( @Nonnull StartupReportConfiguration reporterConfiguration )
    {
        Constructor<?> constructor = getConstructor( startupReportConfiguration, boolean.class, boolean.class,
                                                           String.class, boolean.class, boolean.class, File.class,
                                                           boolean.class, String.class, File.class, boolean.class,
                                                           int.class, String.class, String.class );
        //noinspection BooleanConstructorCall
        Object[] params = { reporterConfiguration.isUseFile(), reporterConfiguration.isPrintSummary(),
            reporterConfiguration.getReportFormat(), reporterConfiguration.isRedirectTestOutputToFile(),
            reporterConfiguration.isDisableXmlReport(), reporterConfiguration.getReportsDirectory(),
            reporterConfiguration.isTrimStackTrace(), reporterConfiguration.getReportNameSuffix(),
            reporterConfiguration.getStatisticsFile(), reporterConfiguration.isRequiresRunHistory(),
            reporterConfiguration.getRerunFailingTestsCount(), reporterConfiguration.getXsdSchemaLocation(),
            reporterConfiguration.getEncoding().name() };
        return newInstance( constructor, params );
    }

}
