/*
 * Copyright © 2013-2016 The Hyve B.V.
 *
 * This file is part of Transmart.
 *
 * Transmart is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Transmart.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.transmartproject.rest.logging

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.contrib.json.classic.JsonLayout
import ch.qos.logback.core.AppenderBase
import ch.qos.logback.core.Layout
import com.google.common.base.Charsets
import groovy.transform.CompileStatic
import groovy.transform.InheritConstructors
import groovy.util.logging.Slf4j

import static java.lang.ProcessBuilder.Redirect.INHERIT


/**
 * This appender spawns a process, and sends log messages encoded as json to the process which can read them on its
 * standard input. If the process dies or writing to its input pipe fails for some other reason the process is
 * restarted. Note that ChildProcessAppender does not forcibly kill its child process in such cases, only the
 * stdin pipe is closed. It is expected that the process will exit if its stdin is closed, but a misbehaving process
 * may live on.
 *
 * Process management is done using a restart counter and a time window. If the child process needs to be restarted
 * more than a set number of times within the time window, the process is considered to be broken and this appender
 * will stop trying to restart it and go into a 'broken' state. If more elaborate process management is needed you
 * should configure this appender to start the child under a process manager program.
 *
 * This appender has the standard properties inherited from AppenderSkeleton, i.e. name, filter and threshold.
 * Properties layout and errorHandler are not used. Furthermore there are the following properties:
 *
 * command: List<String>  The command to run to start the external process
 *
 * restartLimit: int (default 15)  The number of times the child process will be restarted within the restartWindow
 * before this appender decides that the child process configuration is broken. Set to 0 to disable the restart
 * limiting feature. Doing so can cause running in an infinite restart loop if the child process exits immediately,
 * so doing so is not recommended for production deployments.
 *
 * restartWindow: int (default 1800)  The number of seconds of the restartWindow. If the child process fails more
 * than restartLimit times within this window, this is interpreted as a configuration error for the child. The child
 * is not restarted and this appender goes into a 'broken' state.
 *
 * throwOnFailure: boolean (default false)  Throw an exception if this appender goes into the 'broken' state or if it
 * is broken and it is asked to handle new log messages. Enable this if you want to be sure Transmart fails fast if
 * the child process cannot be restarted.
 */
@CompileStatic
@Slf4j
class ChildProcessAppender extends AppenderBase<ILoggingEvent> {

    List<String> command
    boolean throwOnFailure = false
    int restartLimit = 15
    int restartWindow = 1800
    Layout<ILoggingEvent> layout

    protected long starttime
    protected int failcount = 0
    protected Process process
    protected Writer input

    // The appender being broken should be unlikely. It indicates a misconfiguration or an error in the child program.
    protected boolean broken = false

    private ThreadLocal<int[]> recursionCount = [
            initialValue: { [0] as int[] }
    ] as ThreadLocal<int[]>

    void setRestartLimit(int l) {
        if (l < 0) {
            throw new IllegalArgumentException("restartLimit cannot be negative (use 0 to disable the limit)")
        }
        this.restartLimit = l
    }

    void setRestartWindow(int w) {
        if (w <= 0) {
            throw new IllegalArgumentException("restartWindow must be larger than 0")
        }
        this.restartWindow = w
    }

    boolean isBroken() { return broken }

    ChildProcessAppender() {
        this.layout = new JsonLayout()
    }

    /* We cannot call into the normal logging system while we have this appender locked (that risks deadlock), so use
     * the backup logging system.*/

    private void debug(String msg) {
        log.debug("${this.class.name}(name: $name): $msg")
    }

    private synchronized startProcess() {
        if (process == null) {
            starttime = System.currentTimeMillis()
        }
        process = new ProcessBuilder(command).redirectOutput(INHERIT).redirectError(INHERIT).start()
        input = new OutputStreamWriter(process.getOutputStream(), Charsets.UTF_8)
        // Give child some time to fail if it fails quickly
        sleep(1)
    }

    synchronized boolean getChildAlive() {
        if (process == null) {
            return false
        }
        try {
            process.exitValue()
            return false
        } catch (IllegalThreadStateException _) {
            return true
        }
    }

    private synchronized String restartChild() {
        failcount++
        long restartWindowEnd = starttime + restartWindow * 1000L
        long now = System.currentTimeMillis()
        boolean inWindow = restartLimit == 0 ? false : now <= restartWindowEnd
        if (restartLimit != 0 && inWindow && failcount > restartLimit) {
            broken = true
            // Don't log from here while we are synchronized, that would cause a deadlock condition
            return "Failed to restart external log handling process \"${command.join(' ')}\", failed $failcount times" +
                    " in less than $restartWindow seconds"
        }

        try {
            input.close()
        } catch (IOException e) {
            /* ignore. Can happen if the implementation already closed its underlying stream
                   when the child closed its inputstream */
        }

        if (!inWindow) {
            starttime = now
            failcount = 0
        }
        debug("Restarting external logging process ${command.join(' ')}")
        startProcess()
        return null
    }

    void write(String str) {
        int[] rc = recursionCount.get()
        int oldrc = rc[0]
        try {
            rc[0]++
            // Recursive call, ignore
            if (rc[0] > 1) {
                return
            }

            if (broken) {
                String msg = "Attempting to write to broken external log handling process"
                if (throwOnFailure) {
                    throw new ChildFailedException(msg)
                } else {
                    log.warn(msg)
                    return
                }
            }

            String errmsg = null
            // input.write and input.flush synchronize, so there is no use in optimizing away this synchronized block
            // in the normal flow.
            synchronized (this) {
                if (!input) {
                    startProcess()
                }
                while (errmsg == null) {
                    try {
                        input.write(str)
                        input.flush()
                        return
                    } catch (IOException e) {
                        debug("Caught IOException while writing to child process: $e")
                        errmsg = restartChild()
                    }
                }
            }

            assert errmsg != null
            // Log outside of the synchronized block
            log.error(errmsg)
            if (throwOnFailure) {
                throw new ChildFailedException(errmsg)
            }
        } finally {
            rc[0] = oldrc
        }
    }

    @Override
    void start() {
        if (this.layout == null) {
            addError("No layout set for the appender named [" + name + "].")
            return
        }
        super.start()
    }

    @Override
    void append(ILoggingEvent event) {
        // Check for recursive invocation
        if (recursionCount.get()[0] <= 0) {
            write(layout.doLayout(event))
        }
    }

    @Override
    synchronized void stop() {
        input?.close()
        super.stop()
    }

    @InheritConstructors
    static class ChildFailedException extends IOException {}
}
