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
