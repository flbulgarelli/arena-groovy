package org.uqbar.arena.groovy

import org.uqbar.arena.Application
import org.uqbar.arena.groovy.dsl.ArenaDSL

abstract class GroovyApplication extends Application {

  static {
    ArenaDSL
  }

  @Override
  public void start() {
    try{
      super.start()
    } catch (Throwable e) {
      GroovyArenaExtensions.filterStackTrace(e)
      e.printStackTrace()
    }
  }
}
