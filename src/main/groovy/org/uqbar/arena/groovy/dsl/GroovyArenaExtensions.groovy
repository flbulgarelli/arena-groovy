package org.uqbar.arena.groovy.dsl

import java.lang.reflect.Modifier

import groovy.lang.Closure

import org.apache.commons.lang.WordUtils
import org.reflections.Reflections
import org.uqbar.arena.widgets.Button
import org.uqbar.arena.widgets.Container
import org.uqbar.arena.widgets.Selector
import org.uqbar.arena.widgets.Widget
import org.uqbar.arena.widgets.tables.Column;
import org.uqbar.arena.widgets.tables.Table;
import org.uqbar.arena.widgets.tree.Tree
import org.uqbar.arena.windows.Dialog
import org.uqbar.arena.windows.Window;
import org.uqbar.commons.model.IModel
import org.uqbar.lacar.ui.model.Action

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
      bindings.each { binding, property ->
        thisWidget."bind${binding.capitalize()}ToProperty"(property)
      }
    }
  }

  private static supportClosuresAsActions() {
    [
      [Selector, 'onSelection'],
      [Tree, 'onClickItem'],
      [Tree, 'onExpand'],
      [Dialog, 'onAccept'],
      [Dialog, 'onCancel'],
      [Button, 'onClick']
    ].each { ConcreteWidget, selector ->
      ConcreteWidget.metaClass."$selector" = { Closure actionClosure ->
        delegate."$selector"(action(actionClosure))
      }
    }
  }

  static {
    makeDescriptive(Widget)
    makeDescriptive(Window)
    makeDescriptive(Column)
    makeDescriptive(Table)
    makeBindable(Widget)
    supportClosuresAsActions()
  }
}

