const fs = require('fs');
const path = require('path');

const appBuildGradlePath = path.join('android', 'app', 'build.gradle');

const defaultCompileStatement = "compile 'org.ethereum:geth:1.7.1'";
const requiredCompileStatement = "compile 'org.ethereum:geth:1.7.1'";


// android/app/build.gradle
// 0) Load the file
let buildGradleContents = fs.readFileSync(appBuildGradlePath, 'utf8');

// 1) Check that react-native-firebase compile statement is the correct format
buildGradleContents = buildGradleContents.replace(defaultCompileStatement, requiredCompileStatement);

// 4) Write file
fs.writeFileSync(appBuildGradlePath, buildGradleContents);
