Arena-Groovy
============

Arena-Groovy provee integración entre el framework educativo de presentación [Arena](https://github.com/uqbar-project/arena) y el lenguaje de programación Groovy.


# Requerimientos

* Groovy 2.x
* Java 7
* Arena 3

# Integración básica

Dado que tanto Arena es un framework de presentación para la JVM, es posible desarrollar aplicaciones Arena usando el lenguje Groovy de forma nativa (existen sin embargo mínimas incompatiilidades, actualmente la versión compatible se encuentra en [este fork](https://github.com/flbulgarelli/arena)).

Sin embargo, siendo que Groovy posee bloques de código, puede resultar útil empelar los mismos para definir Action's y Transformer's. Para lograr esto, basta con hacer ```import static``` de  ```org.uqbar.arena.groovy.GroovyArenaExtensions```. Ejemplo:

```groovy
  def button = new Button(parent)
  button.caption = "Convertir a kilómetros"
  button.onClick(action { modelObject.convertir() })   
```

[el ejemplo completo se encuentra aquí](https://github.com/flbulgarelli/ejercicios-ui/blob/master/arena/conversor-ui-arena-groovy/src/main/groovy/org/uqbar/arena/examples/conversor/ConversorWindow.groovy)

# Integración con DSL

Arena-Groovy provee un DSL para construir de forma declarativa las UIs, que se puede usar en distintos niveles: descripción de la estructura jerarquica, binding contra propiedades, binding avanzado, extensiones. 

En cualquier caso, para poder emplear el DSL, es necesario que la ventana principal o aplicación, hereden de ```GroovyMainWindow``` o ```GroovyApplication```, en lugar de ```MainWindow``` o ```Appplication```, respectivamente. 

## Descripción jerárquica

Las UIs tienen una estructura naturalmente jerárquica: los componentes (```Widget```s) de la misma constituyen un arbol, donde las hojas son típicamente controles, y tanto la raiz como nodos son contenedores. 

El DSL agrega a todos los componentes el método ```describe```. que toma un bloque de código por parámetro, que constituye la descripción del componente. Ejemplo:


```groovy
  mainPanel.describe {
    //la descripción va aquí
  }
```

Dentro del bloque descriptor, podemos ejecutar cualquier código groovy valido, referenciando al componente descripto mediante el parámetro ```it```. Además, mensajes y propierdades con receptor implícito son automáticamente ruteados a ```it```. De esta forma, podemos instanciar otros componentes que sean hijos, de la siguiente forma:


```groovy
  mainPanel.describe {
    layout = new VerticalLayout()      
    new Label(it).describe { 
      text = "Ingrese la longitud en millas" 
    }
    new TextBox(it).describe { 
      bindValueToProperty("millas") 
    }
    new Button(it).describe {
      caption = "Convertir a kilómetros"
      onClick { this.modelObject.convertir() }
    }
    //etc..
  }
```

Como se puede observar, dentro de un bloque ```describe``` las conversiones de bloque de código a Trasnformer o Action son implícitas (no fue necesario el uso del método ```action``` en la registración de ```onClick```.

Nótese también que __el uso explícito de la referencia ```this``` es obligatorio dentro del bloque describe__, ya que como se señaló antes, los mensajes con receptor implícito son ruteados al componte descriptor. 

Dado que esta forma de insertar hijos en un contenedor es tan común, cuando el componente descripto es un contenedor (implenta ```Container```), el DSL de Arena expone dentro del bloque descripción atajos para crear hijos estándares (cualquiera de los Widgets concretos provistos por el jar de Arena en ```org.uqbar.arena.widgets```). Ejemplo: 

```groovy
  mainPanel.describe {
    layout = new VerticalLayout()      
    label { 
      text = "Ingrese la longitud en millas" 
    }
    textBox { 
      bindValueToProperty("millas") 
    }
    button {
      caption = "Convertir a kilómetros"
      onClick { this.modelObject.convertir() }
    }
    //etc..
  }
```

El ejemplo completo se encuentra [aquí](https://github.com/flbulgarelli/ejercicios-ui/blob/master/arena/conversor-ui-arena-groovy/src/main/groovy/org/uqbar/arena/groovy/dsl/examples/conversor/ConversorWindow.groovy)


## Binding básico

Para establecer el binding entre las propiedades de los componentes de vista y las propiedades del modelo de la vista/aplicación, Arena expone mensajes de la forma ```bindXYZToProperty(propiedad)```, donde XYZ es justamente esa propiedad dl componente visual a enlazar. 

Si bien es posible enviar expliícitamente estos mensajes dentro de un bloque descriptor, por ejemplo

```
  mainPanel.describe {
    //...
    textBox { 
      bindValueToProperty("millas") 
    }
    //...
    label {
      background = Color.ORANGE
      bindValueToProperty("kilometros")
    }
    //...
  }
```

el DSL ofrece atajos: todos los métodos que crean componentes dentro de un bloque descriptor (por ejemplo, ```textBox { ... }```), aceptan además, de forma opcional, un diccionario (```java.util.Map```) de bindings, donde las claves son las propiedades del componente visual, y los valores, las propiedades del modelo enlazadas. 

Así, el ejemplo anterior puede ser reescrito de la siguiente forma:

```groovy
  mainPanel.describe {
    //..
    textBox(value: "millas") 
    //...
    label(value: "kilometros") {
      background = Color.ORANGE
    }
    //...
  }
```

Este es un buen momento para notar además que los bloques vacios son opcionales (```textBox(value: "millas")``` no tiene ningún bloque) 

El ejemplo completo se encuentra [aquí](https://github.com/flbulgarelli/ejercicios-ui/blob/master/arena/conversor-ui-arena-groovy/src/main/groovy/org/uqbar/arena/groovy/dsl/binding/examples/conversor/ConversorWindow.groovy)
  

## Binding avanzado

Arena sin embargo no soporta solo binding contra propiedades. Algunos componentes (```TreeNode```'s y ```Column```'s) soportan además binding contra transformers. Con el DSL, La configuracion es similar, pero pasando un bloque en lugar de un string. Ejemplo: 


```groovy
  table.describe {
    //...    
    column(contents: {it.fecha.format("dd/MM/yyyy")}) {
      title = "Fecha de ingreso"
      fixedSize = 200
    }
    //...
  }
```

Por último, Arena también permite configurar los bindings, proveyendo adaptadores para los tipos de datos a ambos lados del binding:

```groovy
    mainPanel.describe {
      panel {
        layout = new ColumnLayout(2)
        
        label { text = "Nombre" }
        textBox(value: "nombre")
        
        //etc...
 
        label { text = "Fecha de Ingreso" }
        textBox(value: "fecha".adaptDate()) 
    }
  }
```

Como se observa, esto se logra mediante métodos específicos a Arena que el DSL le agrega a los Strings: 
 * .notNull(): bindea contra una property adaptada con NotNullTransformer
 * .notEmpty(): bindea contra una property adaptada con NotEmptyTransformer
 * .adaptDate(): bindea contra una property adaptada con DateAdapter
 * .adapt(Adapter) : bindea contra una property adaptada con el Transformer dado

El ejemplo completo se encuentra [aquí](https://github.com/flbulgarelli/ejercicios-ui/blob/master/arena/videoclub-arena-groovy/src/main/groovy/uqbar/videoclub/arena/AbstractSocioDialog.groovy)

## Extensiones

No hay que olvidarse de que a fin de cuentas, el DSL es código Groovy, por lo que es suceptible de, por ejemplo, extraer extraer partes de la descripción a otros métodos. Por ejemplo, para introducir un _template method_:

```groovy
  void createResultsGrid(Panel mainPanel) {
    mainPanel.describe {
      table(items: "results", selection: "selected") {
        itemType = this.modelObject.entityType
        this.describeResultsGrid(it)
      }
    }
  }

  abstract void describeResultsGrid(table)
```

Otro motivo para extraer un método es para definir un componente al vuelo. Por ejemplo, supongamos que siempre que querramos un TextBox simple bindeado a una propiedad, lo querremos etiquetar mediante un Label. Entonces nuestro código repetirá una y otra vez la siguiente estructura:

```groovy
  parent.describe {
    //... 
     
    label { text = /* el texto */ }
    textBox(value: /* el binding */)
    
    //...
  }
```

Lo que podemos generalizar en un método:

```groovy
  static labeledTextBox(parent, labelText, valueBinding) {
    parent.describe {
      label { text = labelText }
      textBox(value: valueBinding)
    }
  }
```

Y usarlo, por ejemplo, de la siguiente forma:

```groovy  
  void createFormPanel(Panel mainPanel) {
    mainPanel.describe {
      panel {
        //...                
        this.labeledTextBox(it, "Nombre", "nombre")
        this.labeledTextBox(it, "Direccion", "direccion")
        //etc...
      }
    }
  }
```


Si bien esto es perfectamente válido, el DSL permite registrar extensiones como esta, para lograr los mismos resultados, pero haciendo que nuestro ```labeledTextBox``` se vea nativo del DSL. Para esto, registramos en el objeto ```ArenaDSL```

```groovy
  static {
    ArenaDSL.registerContainerExtension("labeledTextBox", VideoclubApplication.&labeledTextBox)    
  }

  static labeledTextBox(parent, labelText, valueBinding) {
    parent.describe {
      label { text = labelText }
      textBox(value: valueBinding)
    }
  }
```

Y usarlo de la siguiente forma: 

```groovy  
  void createFormPanel(Panel mainPanel) {
    mainPanel.describe {
      panel {
        //...                
        labeledTextBox("Nombre", "nombre")
        labeledTextBox("Direccion", "direccion")
        //etc...
      }
    }
  }
```

El ejemplo completo se encuentra [aquí](https://github.com/flbulgarelli/ejercicios-ui/tree/master/arena/videoclub-arena-groovy/src/main/groovy/uqbar/videoclub/arena/extensions)
