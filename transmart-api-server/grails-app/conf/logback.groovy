/**
 * Default logging configuration for the tranSMART API server.
 * This configuration can be overridden by adding
 * <code>-Dlogging.config=/path/to/logback.groovy</code> to
 * the start script.
 *
 * See https://docs.grails.org/latest/guide/conf.html#externalLoggingConfiguration.
 */

import ch.qos.logback.contrib.jackson.JacksonJsonFormatter
import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter
import org.transmartproject.rest.logging.ApiAuditLogJsonLayout
// import org.transmartproject.rest.logging.ChildProcessAppender

import java.nio.charset.Charset

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// See http://logback.qos.ch/manual/groovy.html for details on configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')
        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

root(WARN, ['STDOUT'])

boolean productionMode = Environment.current == Environment.PRODUCTION
def logDirectory = BuildSettings.TARGET_DIR
if (productionMode) {
    def catalinaBase = System.getProperty('catalina.base') ?: '.'
    logDirectory = "${catalinaBase}/logs".toString()
}

if ((productionMode || Environment.isDevelopmentMode()) && logDirectory != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${logDirectory}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            charset = Charset.forName('UTF-8')
            pattern = '[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [%thread] ' +
                    '%logger{10} [%file:%line] %msg%n'
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}

if (productionMode && logDirectory) {
    appender('transmart', RollingFileAppender) {
        file = "${logDirectory}/transmart.log"
        encoder(PatternLayoutEncoder) {
            charset = Charset.forName('UTF-8')
            pattern = '[%d{yyyy-MM-dd HH:mm:ss.SSS}] [%level] [%thread] ' +
                    '%logger{10} [%file:%line] %msg%n'
        }
        rollingPolicy(SizeAndTimeBasedRollingPolicy) {
            // daily rollover
            fileNamePattern = "${logDirectory}/transmart.%d{yyyy-MM-dd}.%i.log"
            // size limit on the log files
            maxFileSize = '100MB'
            // optional parameters
            // maxHistory controls the maximum number of archive files to keep, asynchronously deleting older files.
            maxHistory = 9
            // totalSizeCap controls the total size of all archive files.
            // Oldest archives are deleted asynchronously when the total size cap is exceeded
            // totalSizeCap = '3GB'
        }
    }
    root(INFO, ['transmart'])
}


/**
 * Configuration for writing audit metrics.
 * See https://logback.qos.ch/manual/appenders.html for details on configuration.
 */
appender('fileAuditLogger', RollingFileAppender) {
    file = "${logDirectory}/audit.log"
    rollingPolicy(SizeAndTimeBasedRollingPolicy) {
        // daily rollover
        fileNamePattern = "${logDirectory}/audit.%d{yyyy-MM-dd}.%i.log"
        // size limit on the log files
        maxFileSize = '100MB'
        /* // optional parameters
        // maxHistory controls the maximum number of archive files to keep, asynchronously deleting older files.
        maxHistory = 30
        // totalSizeCap controls the total size of all archive files.
        // Oldest archives are deleted asynchronously when the total size cap is exceeded
        totalSizeCap = '3GB'
         */
    }
    encoder(LayoutWrappingEncoder) {
        layout(ApiAuditLogJsonLayout) {
            jsonFormatter(JacksonJsonFormatter) {
                prettyPrint = true
            }
            appendLineSeparator = true
        }
    }
}

// appender('processAuditLogger', ChildProcessAppender) {
    // specify the command as in the example below
    //    command = ['/usr/bin/your/command/here', 'arg1', 'arg2']
// }

logger('org.transmartproject.db.log', TRACE, ['fileAuditLogger'], true)
// logger('org.transmartproject.db.log', TRACE, ['processAuditLogger'], true)
