package eu.siacs.conversations.xml;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.LinkedBlockingQueue;

import eu.siacs.conversations.xmpp.stanzas.AbstractStanza;

public class TagWriter {
	
	private OutputStreamWriter outputStream;
	private boolean finshed = false;
	private LinkedBlockingQueue<AbstractStanza> writeQueue = new LinkedBlockingQueue<AbstractStanza>();
	private Thread asyncStanzaWriter = new Thread() {
		private boolean shouldStop = false;
		@Override
		public void run() {
			while(!shouldStop) {
				if ((finshed)&&(writeQueue.size() == 0)) {
					return;
				}
				try {
					AbstractStanza output = writeQueue.take();
					outputStream.write(output.toString());
					outputStream.flush();
				} catch (IOException e) {
					shouldStop = true;
				} catch (InterruptedException e) {
					shouldStop = true;
				}
			}
		}
	};
	
	public TagWriter() {
	}
	
	public void setOutputStream(OutputStream out) {
		this.outputStream = new OutputStreamWriter(out);
	}
	
	public TagWriter beginDocument() throws IOException {
		outputStream.write("<?xml version='1.0'?>");
		outputStream.flush();
		return this;
	}
	
	public TagWriter writeTag(Tag tag) throws IOException {
		outputStream.write(tag.toString());
		outputStream.flush();
		return this;
	}

	public TagWriter writeElement(Element element) throws IOException {
		outputStream.write(element.toString());
		outputStream.flush();
		return this;
	}
	
	public TagWriter writeStanzaAsync(AbstractStanza stanza) {
		if (finshed) {
			return this;
		} else {
			if (!asyncStanzaWriter.isAlive()) asyncStanzaWriter.start();
			writeQueue.add(stanza);
			return this;
		}
	}
	
	public void finish() {
		this.finshed = true;
	}
	
	public boolean finished() {
		return (this.writeQueue.size() == 0);
	}
}
