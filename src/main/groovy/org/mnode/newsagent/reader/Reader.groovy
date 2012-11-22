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
package org.mnode.newsagent.reader

import java.lang.Thread.UncaughtExceptionHandler;

import javax.jcr.NamespaceException
import javax.jcr.Session
import javax.jcr.SimpleCredentials
import javax.naming.InitialContext
import javax.swing.JFrame

import org.apache.jackrabbit.core.jndi.RegistryHelper
import org.mnode.newsagent.FeedReader
import org.mnode.newsagent.FeedReaderImpl
import org.mnode.newsagent.FeedResolverImpl
import org.mnode.newsagent.OpmlImporterImpl
import org.mnode.newsagent.jcr.JcrFeedCallback
import org.mnode.newsagent.jcr.JcrOpmlCallback
import org.mnode.newsagent.util.FeedFetcherCacheImpl
import org.mnode.ousia.DialogExceptionHandler
import org.mnode.ousia.OusiaBuilder
import org.mnode.ousia.flamingo.icons.LogoSvgIcon

Thread.defaultUncaughtExceptionHandler = { thread, throwable ->
    throwable.printStackTrace()
} as UncaughtExceptionHandler

try {
	new Socket('localhost', 1337)
	println 'Already running'
	System.exit(0)
}
catch (Exception e) {
}

new File(System.getProperty("user.home"), ".newsagent").mkdir()
def configFile = new File(System.getProperty("user.home"), ".newsagent/config.xml")
configFile.text = Reader.getResourceAsStream("/config.xml").text

def ousia = new OusiaBuilder()
//def sg = new SceneGraphBuilder()

Thread.start {
	ServerSocket server = [1337]
	while(true) {
		try {
			server.accept {}
		}
		finally {
			ousia.doLater {
				newsagentFrame.visible = true
			}
		}
	}
}

File repositoryLocation = [System.getProperty("user.home"), ".newsagent/data"]
//File repositoryLocation = ['target/repository']

def context = new InitialContext()
RegistryHelper.registerRepository(context, 'newsagent', configFile.absolutePath, repositoryLocation.absolutePath, false)
def repository = context.lookup('newsagent')

Session session = repository.login(new SimpleCredentials('readonly', ''.toCharArray()))
Runtime.getRuntime().addShutdownHook({
	RegistryHelper.unregisterRepository(context, 'newsagent')
})

try {
	session.workspace.namespaceRegistry.registerNamespace('mn', 'http://mnode.org/namespace')
}
catch (NamespaceException e) {
	println e.message
}
JcrFeedCallback callback = [node:session.save {rootNode << 'mn:subscriptions'}, downloadEnclosures:false]
//
session.save {rootNode << 'mn:tags'}

FeedReader reader = new FeedReaderImpl(new FeedFetcherCacheImpl('org.mnode.newsagent.reader.feedCache'))
//reader.read(new FeedResolverImpl().resolve("slashdot.org")[0], callback)
OpmlImporterImpl importer = []
//importer.importOpml(new FileInputStream('src/test/resources/google-reader-subscriptions.xml'), new JcrOpmlCallback(node: session.rootNode))

def updateFeed
updateFeed = { feedNode ->
  if (feedNode['mn:source']) {
    reader.read new URL(feedNode['mn:source'].string), callback
  }
  else {
    feedNode.nodes.each {
	  updateFeed it
	}
  }
}

FeedResolverImpl feedResolver = []

ousia.edt {
//	lookAndFeel('substance-mariner').fontPolicy = SubstanceFontUtilities.getScaledFontPolicy(1.2)
	lookAndFeel('substance-mariner')
	
    def frameIconImages = [
        imageIcon('/logo64.png').image,
        imageIcon('/logo48.png').image,
        imageIcon('/logo32.png').image,
        imageIcon('/logo16.png').image
    ]
    /*
	imageIcon(id: 'logo64', '/logo64.png')
	imageIcon(id: 'logo48', '/logo48.png')
	imageIcon(id: 'logo32', '/logo32.png')
	imageIcon(id: 'logo16', '/logo16.png')
	*/
    def applicationIcon = new LogoSvgIcon()
	
	actions {
		action id: 'addSubscriptionAction', name: rs('Add Subscription..'), closure: {
			def subscriptionText = addSubscriptionField.text
			addSubscriptionField.text = null
			//subscriptionTable.model = new DefaultTableModel()
			doOutside {
				try {
				//Desktop.desktop.browse(URI.create('http://basetools.org/coucou'))
					def feedUrls = feedResolver.resolve(subscriptionText)
					reader.read feedUrls[0], callback
				} catch (def e) {
					reader.read subscriptionText, callback
				} finally {
					subscriptionNodes = subscriptionQuery.execute().nodes.toList()
					doLater {
						subscriptionTable.model = new SubscriptionTableModel(subscriptionNodes)
					}
				}
			}
		}
		
		action id: 'toggleTableHeaders', name: rs('Show Table Headers'), closure: { e ->
			 //if (e.source.actionModel.selected) {
			 if (e.source.selected) {
				 //subscriptionTable.tableHeader.visible = true
				 //subscriptionTable.tableHeader.preferredSize = null
				 entryTable.tableHeader.visible = true
				 entryTable.tableHeader.preferredSize = null
			 }
			 else {
				 //subscriptionTable.tableHeader.visible = false
				 //subscriptionTable.tableHeader.preferredSize = [-1, 0]
				 entryTable.tableHeader.visible = false
				 entryTable.tableHeader.preferredSize = [-1, 0]
			 }
		}
	}
/*	
	ribbonFrame(id: 'newsagentFrame', title: rs('Newsagent Reader'), show: true, defaultCloseOperation: JFrame.EXIT_ON_CLOSE, locationRelativeTo: null, trackingEnabled: true, size: [600, 400],
		iconImages: [logo64.image, logo48.image, logo32.image, logo16.image]) {
        
		borderLayout()
		
		panel(constraints: BorderLayout.NORTH) {
			borderLayout()
			breadcrumbBar(id: 'breadcrumb', new BreadcrumbContextCallback(rootContext: new RootContext(session.rootNode)), throwsExceptions: false, constraints: BorderLayout.WEST)
			toggleButton(action: toggleTableHeaders, selected: true)
			textField(id: 'addSubscriptionField', columns: 14, prompt: addSubscriptionAction.getValue('Name'), promptFontStyle: Font.ITALIC, promptForeground: Color.LIGHT_GRAY,
						keyPressed: {e-> if (e.keyCode == KeyEvent.VK_ESCAPE) e.source.text = null}, constraints: BorderLayout.EAST) {
						
				addSubscriptionField.addActionListener addSubscriptionAction
			}
		}
        panel(new ViewPane(session))
	}
*/
    frame(new RibbonWindow(session, ousia), id: 'newsagentFrame', title: 'Newsagent Reader', size: [640, 400], locationRelativeTo: null,
        visible: true, defaultCloseOperation: JFrame.EXIT_ON_CLOSE, iconImages: frameIconImages,
        applicationIcon: applicationIcon, trackingEnabled: true)
          
    Thread.defaultUncaughtExceptionHandler = new DialogExceptionHandler(dialogOwner: newsagentFrame)

	doOutside {
		updateFeed session.rootNode['mn:subscriptions']
	}
}
