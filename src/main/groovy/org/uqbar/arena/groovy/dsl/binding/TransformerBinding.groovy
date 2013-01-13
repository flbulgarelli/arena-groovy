package org.uqbar.arena.groovy.dsl.binding

@Category(Closure)
class TransformerBindingMixin {
  def configureBinding(bindingName, target) {
    target."bind${bindingName}ToTransformer"(this)
  }
}