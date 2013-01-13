package org.uqbar.arena.groovy.dsl

import groovy.lang.Closure
import groovy.transform.PackageScope

import org.uqbar.arena.bindings.DateAdapter
import org.uqbar.arena.groovy.dsl.binding.PropertyBindingMixin;
import org.uqbar.arena.groovy.dsl.binding.TransformedPropertyBinding;
import org.uqbar.arena.groovy.dsl.binding.TransformerBindingMixin;
import org.uqbar.arena.widgets.Button
import org.uqbar.arena.widgets.Container
import org.uqbar.arena.widgets.Selector
import org.uqbar.arena.widgets.Widget
import org.uqbar.arena.widgets.tables.Column
import org.uqbar.arena.widgets.tables.Table
import org.uqbar.arena.widgets.tree.Tree
import org.uqbar.arena.widgets.tree.TreeNode
import org.uqbar.arena.windows.Dialog
import org.uqbar.arena.windows.Window
import org.uqbar.lacar.ui.model.Action
import org.uqbar.lacar.ui.model.adapter.NotEmptyTransformer
import org.uqbar.lacar.ui.model.adapter.NotNullTransformer

import com.uqbar.commons.collections.Transformer

/**
 * Agrega soporte para:
 * <ul>
 * <li>Pasar bloques ({@link Closure}s) en donde se espera una {@link Action}</li>
 * <li>"Describir" declarativamente y jerarquicamente los contenidos de los widgets, empleando él método </li>
 * 
 * </ul>
 * @author flbulgarelli
 *
 */
class GroovyArenaExtensions {

  static def action(Closure closure) {
    closure as Action
  }
  
  static def transformer(Closure closure) {
    closure as Transformer
  }
  
  private static makeDescriptive(Target) {
    def wrap = {
      if(it instanceof Container)
        new RichContainer(target: it)
      else if(it instanceof Table)
        new RichTable(target: it)
      else
        new Proxy(target: it)
    }
    Target.metaClass.describe = { description ->
        def thisWidget = delegate
        def descriptiveWrapper = wrap(thisWidget)
        description.clone().with {
          it.resolveStrategy = Closure.DELEGATE_ONLY 
          it.delegate = descriptiveWrapper
          it(thisWidget)
        }
        thisWidget

    }
  }
   
  private static makeBindable(Target) {
    Target.metaClass.bind = { Map bindings ->
      def thisWidget = delegate
      bindings.each { bindingName, bindingValue ->
        bindingValue.configureBinding(bindingName.capitalize(), thisWidget)
      }
    }
  }

  private static supportClosuresAsActions() {
    supportClosures([
      [Selector, 'onSelection'],
      [Tree, 'onClickItem'],
      [Tree, 'onExpand'],
      [Dialog, 'onAccept'],
      [Dialog, 'onCancel'],
      [Button, 'onClick']
    ], GroovyArenaExtensions.&action)
  }
  
  private static supportClosuresAsTransformers() {
    supportClosures([
      [Column, 'bindContentsToTransformer'],
      [TreeNode, 'bindContentsToTransformer'],
    ], GroovyArenaExtensions.&transformer)
  }
  
  private static supportClosures(classesAndSelectors, transformation) {
    classesAndSelectors.each { ConcreteWidget, selector ->
      ConcreteWidget.metaClass."$selector" = { Closure closure ->
        delegate."$selector"(transformation(closure))
      }
    }
  }
  
  @PackageScope static bindAndDescribe(Container, container, Widget, selector, args) {
    def configurations = Arrays.asList(args)
    
    if (configurations.size() > 2)
      throw new MissingMethodException(selector, Container, args)
      
    def widget = Widget.newInstance([container]as Object[])
    def (bindings, description) = bindingsAndDescription(widget, configurations)
    widget.bind(bindings)
    widget.describe(description)
  }
  
  private static bindingsAndDescription(widget, configurations) {
    def bindings = configurations.find { it instanceof Map } ?: [:]
    def description = configurations.find { it instanceof Closure } ?: {}
    [bindings, description]
  }

  static {
    String.mixin(PropertyBindingMixin)
    Closure.mixin(TransformerBindingMixin)
    
    makeDescriptive(Window)
    
    [Widget, Column].each {
      makeDescriptive(it)
      makeBindable(it)
    }
    
    makeDescriptive(Table)
    
    supportClosuresAsActions()
    supportClosuresAsTransformers()
  }
}

