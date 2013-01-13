package org.uqbar.arena.groovy.dsl.binding


//may hook from PropertyObservables instead of property bindings, but property biding is apparently more
//supported than plain observers in arena - there are more bind.*ToProperty than bind.* methods
class TransformedPropertyBinding {
  def property
  def transformer

  def configureBinding(bindingName, target) {
    property.configureBinding(bindingName, target).setTransformer(transformer)
  }
}