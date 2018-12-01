module.exports = function (api) {
    api.cache(true)
    return {
        "presets": ["module:metro-react-native-babel-preset"],
    };
}

//  https://github.com/facebook/react-native/issues/21475#issuecomment-432509636