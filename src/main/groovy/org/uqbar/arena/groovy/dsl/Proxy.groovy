package org.uqbar.arena.groovy.dsl

class Proxy {
  def target
  
  def methodMissing(String name, args) {
    target.invokeMethod(name, args)
  }

  def propertyMissing(String name) {
    target."$name"
  }

  def propertyMissing(String name, value) {
    target."$name" = value
  }
}
