const fs = require('fs')
const path = require('path')

const appBuildGradlePath = path.join('android', 'app', 'build.gradle')
const defaultCompileStatement = "compile 'org.ethereum:geth:1.8.3'"
const requiredCompileStatement = "compile 'org.ethereum:geth:1.8.3'"
let buildGradleContents = fs.readFileSync(appBuildGradlePath, 'utf8')
buildGradleContents = buildGradleContents.replace(defaultCompileStatement, requiredCompileStatement)
fs.writeFileSync(appBuildGradlePath, buildGradleContents)
