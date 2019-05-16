const path = require('path');
const pkg = require(path.resolve(process.cwd(), './package.json'));
const _ = require('lodash');

const stubPath = path.resolve(__dirname, 'stub.js');
const extraNodeModules = _.has(pkg.dependencies, 'react-native-gesture-handler') ? {} : { 'react-native-gesture-handler': stubPath };

module.exports = {
    resolver: {
        extraNodeModules
    }
};