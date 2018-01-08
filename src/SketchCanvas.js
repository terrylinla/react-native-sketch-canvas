'use strict';

import React from 'react'
import PropTypes from 'prop-types'
import ReactNative, {
  requireNativeComponent,
  NativeModules,
  UIManager,
  View,
  Text,
  TouchableOpacity,
  PanResponder,
  Dimensions,
  Platform
} from 'react-native'

const RNSketchCanvas = requireNativeComponent('RNSketchCanvas', SketchCanvas, {
  nativeOnly: {
    nativeID: true,
    onChange: true
  }
});
const SketchCanvasManager = NativeModules.RNSketchCanvasManager || {};

class SketchCanvas extends React.Component {
  static propTypes = {
    style: View.propTypes.style,
    strokeColor: PropTypes.string,
    strokeWidth: PropTypes.number,
    onStrokeStart: PropTypes.func,
    onStrokeChanged: PropTypes.func,
    onStrokeEnd: PropTypes.func,
    onSketchSaved: PropTypes.func,
    onBase64: PropTypes.func,
    user: PropTypes.string,

    touchEnabled: PropTypes.bool,
  };

  static defaultProps = {
    style: null,
    strokeColor: '#000000',
    strokeWidth: 3,
    onStrokeStart: () => {},
    onStrokeChanged: () => {},
    onStrokeEnd: () => {},
    onSketchSaved: () => {},
    onBase64: () => {},
    user: null,

    touchEnabled: true,
  };

  constructor(props) {
    super(props)

    this._pathsToProcess = []
    this._paths = []
    this._path = null
    this._handle = null
    this._screenScale = Platform.OS === 'ios' ? 1 : Dimensions.get('window').scale
    this._offset = { x: 0, y: 0 }
    this._size = { width: 0, height: 0 }
    this._initialized = false
  }

  clear() {
    this._paths = []
    this._path = null
    if (Platform.OS === 'ios') {
      SketchCanvasManager.clear()
    } else {
      UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.clear, [])
    }
  }

  undo() {
    let lastId = -1;
    this._paths.forEach(d => lastId = d.drawer === this.props.user ? d.path.id : lastId)
    if (lastId >= 0)  this.deletePath(lastId)
    return lastId
  }

  addPath(data) {
    if (this._initialized) {
      if (this._paths.filter(p => p.path.id === data.path.id).length === 0) this._paths.push(data)
      const pathData = data.path.data.map(p => {
        const coor = p.split(',').map(pp => parseFloat(pp).toFixed(2))
        return `${coor[0] * this._screenScale * this._size.width / data.size.width },${coor[1] * this._screenScale * this._size.height / data.size.height }`;
      })
      if (Platform.OS === 'ios') {
        SketchCanvasManager.addPath(data.path.id, data.path.color, data.path.width, pathData)
      } else {
        UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.addPath, [
          data.path.id, data.path.color, data.path.width, pathData
        ])
      }
    } else {
      this._pathsToProcess.filter(p => p.path.id === data.path.id).length === 0 && this._pathsToProcess.push(data)
    }
  }

  deletePath(id) {
    this._paths = this._paths.filter(p => p.path.id !== id)
    if (Platform.OS === 'ios') {
      SketchCanvasManager.deletePath(id)
    } else {
      UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.deletePath, [ id ])
    }
  }

  save(imageType, transparent, folder, filename) {
    if (Platform.OS === 'ios') {
      SketchCanvasManager.save(imageType, transparent)
    } else {
      UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.save, [ imageType, folder, filename, transparent ])
    }
  }

  getPaths() {
    return this._paths
  }

  getBase64(imageType, transparent, callback) {
    if (Platform.OS === 'ios') {
      SketchCanvasManager.transferToBase64(imageType, transparent, callback)
    } else {
      UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.getBase64, [ imageType, transparent ])
    }
  }

  componentWillMount() {
    this.panResponder = PanResponder.create({
      // Ask to be the responder:
      onStartShouldSetPanResponder: (evt, gestureState) => false,
      onStartShouldSetPanResponderCapture: (evt, gestureState) => false,
      onMoveShouldSetPanResponder: (evt, gestureState) => true,
      onMoveShouldSetPanResponderCapture: (evt, gestureState) => true,

      onPanResponderGrant: (evt, gestureState) => {
        if (!this.props.touchEnabled) return
        const e = evt.nativeEvent
        this._offset = { x: e.pageX - e.locationX, y: e.pageY - e.locationY }
        this._path = {
          id: parseInt(Math.random() * 100000000), color: this.props.strokeColor, 
          width: this.props.strokeWidth, data: []
        }
        
        if (Platform.OS === 'ios') {
          SketchCanvasManager.newPath(this._path.id, this._path.color, this._path.width)
          SketchCanvasManager.addPoint(
            parseFloat((gestureState.moveX - this._offset.x).toFixed(2) * this._screenScale), 
            parseFloat((gestureState.moveY - this._offset.y).toFixed(2) * this._screenScale)
          )
        } else {
          UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.newPath, [
            this._path.id, this._path.color, this._path.width,
          ])
          UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.addPoint, [
            parseFloat((gestureState.moveX - this._offset.x).toFixed(2) * this._screenScale), 
            parseFloat((gestureState.moveY - this._offset.y).toFixed(2) * this._screenScale)
          ])
        }
        this._path.data.push(`${parseFloat(gestureState.moveX - this._offset.x).toFixed(2)},${parseFloat(gestureState.moveY - this._offset.y).toFixed(2)}`)
        this.props.onStrokeStart()
      },
      onPanResponderMove: (evt, gestureState) => {
        if (!this.props.touchEnabled) return
        if (this._path) {
          if (Platform.OS === 'ios') {
            SketchCanvasManager.addPoint(
              parseFloat((gestureState.moveX - this._offset.x).toFixed(2) * this._screenScale), 
              parseFloat((gestureState.moveY - this._offset.y).toFixed(2) * this._screenScale)
            )
          } else {
            UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.addPoint, [
              parseFloat((gestureState.moveX - this._offset.x).toFixed(2) * this._screenScale), 
              parseFloat((gestureState.moveY - this._offset.y).toFixed(2) * this._screenScale)
            ])
          }
          this._path.data.push(`${parseFloat(gestureState.moveX - this._offset.x).toFixed(2)},${parseFloat(gestureState.moveY - this._offset.y).toFixed(2)}`)
          this.props.onStrokeChanged()
        }
      },
      onPanResponderRelease: (evt, gestureState) => {
        if (!this.props.touchEnabled) return
        if (this._path) {
          this.props.onStrokeEnd({ path: this._path, size: this._size, drawer: this.props.user })
          this._paths.push({ path: this._path, size: this._size, drawer: this.props.user })
        }
        if (Platform.OS === 'ios') {
          SketchCanvasManager.endPath()
        } else {
          UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.endPath, [])
        }
      },

      onShouldBlockNativeResponder: (evt, gestureState) => {
        return true;
      },
    });
  }

  render() {
    return (
      <RNSketchCanvas 
        ref={ref => {
          this._handle = ReactNative.findNodeHandle(ref)
        }}
        style={this.props.style}
        onLayout={e => {
          this._size={ width: e.nativeEvent.layout.width, height: e.nativeEvent.layout.height }
          this._initialized = true
          this._pathsToProcess.length > 0 && this._pathsToProcess.forEach(p => this.addPath(p))
        }}
        {...this.panResponder.panHandlers} 
        onChange={(e) => {
          if (e.nativeEvent.hasOwnProperty('success')) {
            this.props.onSketchSaved(e.nativeEvent.success)
          } else if (e.nativeEvent.hasOwnProperty('base64')) {
            this.props.onBase64(e.nativeEvent.base64)
          }
        }}
      />
    );
  }
}

module.exports = SketchCanvas;
