package org.uqbar.arena.groovy

import org.uqbar.arena.groovy.dsl.ArenaDSL
import org.uqbar.arena.windows.MainWindow

@SuppressWarnings("unchecked")
abstract class GroovyMainWindow extends MainWindow {

  static {
    ArenaDSL
  }

  @Override
  public void startApplication() {
    try{
      super.startApplication()
    } catch (Throwable e) {
      GroovyArenaExtensions.filterStackTrace(e)
      e.printStackTrace()
    }
  }
}
