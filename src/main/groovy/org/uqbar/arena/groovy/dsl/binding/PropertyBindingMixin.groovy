package org.uqbar.arena.groovy.dsl.binding

import org.uqbar.arena.bindings.DateAdapter
import org.uqbar.lacar.ui.model.adapter.NotEmptyTransformer
import org.uqbar.lacar.ui.model.adapter.NotNullTransformer

@Category(String)
class PropertyBindingMixin {
  def configureBinding(bindingName, target) {
    target."bind${bindingName}ToProperty"(this)
  }

  def notNull() {
    adapt(this, new NotNullTransformer())
  }

  def notEmpty() {
    adapt(this, new NotEmptyTransformer())
  }

  def adaptDate() {
    adapt(this, new DateAdapter())
  }

  def adapt(adapter) {
    new TransformedPropertyBinding(property: this, transformer: adapter)
  }
}

