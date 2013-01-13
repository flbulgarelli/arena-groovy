package org.uqbar.arena.groovy.dsl

import org.uqbar.arena.widgets.tables.Column
import org.uqbar.arena.widgets.tables.Table

import static org.uqbar.arena.groovy.dsl.ArenaDSL.*
class RichTable extends Proxy {

  def column(...args) {
    bindAndDescribe(RichTable, target, Column, "column", args)
  }
}
