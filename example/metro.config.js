const blacklist = require('metro-config/src/defaults/blacklist');
const { mergeConfig } = require("metro-config");
const path = require('path');
const pkg = require('./package.json');
const configB = require('../metro.config');

const config = {
    resolver: {
        blacklistRE: blacklist([path.resolve(__dirname, '../node_modules/react-native-gesture-handler')]),
        providesModuleNodeModules: Object.keys(pkg.dependencies)
    },
    watchFolders: [path.resolve(__dirname, '..')],
    transformer: {
        getTransformOptions: async () => ({
            transform: {
                experimentalImportSupport: true,
                inlineRequires: true
            }
        })
    },

};

module.exports = mergeConfig(config, configB);