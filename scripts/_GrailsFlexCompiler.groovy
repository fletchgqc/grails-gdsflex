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


Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << grailsScript("_GrailsCompile")
includeTargets << new File("${gdsflexPluginDir}/scripts/_FlexCommon.groovy")


private def getConfig(ClassLoader loader = Thread.currentThread().getContextClassLoader()) {

    Class groovyClass = loader.parseClass(new File("${gdsflexPluginDir}/src/groovy/org/granite/config/GraniteConfigUtil.groovy"))
    GroovyObject groovyObject = (GroovyObject)groovyClass.newInstance()
    
    return groovyObject.getUserConfig(loader)?.as3Config
}


flexLoaderSet = false

def configureFlexCompilerClasspath() {
	if (flexLoaderSet) {
		println "Flex compiler classpath already set" 
		return
	}
	
	GroovyClassLoader loader = new GroovyClassLoader()
	
	loader.addURL(new File("${flexSDK}/lib/adt.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/afe.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/aglj32.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/asc.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/asdoc.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik_ja.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-awt-util.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-bridge.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-css.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-dom.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-ext.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-gvt.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-parser.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-script.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-svg-dom.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-svggen.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-transcoder.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-util.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/batik-xml.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/commons-discovery.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/compc.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/mxmlc.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/mxmlc_ja.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/copylocale.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/digest.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/flex-compiler-oem.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/flex-fontkit.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/flex-messaging-common.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/mm-velocity-1.4.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/optimizer.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/rideau.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/swfutils.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/xmlParserAPIs.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/xercesImpl.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/xercesPatch.jar").toURI().toURL())
	loader.addURL(new File("${flexSDK}/lib/xalan.jar").toURI().toURL())
	
	loader.parseClass(new File("${gdsflexPluginDir}/scripts/flexcompiler/FlexCompilerException.groovy"))
	loader.parseClass(new File("${gdsflexPluginDir}/scripts/flexcompiler/FlexCompilerType.groovy"))
	loader.parseClass(new File("${gdsflexPluginDir}/scripts/flexcompiler/FlexCompiler.groovy"))
	loader.parseClass(new File("${gdsflexPluginDir}/scripts/flexcompiler/FlexCompilerWrapper.groovy"))
			
	flexLoaderSet = true
	
	println "Flex compiler classloader created"
	
	Thread.currentThread().setContextClassLoader(loader)
}

target(initFlexProject: "Init flex project") {
    depends(compilePlugins)
    
    GroovyClassLoader loader = new GroovyClassLoader(rootLoader)
    try {
    	loader.addURL(new File("${pluginClassesDirPath}").toURI().toURL())
    }
    catch (groovy.lang.MissingPropertyException e) {
    	// Before Grails 1.3
    	loader.addURL(new File("${classesDir}").toURI().toURL())
    }
    
	def as3Config = getConfig(loader)	
    
	def sourceDir = as3Config.srcDir ?: "${basedir}/grails-app/views/flex"
	def modules = as3Config.modules ?: []
	
	Ant.mkdir(dir: sourceDir)
	
    configureFlexCompilerClasspath()
	
    Class wrapperClass = Thread.currentThread().getContextClassLoader().loadClass("FlexCompilerWrapper")
	java.lang.reflect.Method wrapperInit = wrapperClass.getMethod("init", Object.class, Object.class, Object.class, Object.class, Object.class, Object.class)
	wrapperInit.invoke(null, flexSDK, basedir, gdsflexPluginDir, sourceDir, modules, grailsAppName)
}


target(flexCompile: "Compile the flex file to swf file") {
    depends(initFlexProject)
    
	Ant.copy(todir: "${basedir}/web-app/WEB-INF/flex", overwrite: false) {
	    fileset(dir: "${gdsflexPluginDir}/src/flex")
	}
	Ant.copy(todir: "${basedir}/web-app/WEB-INF/granite", overwrite: false) {
	    fileset(dir: "${gdsflexPluginDir}/src/granite")
	}
    
    Class wrapperClass = Thread.currentThread().getContextClassLoader().loadClass("FlexCompilerWrapper")
	java.lang.reflect.Method wrapperCompile = wrapperClass.getMethod("compile", Object.class)
	wrapperCompile.invoke(null, argsMap["params"])
}
