package com.tertiumtechnology.testapp.util;

import android.os.Handler;

import java.util.List;

public class Chain {
    static class ChainElement {

        private final Runnable operation;
        private ChainElement next;

        ChainElement(Runnable operation) {
            this.operation = operation;
        }

        ChainElement getNext() {
            return next;
        }

        void setNext(ChainElement next) {
            this.next = next;
        }

        Runnable getOperation() {
            return operation;
        }
    }

    private ChainElement currentElement;
    private ChainElement firstElement;
    private Handler handler;
    private boolean isCircular;

    public Chain(Handler handler, boolean isCircular) {
        this.handler = handler;
        this.isCircular = isCircular;
    }

    public void executeNext() {
        if (currentElement != null) {
            // chain command
            ChainElement next = currentElement.getNext();
            if (next != null) {
                // go to next command
                handler.post(
                        next.getOperation()
                );
                currentElement = next;
            }
            else if (isCircular) {
                // last command done, waiting for first operation
                handler.postDelayed(
                        firstElement.getOperation(), 10000
                );
                currentElement = firstElement;
            }
        }
    }

    public boolean hasEnded() {
        return currentElement.getNext() == null;
    }

    public void init(List<Runnable> runnables) {

        ChainElement last = null;

        for (Runnable runnable : runnables) {
            ChainElement chainElement = new ChainElement(runnable);
            if (last == null) {
                firstElement = currentElement = chainElement;
            }
            else {
                last.setNext(chainElement);
            }
            last = chainElement;
        }
    }

    public void reset() {
        handler.removeCallbacksAndMessages(null);
        currentElement = firstElement;
    }

    public void startExecution() {
        handler.post(
                firstElement.getOperation()
        );
    }
}