'use strict';

import React from 'react'
import PropTypes from 'prop-types'
import ReactNative, {
  requireNativeComponent,
  NativeModules,
  UIManager,
  PanResponder,
  PixelRatio,
  Platform,
  ViewPropTypes,
  processColor
} from 'react-native'
import { requestPermissions } from './handlePermissions';

const RNSketchCanvas = requireNativeComponent('RNSketchCanvas', SketchCanvas, {
  nativeOnly: {
    nativeID: true,
    onChange: true
  }
});
const SketchCanvasManager = NativeModules.RNSketchCanvasManager || {};

class SketchCanvas extends React.Component {
  static propTypes = {
    style: ViewPropTypes.style,
    strokeColor: PropTypes.string,
    strokeWidth: PropTypes.number,
    onPathsChange: PropTypes.func,
    onStrokeStart: PropTypes.func,
    onStrokeChanged: PropTypes.func,
    onStrokeEnd: PropTypes.func,
    onSketchSaved: PropTypes.func,
    user: PropTypes.string,

    touchEnabled: PropTypes.bool,

    text: PropTypes.arrayOf(PropTypes.shape({
      text: PropTypes.string,
      font: PropTypes.string,
      fontSize: PropTypes.number,
      fontColor: PropTypes.string,
      overlay: PropTypes.oneOf(['TextOnSketch', 'SketchOnText']),
      anchor: PropTypes.shape({ x: PropTypes.number, y: PropTypes.number }),
      position: PropTypes.shape({ x: PropTypes.number, y: PropTypes.number }),
      coordinate: PropTypes.oneOf(['Absolute', 'Ratio']),
      alignment: PropTypes.oneOf(['Left', 'Center', 'Right']),
      lineHeightMultiple: PropTypes.number,
    })),
    localSourceImage: PropTypes.shape({ filename: PropTypes.string, directory: PropTypes.string, mode: PropTypes.oneOf(['AspectFill', 'AspectFit', 'ScaleToFill']) }),

    permissionDialogTitle: PropTypes.string,
    permissionDialogMessage: PropTypes.string,
  };

  static defaultProps = {
    style: null,
    strokeColor: '#000000',
    strokeWidth: 3,
    onPathsChange: () => { },
    onStrokeStart: () => { },
    onStrokeChanged: () => { },
    onStrokeEnd: () => { },
    onSketchSaved: () => { },
    user: null,

    touchEnabled: true,

    text: null,
    localSourceImage: null,

    permissionDialogTitle: '',
    permissionDialogMessage: '',
  };

  state = {
    text: null
  }

  constructor(props) {
    super(props)
    this._pathsToProcess = []
    this._paths = []
    this._path = null
    this._handle = null
    this._screenScale = Platform.OS === 'ios' ? 1 : PixelRatio.get()
    this._offset = { x: 0, y: 0 }
    this._size = { width: 0, height: 0 }
    this._initialized = false

    this.state.text = this._processText(props.text ? props.text.map(t => Object.assign({}, t)) : null)
  }

  componentWillReceiveProps(nextProps) {
    this.setState({
      text: this._processText(nextProps.text ? nextProps.text.map(t => Object.assign({}, t)) : null)
    })
  }

  _processText(text) {
    text && text.forEach(t => t.fontColor = processColor(t.fontColor))
    return text
  }

  clear() {
    this._paths = []
    this._path = null
    UIManager.dispatchViewManagerCommand(this._handle, UIManager.getViewManagerConfig(RNSketchCanvas).Commands.clear, [])
  }

  undo() {
    let lastId = -1;
    this._paths.forEach(d => lastId = d.drawer === this.props.user ? d.path.id : lastId)
    if (lastId >= 0) this.deletePath(lastId)
    return lastId
  }

  addPath(data) {
    if (this._initialized) {
      if (this._paths.filter(p => p.path.id === data.path.id).length === 0) this._paths.push(data)
      const pathData = data.path.data.map(p => {
        const coor = p.split(',').map(pp => parseFloat(pp).toFixed(2))
        return `${coor[0] * this._screenScale * this._size.width / data.size.width},${coor[1] * this._screenScale * this._size.height / data.size.height}`;
      })
      UIManager.dispatchViewManagerCommand(this._handle, UIManager.getViewManagerConfig(RNSketchCanvas).Commands.addPath, [
        data.path.id, processColor(data.path.color), data.path.width * this._screenScale, pathData
      ])
    } else {
      this._pathsToProcess.filter(p => p.path.id === data.path.id).length === 0 && this._pathsToProcess.push(data)
    }
  }

  deletePath(id) {
    this._paths = this._paths.filter(p => p.path.id !== id)
    UIManager.dispatchViewManagerCommand(this._handle, UIManager.getViewManagerConfig(RNSketchCanvas).Commands.deletePath, [id])
  }

  save(imageType, transparent, folder, filename, includeImage, includeText, cropToImageSize) {
    UIManager.dispatchViewManagerCommand(this._handle, UIManager.getViewManagerConfig(RNSketchCanvas).Commands.save, [imageType, folder, filename, transparent, includeImage, includeText, cropToImageSize])
  }

  getPaths() {
    return this._paths
  }

  getBase64(imageType, transparent, includeImage, includeText, cropToImageSize, callback) {
    if (Platform.OS === 'ios') {
      SketchCanvasManager.transferToBase64(this._handle, imageType, transparent, includeImage, includeText, cropToImageSize, callback)
    } else {
      NativeModules.SketchCanvasModule.transferToBase64(this._handle, imageType, transparent, includeImage, includeText, cropToImageSize, callback)
    }
  }

  componentWillMount() {
    this.panResponder = PanResponder.create({
      // Ask to be the responder:
      onStartShouldSetPanResponder: (evt, gestureState) => true,
      onStartShouldSetPanResponderCapture: (evt, gestureState) => true,
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

        UIManager.dispatchViewManagerCommand(
          this._handle,
          UIManager.getViewManagerConfig(RNSketchCanvas).Commands.newPath,
          [
            this._path.id,
            processColor(this._path.color),
            this._path.width * this._screenScale
          ]
        )
        UIManager.dispatchViewManagerCommand(
          this._handle,
          UIManager.getViewManagerConfig(RNSketchCanvas).Commands.addPoint,
          [
            parseFloat((gestureState.x0 - this._offset.x).toFixed(2) * this._screenScale),
            parseFloat((gestureState.y0 - this._offset.y).toFixed(2) * this._screenScale)
          ]
        )
        const x = parseFloat((gestureState.x0 - this._offset.x).toFixed(2)), y = parseFloat((gestureState.y0 - this._offset.y).toFixed(2))
        this._path.data.push(`${x},${y}`)
        this.props.onStrokeStart(x, y)
      },
      onPanResponderMove: (evt, gestureState) => {
        if (!this.props.touchEnabled) return
        if (this._path) {
          UIManager.dispatchViewManagerCommand(this._handle, UIManager.getViewManagerConfig(RNSketchCanvas).Commands.addPoint, [
            parseFloat((gestureState.moveX - this._offset.x).toFixed(2) * this._screenScale),
            parseFloat((gestureState.moveY - this._offset.y).toFixed(2) * this._screenScale)
          ])
          const x = parseFloat((gestureState.moveX - this._offset.x).toFixed(2)), y = parseFloat((gestureState.moveY - this._offset.y).toFixed(2))
          this._path.data.push(`${x},${y}`)
          this.props.onStrokeChanged(x, y)
        }
      },
      onPanResponderRelease: (evt, gestureState) => {
        if (!this.props.touchEnabled) return
        if (this._path) {
          this.props.onStrokeEnd({ path: this._path, size: this._size, drawer: this.props.user })
          this._paths.push({ path: this._path, size: this._size, drawer: this.props.user })
        }
        UIManager.dispatchViewManagerCommand(this._handle, UIManager.getViewManagerConfig(RNSketchCanvas).Commands.endPath, [])
      },

      onShouldBlockNativeResponder: (evt, gestureState) => {
        return true;
      },
    });
  }

  async componentDidMount() {
    const isStoragePermissionAuthorized = await requestPermissions(
      this.props.permissionDialogTitle,
      this.props.permissionDialogMessage,
    );
  }

  render() {
    return (
      <RNSketchCanvas
        ref={ref => {
          this._handle = ReactNative.findNodeHandle(ref)
        }}
        style={this.props.style}
        onLayout={e => {
          this._size = { width: e.nativeEvent.layout.width, height: e.nativeEvent.layout.height }
          this._initialized = true
          this._pathsToProcess.length > 0 && this._pathsToProcess.forEach(p => this.addPath(p))
        }}
        {...this.panResponder.panHandlers}
        onChange={(e) => {
          if (e.nativeEvent.hasOwnProperty('pathsUpdate')) {
            this.props.onPathsChange(e.nativeEvent.pathsUpdate)
          } else if (e.nativeEvent.hasOwnProperty('success') && e.nativeEvent.hasOwnProperty('path')) {
            this.props.onSketchSaved(e.nativeEvent.success, e.nativeEvent.path)
          } else if (e.nativeEvent.hasOwnProperty('success')) {
            this.props.onSketchSaved(e.nativeEvent.success)
          }
        }}
        localSourceImage={this.props.localSourceImage}
        permissionDialogTitle={this.props.permissionDialogTitle}
        permissionDialogMessage={this.props.permissionDialogMessage}
        text={this.state.text}
      />
    );
  }
}

SketchCanvas.MAIN_BUNDLE = Platform.OS === 'ios' ? UIManager.getViewManagerConfig(RNSketchCanvas).Constants.MainBundlePath : '';
SketchCanvas.DOCUMENT = Platform.OS === 'ios' ? UIManager.getViewManagerConfig(RNSketchCanvas).Constants.NSDocumentDirectory : '';
SketchCanvas.LIBRARY = Platform.OS === 'ios' ? UIManager.getViewManagerConfig(RNSketchCanvas).Constants.NSLibraryDirectory : '';
SketchCanvas.CACHES = Platform.OS === 'ios' ? UIManager.getViewManagerConfig(RNSketchCanvas).Constants.NSCachesDirectory : '';

module.exports = SketchCanvas;
