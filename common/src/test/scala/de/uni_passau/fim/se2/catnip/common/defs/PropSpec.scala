package de.uni_passau.fim.se2.catnip.common.defs

import org.scalatest.matchers.should
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatest.propspec.AnyPropSpec
import org.slf4j.impl.SimpleLogger

abstract class PropSpec
    extends AnyPropSpec
    with TableDrivenPropertyChecks
    with should.Matchers {

  System.setProperty(SimpleLogger.DEFAULT_LOG_LEVEL_KEY, "Info")
}
