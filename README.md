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
    return <ScratchCard strokeWidth={40} fillColor={"#FF0000"} />;
  }
}

AppRegistry.registerComponent("example", () => example);
```
