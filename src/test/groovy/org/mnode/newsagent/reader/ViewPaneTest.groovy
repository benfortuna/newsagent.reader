/**
 * Copyright (c) 2012, Ben Fortuna
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  o Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *
 *  o Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 *  o Neither the name of Ben Fortuna nor the names of any other contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.mnode.newsagent.reader;

import static org.junit.Assert.*
import groovy.swing.SwingBuilder

import java.util.concurrent.TimeUnit

import javax.jcr.SimpleCredentials
import javax.naming.InitialContext

import org.apache.jackrabbit.core.jndi.RegistryHelper
import org.junit.Before;
import org.junit.BeforeClass
import org.junit.Test

class ViewPaneTest {

    static def swing
    
    def session
    
    @BeforeClass
    static void setupClass() {
        swing = new SwingBuilder()
    }
    
    @Before
    void setup() {
        new File(System.getProperty("user.home"), ".newsagent").mkdir()
        def configFile = new File(System.getProperty("user.home"), ".newsagent/config.xml")
        configFile.text = Reader.getResourceAsStream("/config.xml").text
        File repositoryLocation = ['target/repository']
        
        def context = new InitialContext()
        RegistryHelper.registerRepository(context, 'newsagent', configFile.absolutePath, repositoryLocation.absolutePath, false)
        def repository = context.lookup('newsagent')
        
        session = repository.login(new SimpleCredentials('readonly', ''.toCharArray()))
        Runtime.getRuntime().addShutdownHook({
            RegistryHelper.unregisterRepository(context, 'newsagent')
        })
    }
    
    @Test
    void testCreate() {
        swing.edt {
            frame(id: 'newsagentFrame', visible: true) {
                panel(new  ViewPane(session, swing))
            }
        }
        TimeUnit.SECONDS.sleep(10)
    }
}
