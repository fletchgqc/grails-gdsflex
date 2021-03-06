/*
  GRANITE DATA SERVICES
  Copyright (C) 2011 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

import grails.util.Environment
import grails.util.BuildSettings

Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << new File("${gdsflexPluginDir}/scripts/_FlexCommon.groovy")
includeTargets << new File("${gdsflexPluginDir}/scripts/_GrailsGas3.groovy")
includeTargets << new File("${gdsflexPluginDir}/scripts/_GrailsFlexCompiler.groovy")


packageCompileFlag = false

eventCompileEnd = {
    if (checkDir() && packageCompileFlag) {
        gas3()
    }
}

eventPackagingEnd = {
    if (checkDir()) {
        if (!packageCompileFlag) {
            gas3()
            packageCompileFlag = true
        }
		if (Environment.current == Environment.PRODUCTION && config?.as3Config.autoCompileFlex) {
            includeTargets << new File("${gdsflexPluginDir}/scripts/_GrailsFlexCompiler.groovy")
            println "Starting compile flex files"
            flexCompile()
        }
    }
}

def checkDir() {
    return gdsflexPluginDir.path != basedir
}
