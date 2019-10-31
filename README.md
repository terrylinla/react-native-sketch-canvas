# react-native-canvas-scratch-card

A React Native component for scratch card on Android.

## Installation

---

Install from `npm` (only support RN >= 0.40)

```bash
npm install @thalesness/react-native-canvas-scratch-card --save
```

Link native code

```bash
react-native link @thalesness/react-native-canvas-scratch-card
```

## Usage

```javascript
import React, { Component } from "react";
import { AppRegistry, View } from "react-native";

import { ScratchCard } from "@thalesness/react-native-canvas-scratch-card";

export default class example extends Component {
  render() {
    return <ScratchCard strokeWidth={40} fillColor={"#FF0000"} bgImage={{
         filename: “bulb.png”,
         directory: “”
       }} />;
  }
}

AppRegistry.registerComponent("example", () => example);
```

Set up your image in the in a drawable folder, as you can see [here](https://github.com/thalesgaldino/react-native-canvas-scratch-card-demo/tree/master/android/app/src/main/res/drawable-xxhdpi). To check out the demo, have a look [here](https://github.com/thalesgaldino/react-native-canvas-scratch-card-demo)
