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

Ant.property(environment:"env")
grailsHome = Ant.antProject.properties."env.GRAILS_HOME"

includeTargets << grailsScript("_GrailsPackage")
includeTargets << new File("${gdsflexPluginDir}/scripts/_GrailsGas3.groovy")
includeTargets << new File("${gdsflexPluginDir}/scripts/_GrailsInstallFlexTemplates.groovy")
includeTargets << grailsScript("_GrailsCompile")


import groovy.text.Template
import groovy.text.SimpleTemplateEngine


target ('default': "generate Flex app") {
	depends(packageApp)
	depends(installFlexTemplates)
	depends(gas3)
	
	def domainDir = new File("${basedir}/grails-app/domain")

	domainClassList = []
	list(domainDir, domainClassList)
	
	def engine = new SimpleTemplateEngine()
	def template = engine.createTemplate(new File("${gdsflexPluginDir}/src/templates/mainflexapp.gsp"))
	def binding = [ domainClassList: domainClassList ]
	Writable writable = template.make(binding)
	
    GroovyClassLoader loader = new GroovyClassLoader(rootLoader)
    loader.addURL(new File(classesDirPath).toURI().toURL())
    Class groovyClass = loader.parseClass(new File("${gdsflexPluginDir}/src/groovy/org/granite/config/GraniteConfigUtil.groovy"))
    GroovyObject groovyObject = (GroovyObject)groovyClass.newInstance()
    
    def as3Config = groovyObject.getUserConfig()?.as3Config
    
	def targetDir = as3Config.srcDir ?: "${basedir}/grails-app/views/flex/"
	if (targetDir.endsWith("/"))
		targetDir = targetDir.substring(0, targetDir.length()-1)
    
	File outFile = new File(targetDir + "/${grailsAppName}.mxml")
	int i = 0;
	File bakFile = outFile;
	while (bakFile.exists()) {
		i++;
		bakFile = new File(targetDir + "/${grailsAppName}.mxml.bak.${i}")
	}
	if (i > 0) {
		println "Backup existing Flex app to ${bakFile}"
		outFile.renameTo(bakFile)
	}
	
	outFile = new File(targetDir + "/${grailsAppName}.mxml")
	writable.writeTo(new FileWriter(outFile))
	
	println "Generated Flex app in ${targetDir}/${grailsAppName}.mxml"
}


def list(File dir, List domainClassList) {
	dir.eachFileRecurse {
		if (it.getPath().endsWith(".groovy")) {
			domainClassList << it.getPath().substring(dir.getPath().length()+1, it.getPath().length()-7).replace(File.separator, ".")
		}
	}
	
}
