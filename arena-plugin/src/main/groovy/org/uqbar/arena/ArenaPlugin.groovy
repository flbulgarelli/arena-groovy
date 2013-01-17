package org.uqbar.arena

import org.gradle.api.Plugin
import org.gradle.api.Project

class ArenaPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {
    project.dependencies {           
      osSwitch(
          "windows:x86"    : { compile 'org.eclipse.swt.win32:org.eclipse.swt.win32.win32.x86:3.4.1.v3452b' },
          "windows:amd64"  : { compile 'org.eclipse.swt.win32:org.eclipse.swt.win32.win32.x86_64:3.4.1.v3449c' },
          "windows:x86_64" : { compile 'org.eclipse.swt.win32:org.eclipse.swt.win32.win32.x86_64:3.4.1.v3449c' },
          "unix:i386"      : { compile 'org.eclipse.swt.gtk:org.eclipse.swt.gtk.linux.x86:3.4.1.v3452b' },
          "unix:amd64"     : { compile 'org.eclipse.swt.gtk:org.eclipse.swt.gtk.linux.x86_64:3.5.2.v3557f' },
          "mac:x86_64"     : { compile 'org.eclipse.swt.cocoa.macosx:org.eclipse.swt.cocoa.macosx.x86_64:3.100.0.v4233d'  }
          )
    }
  }

  def osSwitch(cases) {
    def os = System.properties."os.name".toLowerCase()
    def family = familyForOs(os)
    def arch = System.properties."os.arch".toLowerCase()
    cases[("$family:$arch")].call()
  }

  def familyForOs(os) {
    if (os.contains("windows"))
      "windows"
    else if (os.contains("mac os"))
      "mac"
    else
      "unix"
  }
}



