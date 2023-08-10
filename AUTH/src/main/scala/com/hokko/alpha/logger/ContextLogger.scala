package com.hokko.alpha.logger

import java.util.logging.{Level, Logger}

trait ContextLogger{
    def log(string: String, level: Level = Level.INFO) : Unit
}
case class ContextLoggerActive[T](clazz: Class[T], setLevel: Level) extends ContextLogger {
    private val logger = Logger.getLogger(clazz.getSimpleName.replace("$",""))
    logger.setLevel(setLevel)
    override def log(string: String, level: Level): Unit = {
        logger.log(level, string);
    }
}

object ContextLogger{
    private val debugConfig = DebugConfiguration.getConfiguration

    def apply[T](clazz: Class[T]): ContextLogger = {
        debugConfig match {
            case Some(value) => {
                if(value.debugOn) ContextLoggerActive(clazz, Level.parse(value.level))
                else SilentLogger
            }
            case None => SilentLogger
        }
    }
}

object SilentLogger extends ContextLogger {
    override def log(string: String, level: Level): Unit = ()
}