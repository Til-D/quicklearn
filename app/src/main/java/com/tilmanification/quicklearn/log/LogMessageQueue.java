package com.tilmanification.quicklearn.log;

import java.util.LinkedList;

public class LogMessageQueue {

    private LinkedList<LogMessage>	messages	= new LinkedList<LogMessage>();

    public synchronized void queue(LogMessage message) {
        this.messages.addLast(message);
        notifyAll();
    }

    public synchronized LogMessage peek() {
        try {
            while (messages.size() == 0) {
                wait();
            }
            return messages.getFirst();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized LogMessage dequeue() {
        try {
            while (messages.size() == 0) {
                wait();
            }
            return messages.removeFirst();
        } catch (Exception e) {
            return null;
        }
    }

    public synchronized int size() {
        return messages.size();
    }
}

class LogMessage {

    public String	fileName;
    public String	message;

    public LogMessage(String fileName, String message) {
        this.fileName = fileName;
        this.message = message;

    }
}
