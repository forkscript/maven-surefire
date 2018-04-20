package org.apache.maven.surefire.junit4;

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

import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Customized {@link RunListener} providing command line interface.
 */
public class BasicJunitRunListener extends RunListener
{
    protected long runCount = 0;
    protected long ignoreCount = 0;
    protected long failureCount = 0;

    protected PrintStream out;

    public BasicJunitRunListener()
    {
        this( System.out );
    }

    public BasicJunitRunListener( PrintStream out )
    {
        this.out = out;
    }

    @Override
    public void testRunFinished( Result result )
    {
        out.printf( "[FORKSCRIPT] Run Count : %d\n", runCount );
        out.printf( "[FORKSCRIPT] Failure Count : %d\n", failureCount );
        out.printf( "[FORKSCRIPT] Ignore Count : %d\n", ignoreCount );
        out.printf( "[FORKSCRIPT] Run Time (ms) : %d\n", result.getRunTime() );
    }

    @Override
    public void testStarted( Description description )
    {
        if ( description.isTest() )
        {
            ++runCount;
        }
    }

    @Override
    public void testFailure( Failure failure )
    {
        if ( failure.getDescription().isTest() )
        {
            ++failureCount;
        }
    }


    public void testAssumptionFailure( Failure failure )
    {
        if ( failure.getDescription().isTest() )
        {
            ++ignoreCount;
        }
    }

    @Override
    public void testIgnored( Description description )
    {
        if ( description.isTest() )
        {
            ++ignoreCount;
        }
    }

    private static Result runMain( JUnitCore core, String... args )
    {
        List<Class<?>> classes = new ArrayList<Class<?>>();
        for ( String className : args )
        {
            try
            {
                System.out.println( String.format( "[FORKSCRIPT] Running %s", className ) );
                classes.add( Class.forName( className, true, Thread.currentThread().getContextClassLoader() ) );
            }
            catch ( ClassNotFoundException e )
            {
                throw new IllegalArgumentException( "Could not find class [" + className + "]", e );
            }
        }
        RunListener listener = new BasicJunitRunListener();
        core.addListener( listener );
        return core.run( ( Class<?>[] ) classes.toArray( new Class<?>[0] ) );
    }

    /**
     * Run single: {@code MyJunitRunnerListener Test1 Test2 Test3 ...}
     * Run fork: {@code for t in Tests; do MyJunitRunnerListener t; done}
     */
    public static void main( String... args )
    {
        Result result = runMain( new JUnitCore(), args );
        System.exit( result.wasSuccessful() ? 0 : 1 );
    }
}