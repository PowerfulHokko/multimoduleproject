package com.hokko.alpha.logger

import pureconfig.error.ConfigReaderFailures

private object DebugConfiguration {

    import pureconfig._
    import pureconfig.generic.auto._

    lazy val getConfiguration: Option[DebugConfiguration] = {
        ConfigSource.resources("debug.conf").at("debug-config").load[DebugConfiguration] match {
            case Left(value: ConfigReaderFailures) => {
                println(value.prettyPrint())
                Option(DefaultDebug)
            }
            case Right(value: DebugConfiguration) => {
                Option(value)
            }
        }
    }
}

private case class DebugConfiguration(debugOn: Boolean, level: String)
private object DefaultDebug extends DebugConfiguration(false, "OFF")
