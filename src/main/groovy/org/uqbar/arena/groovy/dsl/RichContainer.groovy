package org.uqbar.arena.groovy.dsl

import java.lang.reflect.Modifier

import org.apache.commons.lang.WordUtils
import org.reflections.Reflections
import org.uqbar.arena.widgets.Container
import org.uqbar.arena.widgets.Widget
import org.uqbar.commons.model.IModel

/**
 * Wrapper de {@link Container}, que entiende mensajes para 
 * crear widgets estandar hijos.
 * 
 * Estos métodos se agregan a RichContainer y no a Container directamente por cuestione tecnológicas: 
 * para permitir el uso del contenedor como receptor implícito en el contexto de un bloque, sin perder 
 * el soporte para la recepción de mensajes definidos dinácamente, el objeto delegate
 * debe ser de una clase Groovy y no de una clase Java (en general, en cualquier otro contexto 
 * agregar el método directametne a {@link Container} funcionaría)
 * 
 * @author flbulgarelli
 */
class RichContainer extends Proxy implements Container {

  IModel<?> getModel() {
    target.model
  }

  void addChild(Widget child) {
    target.addChild(child)
  }

  static {
    standardWidgets.each { ConcreteWidget ->
      def selector = selectorForWidget(ConcreteWidget)
      RichContainer.metaClass."${selector}" = {  ... args ->
        def configurations = Arrays.asList(args)
        
        if (configurations.size() > 2)
          throw new MissingMethodException(selector, RichContainer, args)
          
        def widget = ConcreteWidget.newInstance([delegate.target]as Object[])
        def (bindings, description) = bindingsAndDescription(widget, configurations)
        widget.describe(description)
        widget.bind(bindings)    
      }
    }
  }
  
  private static bindingsAndDescription(widget, configurations) {
    def bindings = configurations.find { it instanceof Map } ?: [:]
    def description = configurations.find { it instanceof Closure } ?: {}
    [bindings, description]
  }

  static selectorForWidget(ConcreteWidget) {
    WordUtils.uncapitalize(ConcreteWidget.simpleName)
  }

  static getStandardWidgets() {
    new Reflections("org.uqbar.arena.widgets")
        .getSubTypesOf(Widget) //
        .findAll {
          !Modifier.isAbstract(it.modifiers) //
        }
  }
}