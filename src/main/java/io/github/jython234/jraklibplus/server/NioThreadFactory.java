package io.github.jython234.jraklibplus.server;

import java.util.concurrent.ThreadFactory;

/**
 * Created by jython234 on 9/12/2015.
 *
 * @author RedstoneLamp Team
 */
public class NioThreadFactory implements ThreadFactory {
    private int i = 1;

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r);
        t.setName("NioWorkerThread-"+i);
        t.setPriority(Thread.MAX_PRIORITY);
        i++;
        return t;
    }
}
