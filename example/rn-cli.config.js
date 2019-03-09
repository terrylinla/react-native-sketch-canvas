
const path = require('path');
const fs = require('fs');
const _ = require('lodash');

// react-native >= 0.57
//https://medium.com/@slavik_210/symlinks-on-react-native-ae73ed63e4a7

const extraNodeModules = {};
const watchFolders = [];

function localRequire(dirname) {
    const packageJSON = require(path.resolve(dirname, 'package.json'));
    const allDep = {
        ...packageJSON.dependencies,
        ...packageJSON.devDependencies
    };

    const moduleMap = Object.keys(allDep)
        .reduce((modules, name) => {
            const isSymLink = allDep[name].match('file:');
            const pathToNodeModule = path.resolve(__dirname, 'node_modules', name);
            const pathToModule = isSymLink ? path.resolve(dirname, allDep[name].substring('file:'.length)) : fs.existsSync(pathToNodeModule) ? pathToNodeModule : path.resolve(dirname, 'node_modules', name);
            isSymLink && modules.watchFolders.push(pathToModule);
            modules.extraNodeModules[name] = pathToModule;
            return modules;
        }, { extraNodeModules: {}, watchFolders: [] });

    _.assign(extraNodeModules, moduleMap.extraNodeModules);
    watchFolders.push(...moduleMap.watchFolders);

    moduleMap.watchFolders.map((folder) => localRequire(folder));
}

localRequire(__dirname);

module.exports = {
    resolver: {
        extraNodeModules
    },
    watchFolders,
    watch: true,
    resetCache: true
};