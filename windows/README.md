# [module name here] Windows Implementation

## Module Installation
You can either use autolinking on react-native-windows 0.63 and later or manually link the module on earlier realeases.

### Automatic install with autolinking on RNW >= 0.63
RNSketchCanvas supports autolinking. Just call: `npm i @terrylinla/react-native-sketch-canvas --save`

### Manual installation on RNW >= 0.62
1. `npm install @terrylinla/react-native-sketch-canvas --save`
2. Open your solution in Visual Studio 2019 (eg. `windows\yourapp.sln`)
3. Right-click Solution icon in Solution Explorer > Add > Existing Project...
4. Add `node_modules\@terrylinla\react-native-sketch-canvas\windows\RNSketchCanvas\RNSketchCanvas.vcxproj`
5. Right-click main application project > Add > Reference...
6. Select `RNSketchCanvas` in Solution Projects
7. In app `pch.h` add `#include "winrt/RNSketchCanvas.h"`
8. In `App.cpp` add `PackageProviders().Append(winrt::RNSketchCanvas::ReactPackageProvider());` before `InitializeComponent();`

### Using save on Windows
On Windows, `save()` will save the resulting image in the Pictures library. In order for the application to save successfully, the `Pictures Library` capability must be added to the application's manifest:
1. Open your solution in Visual Studio 2019 (eg. `windows\yourapp.sln`)
2. In the main application project, open the `Package.appxmanifest` file.
3. In the `Capabilities` tab, check the `Pictures Library` capability.

## Module development

If you want to contribute to this module Windows implementation, first you must install the [Windows Development Dependencies](https://aka.ms/rnw-deps).

You must temporarily install the `react-native-windows` package. Versions of `react-native-windows` and `react-native` must match, e.g. if the module uses `react-native@0.62`, install `npm i react-native-windows@^0.62 --dev`.

Now, you will be able to open corresponding `RNSketchCanvas...sln` file, e.g. `RNSketchCanvas62.sln` for `react-native-windows@0.62`.