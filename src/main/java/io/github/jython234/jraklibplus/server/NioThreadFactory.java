/**
 * JRakLibPlus is not affiliated with Jenkins Software LLC or RakNet.
 * This software is an enhanced port of RakLib https://github.com/PocketMine/RakLib.

 * This file is part of JRakLibPlus.
 *
 * JRakLibPlus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JRakLibPlus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with JRakLibPlus.  If not, see <http://www.gnu.org/licenses/>.
 */
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
