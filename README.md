# react-native-canvas-card-scratch

A React Native component for drawing by touching on both iOS and Android.

## Features

- Support Android
- Stroke thickness and color are changable while drawing.
- Can undo strokes one by one.
- Can serialize path data to JSON. So it can sync other devices or someone else and continue to edit.
- Save drawing to a non-transparent image (png or jpg) or a transparent image (png only)
- Use vector concept. So sketches won't be cropped in different sizes of canvas.
- Support translucent colors and eraser.
- Support drawing on an image (Thanks to diego-caceres-galvan)
- High performance (See [below](#Performance). Thanks to jeanregisser)
- Can draw multiple canvases in the same screen.
- Can draw multiple multiline text on canvas.

## Installation

---

Install from `npm` (only support RN >= 0.40)

```bash
npm install @terrylinla/react-native-sketch-canvas --save
```

Link native code

```bash
react-native link @terrylinla/react-native-sketch-canvas
```

## Usage

---

### ● Using without UI component (for customizing UI)

```javascript
import React, { Component } from "react";
import { AppRegistry, StyleSheet, View } from "react-native";

import { SketchCanvas } from "@terrylinla/react-native-sketch-canvas";

export default class example extends Component {
  render() {
    return (
      <View style={styles.container}>
        <View style={{ flex: 1, flexDirection: "row" }}>
          <SketchCanvas
            style={{ flex: 1 }}
            strokeColor={"red"}
            strokeWidth={7}
          />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  }
});

AppRegistry.registerComponent("example", () => example);
```
