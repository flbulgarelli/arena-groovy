package org.uqbar.arena.groovy.dsl

import org.uqbar.arena.widgets.tables.Column
import org.uqbar.arena.widgets.tables.Table

class RichTable extends Proxy {

  def column(description) {
    new Column(target).describe(description)
  }
}
