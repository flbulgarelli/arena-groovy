package org.uqbar.arena.groovy

import groovy.lang.Closure

import org.uqbar.lacar.ui.model.Action

import com.uqbar.commons.collections.Transformer

class GroovyArenaExtensions {

  static action(Closure closure) {
    closure as Action
  }

  static transformer(Closure closure) {
    closure as Transformer
  }

  static filterStackTrace(Throwable e) {
    e.stackTrace = e.stackTrace.findAll { !isGroovyInternal(it) }
  }

  private static isGroovyInternal(stackTraceElement) {
    [
      "org.codehaus",
      "sun.reflect",
      "java.lang.reflect", //discutible
      "groovy.lang" //discutible
    ].any { packageName ->
      stackTraceElement.className.startsWith(packageName)
    }
  }
}
