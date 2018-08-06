react-native-sketch-canvas
===================

A React Native component for drawing by touching on both iOS and Android.

<img src="https://media.giphy.com/media/3ov9kbuQg8ayvoYG8E/giphy.gif" height="400" /> <img src="https://media.giphy.com/media/3ov9jNZooUPTbWWbh6/giphy.gif" height="400" />

Features
-------------
* Support iOS and Android
* Stroke thickness and color are changable while drawing.
* Can undo strokes one by one.
* Can serialize path data to JSON. So it can sync other devices or someone else and continue to edit.
* Save drawing to a non-transparent image (png or jpg) or a transparent image (png only)
* Use vector concept. So sketches won't be cropped in different sizes of canvas.
* Support translucent colors and eraser.
* Support drawing on an image (Thanks to diego-caceres-galvan)
* High performance (See [below](#Performance). Thanks to jeanregisser)


## Installation
-------------
Install from `npm` (only support RN >= 0.40)
```bash
npm install @terrylinla/react-native-sketch-canvas --save
```
Link native code
```bash
react-native link @terrylinla/react-native-sketch-canvas
```

## Usage
-------------
<img src="https://i.imgur.com/4qpiX8m.png" height="400" />

### ● Using without UI component (for customizing UI)
```javascript
import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  View,
} from 'react-native';

import { SketchCanvas } from '@terrylinla/react-native-sketch-canvas';

export default class example extends Component {
  render() {
    return (
      <View style={styles.container}>
        <View style={{ flex: 1, flexDirection: 'row' }}>
          <SketchCanvas
            style={{ flex: 1 }}
            strokeColor={'red'}
            strokeWidth={7}
          />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#F5FCFF',
  },
});

AppRegistry.registerComponent('example', () => example);
```

#### Properties
-------------
| Prop  | Type | Description |
| :------------ |:---------------:| :---------------| 
| style | `object` | Styles to be applied on canvas component |
| strokeColor | `string` | Set the color of stroke, which can be #RRGGBB or #RRGGBBAA. If strokeColor is set to #00000000, it will automatically become an eraser. <br/>NOTE: Once an eraser path is sent to Android, Android View will disable hardware acceleration automatically. It might reduce the canvas performance afterward. |
| strokeWidth | `number` | The thickness of stroke |
| onStrokeStart | `function` | An optional function called when user's finger touches the canvas (starts to draw) |
| onStrokeChanged | `function` | An optional function called when user's finger moves |
| onStrokeEnd | `function` | An optional function called when user's finger leaves the canvas (end drawing) |
| onSketchSaved | `function` | An optional function which accpets 2 arguments `success` and `path`. If `success` is true, image is saved successfully and the saved image path might be in second argument. In Android, image path will always be returned. In iOS, image is saved to camera roll or file system, path will be set to null or image location respectively. |
| onPathsChange | `function` | An optional function which accpets 1 argument `pathsCount`, which indicates the number of paths. Useful for UI controls. (Thanks to toblerpwn) |
| user | `string` | An identifier to identify who draws the path. Useful when undo between two users |
| touchEnabled | `bool` | If false, disable touching. Default is true.  |
| localSourceImage | `object` | Require an object (see [below](#objects)) which consists of `filename`, `directory`(optional) and `mode`(optional). If set, the image will be loaded and display as a background in canvas. (Thanks to diego-caceres-galvan))([Here](#background-image) for details) |
| permissionDialogTitle | `string` | Android Only: Provide a Dialog Title for the Image Saving PermissionDialog. Defaults to empty string if not set |
| permissionDialogMessage | `string` | Android Only: Provide a Dialog Message for the Image Saving PermissionDialog. Defaults to empty string if not set |

#### Methods
-------------
| Method | Description |
| :------------ |:---------------|
| clear() | Clear all the paths |
| undo() | Delete the latest path. Can undo multiple times. |
| addPath(path) | Add a path (see [below](#objects)) to canvas.  |
| deletePath(id) | Delete a path with its `id` |
| save(imageType, transparent, folder, filename, includeImage, cropToImageSize) | Save image to camera roll or filesystem. If `localSourceImage` is set and a background image is loaded successfully, set `includeImage` to true to include background image and set `cropToImageSize` to true to crop output image to background image.<br/>Android: Save image in `imageType` format with transparent background (if `transparent` sets to True) to **/sdcard/Pictures/`folder`/`filename`** (which is Environment.DIRECTORY_PICTURES).<br/>iOS: Save image in `imageType` format with transparent background (if `transparent` sets to True) to camera roll or file system. If `folder` and `filename` are set, image will save to **temporary directory/`folder`/`filename`** (which is NSTemporaryDirectory())  |
| getPaths() | Get the paths that drawn on the canvas |
| getBase64(imageType, transparent, includeImage, cropToImageSize, callback) | Get the base64 of image and receive data in callback function, which called with 2 arguments. First one is error (null if no error) and second one is base64 result. |

#### Constants
-------------
| Constant | Description |
| :------------ |:---------------|
| MAIN_BUNDLE | Android: empty string, '' <br/>iOS: equivalent to [[NSBundle mainBundle] bundlePath] |
| DOCUMENT | Android: empty string, '' <br/>iOS: equivalent to NSDocumentDirectory |
| LIBRARY | Android: empty string, '' <br/>iOS: equivalent to NSLibraryDirectory |
| CACHES | Android: empty string, '' <br/>iOS: equivalent to NSCachesDirectory |

### ● Using with build-in UI components
<img src="https://i.imgur.com/O0vVdD6.png" height="400" />

```javascript
import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  Alert,
} from 'react-native';

import RNSketchCanvas from '@terrylinla/react-native-sketch-canvas';

export default class example extends Component {
  render() {
    return (
      <View style={styles.container}>
        <View style={{ flex: 1, flexDirection: 'row' }}>
          <RNSketchCanvas
            containerStyle={{ backgroundColor: 'transparent', flex: 1 }}
            canvasStyle={{ backgroundColor: 'transparent', flex: 1 }}
            defaultStrokeIndex={0}
            defaultStrokeWidth={5}
            closeComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Close</Text></View>}
            undoComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Undo</Text></View>}
            clearComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Clear</Text></View>}
            eraseComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Eraser</Text></View>}
            strokeComponent={color => (
              <View style={[{ backgroundColor: color }, styles.strokeColorButton]} />
            )}
            strokeSelectedComponent={(color, index, changed) => {
              return (
                <View style={[{ backgroundColor: color, borderWidth: 2 }, styles.strokeColorButton]} />
              )
            }}
            strokeWidthComponent={(w) => {
              return (<View style={styles.strokeWidthButton}>
                <View  style={{
                  backgroundColor: 'white', marginHorizontal: 2.5,
                  width: Math.sqrt(w / 3) * 10, height: Math.sqrt(w / 3) * 10, borderRadius: Math.sqrt(w / 3) * 10 / 2
                }} />
              </View>
            )}}
            saveComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Save</Text></View>}
            savePreference={() => {
              return {
                folder: 'RNSketchCanvas',
                filename: String(Math.ceil(Math.random() * 100000000)),
                transparent: false,
                imageType: 'png'
              }
            }}
          />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1, justifyContent: 'center', alignItems: 'center', backgroundColor: '#F5FCFF',
  },
  strokeColorButton: {
    marginHorizontal: 2.5, marginVertical: 8, width: 30, height: 30, borderRadius: 15,
  },
  strokeWidthButton: {
    marginHorizontal: 2.5, marginVertical: 8, width: 30, height: 30, borderRadius: 15,
    justifyContent: 'center', alignItems: 'center', backgroundColor: '#39579A'
  },
  functionButton: {
    marginHorizontal: 2.5, marginVertical: 8, height: 30, width: 60,
    backgroundColor: '#39579A', justifyContent: 'center', alignItems: 'center', borderRadius: 5,
  }
});

AppRegistry.registerComponent('example', () => example);
```

#### Properties
-------------
| Prop  | Type | Description |
| :------------ |:---------------:| :---------------| 
| containerStyle | `object` | Styles to be applied on container |
| canvasStyle | `object` | Styles to be applied on canvas component |
| onStrokeStart | `function` | See [above](#properties) |
| onStrokeChanged | `function` | See [above](#properties) |
| onStrokeEnd | `function` | See [above](#properties) |
| onPathsChange | `function` | See [above](#properties) |
| onClosePressed | `function` | An optional function called when user taps closeComponent |
| onUndoPressed | `function` | An optional function that accepts a argument `id` (the deleted id of path) and is called when user taps "undo" |
| onClearPressed | `function` | An optional function called when user taps clearComponent |
| user | `string` | See [above](#properties) |
| closeComponent | `component` | An optional component for closing |
| eraseComponent | `component` | An optional component for eraser |
| undoComponent | `component` | An optional component for undoing |
| clearComponent | `component` | An optional component for clearing |
| saveComponent | `component` | An optional component for saving |
| strokeComponent | `function` | An optional function which accpets 1 argument `color` and should return a component. |
| strokeSelectedComponent | `function` | An optional function which accpets 3 arguments `color`, `selectedIndex`, `isColorChanged` and should return a component. `isColorChanged` is useful for animating when changing color. Because rerendering also calls this function, we need `isColorChanged` to determine whether the component is rerendering or the selected color is changed. |
| strokeWidthComponent | `function` | An optional function which accpets 1 argument `width` and should return a component. |
| strokeColors | `array` | An array of colors. Example: `[{ color: '#000000' }, {color: '#FF0000'}]` |
| defaultStrokeIndex | `numbber` | The default index of selected stroke color |
| defaultStrokeWidth | `number` | The default thickness of stroke |
| minStrokeWidth | `number` | The minimum value of thickness |
| maxStrokeWidth | `number` | The maximum value of thickness |
| strokeWidthStep | `number` | The step value of thickness when tapping `strokeWidthComponent`. |
| savePreference | `function` | A function which is called when saving image and should return an object (see [below](#objects)). |
| onSketchSaved | `function` | See [above](#properties) |

#### Constants
-------------
| Constant | Description |
| :------------ |:---------------|
| MAIN_BUNDLE | See [above](#constants) |
| DOCUMENT | See [above](#constants) |
| LIBRARY | See [above](#constants) |
| CACHES | See [above](#constants) |

## Background Image
-------------
To use an image as background, `localSourceImage`(see [below](#background-image)) reqires an object, which consists of `filename`, `directory`(optional) and `mode`(optional). <br/>
Note: Because native module cannot read the file in JS bundle, file path cannot be relative to JS side. For example, '../assets/image/image.png' will fail to load image.
### Typical Usage
* Load image from app native bundle
<br/>
  * Android: 
    1. Put your images into android/app/src/main/res/drawable.
    2. Set `filename` to the name of image files with or without file extension. 
    3. Set `directory` to ''
<br/>
  * iOS:
    1. Open Xcode and add images to project by right clicking `Add Files to [YOUR PROJECT NAME]`.
    2. Set `filename` to the name of image files with file extension. 
    3. Set `directory` to MAIN_BUNDLE (e.g. RNSketchCanvas.MAIN_BUNDLE or SketchCanvas.MAIN_BUNDLE)
* Load image from camera
  1. Retrive photo complete path (including file extension) after snapping.
  2. Set `filename` to that path.
  3. Set `directory` to ''

### Content Mode
* AspectFill<br/>
<img src="https://i.imgur.com/vRydI60.png" height="200" />
* AspectFit (default)<br/>
<img src="https://i.imgur.com/r8DtgIN.png" height="200" />
* ScaleToFill<br/>
<img src="https://i.imgur.com/r9dRnAC.png" height="200" />

## Objects
-------------
### Use for saving image
```javascript
{
  folder: 'RNSketchCanvas', // use in Android, the folder name in Pictures
  filename: 'image',  // use in Android, the file name of image
  transparent: true,  // true for transparent background, ignored when imageType sets to 'jpg'
  imageType: 'jpg',  // one of 'jpg' or 'png'
  includeImage: true, // include background image if exists
  cropToImageSize: true  // crop output image to background image if exists
}
```

### Path format
```javascript
{
  drawer: 'user1',
  size: { // the size of drawer's canvas
    width: 480,
    height: 640
  },
  path: {
    id: 8979841, // path id
    color: '#FF000000', // ARGB or RGB
    width: 5,
    data: [
      "296.11,281.34",  // x,y
      "293.52,284.64",
      "290.75,289.73"
    ]
  }
}
```

### LocalSourceImage format
```javascript
{
  filename: 'image.png',  // e.g. 'image.png' or '/storage/sdcard0/Pictures/image.png'
  directory: '', // e.g. SketchCanvas.MAIN_BUNDLE or '/storage/sdcard0/Pictures/'
  mode: 'AspectFill'  // one of 'AspectFill', 'AspectFit' or 'ScaleToFill'
}
```

## Performance
-------------
1. For non-transparent path, both Android and iOS performances are good. Because when drawing non-transparent path, only last segment is drawn on canvas, no matter how long the path is, CPU usage is stable at about 20% and 15% in Android and iOS respectively. 
2. For transparent path, CPU usage stays at around 25% in Android, however, in iOS, CPU usage grows to 100% :(.
* Android (https://youtu.be/gXdCEN6Enmk)<br/>
<img src="https://i.imgur.com/YQ2wVMc.jpg" height="400" />
<img src="https://i.imgur.com/CuIar4h.jpg" height="400" />
* iOS (https://youtu.be/_jO4ky400Eo)<br/>
<img src="https://i.imgur.com/AwkFu94.png" height="400" />
<img src="https://i.imgur.com/UDcaiaz.png" height="400" />

## Example
-------------
The source code includes 3 examples, using build-in UI components, using with only canvas, and sync between two canvases.

Check full example app in the [example](./example) folder 

## Troubleshooting
-------------
Please refer  [here](https://github.com/terrylinla/react-native-sketch-canvas/wiki/Troubleshooting).
