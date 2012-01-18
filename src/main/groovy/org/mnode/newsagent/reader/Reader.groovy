package org.mnode.newsagent.reader

import org.mnode.ousia.OusiaBuilder

def ousia = new OusiaBuilder()

ousia.edt {
	frame(title: rs('Newsagent Reader'), show: true, locationRelativeTo: null, trackingEnabled: true, size: [600, 400])
}
