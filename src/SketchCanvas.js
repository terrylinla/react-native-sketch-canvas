'use strict';

import React from 'react';
import PropTypes from 'prop-types';
import ReactNative, {
    requireNativeComponent,
    NativeModules,
    UIManager,
    PanResponder,
    PixelRatio,
    Platform,
    ViewPropTypes,
    processColor
} from 'react-native';
import { requestPermissions } from './handlePermissions';
import { cloneDeep } from 'lodash';

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
        onLayout: PropTypes.func,
        strokeColor: PropTypes.string,
        strokeWidth: PropTypes.number,
        onPathsChange: PropTypes.func,
        onStrokeStart: PropTypes.func,
        onStrokeChanged: PropTypes.func,
        onStrokeEnd: PropTypes.func,
        onSketchSaved: PropTypes.func,
        user: PropTypes.string,
        paths: PropTypes.arrayOf(PropTypes.shape({
            path: PropTypes.shape({
                id: PropTypes.number,
                color: PropTypes.string,
                width: PropTypes.number,
                data: PropTypes.arrayOf(PropTypes.string),
            }),
            size: PropTypes.shape({
                width: PropTypes.number,
                height: PropTypes.number
            }),
            drawer: PropTypes.string
        })),
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
        onLayout: () => { },
        strokeColor: '#000000',
        strokeWidth: 3,
        onPathsChange: () => { },
        onStrokeStart: () => { },
        onStrokeChanged: () => { },
        onStrokeEnd: () => { },
        onSketchSaved: () => { },
        user: null,
        paths: [],
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
        this._pathsToProcess = [];
        this._paths = []
        this._path = null
        this._handle = null
        this._screenScale = Platform.OS === 'ios' ? 1 : PixelRatio.get()
        this._offset = { x: 0, y: 0 }
        this._size = { width: 0, height: 0 }
        this._initialized = false

        this.state.text = this._processText(props.text ? props.text.map(t => Object.assign({}, t)) : null)
        this._loadPanResponder.call(this);
        this.isPointOnPath = this.isPointOnPath.bind(this);
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        return {
            text: SketchCanvas._processText(nextProps.text ? nextProps.text.map(t => Object.assign({}, t)) : null)
        }
    }

    static _processText(text) {
        text && text.forEach(t => t.fontColor = processColor(t.fontColor))
        return text
    }

    _processText(text) {
        return SketchCanvas._processText(text);
    }

    clear() {
        this._paths = []
        this._path = null
        UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.clear, [])
    }

    undo() {
        let lastId = -1;
        this._paths.forEach(d => lastId = d.drawer === this.props.user ? d.path.id : lastId)
        if (lastId >= 0) this.deletePath(lastId)
        return lastId
    }

    addPaths(paths) {
        if (this._initialized) {
            const parsedPaths = paths.map((data) => {
                if (this._paths.filter(p => p.path.id === data.path.id).length === 0) this._paths.push(data);
                return {
                    id: data.path.id,
                    color: processColor(data.path.color),
                    strokeWidth: data.path.width * this._screenScale,
                    coords: data.path.data.map(p => {
                        const coor = p.split(',').map(pp => parseFloat(pp).toFixed(2));
                        return `${coor[0] * this._screenScale * this._size.width / data.size.width},${coor[1] * this._screenScale * this._size.height / data.size.height}`;
                    })
                };
            });
            
            UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.addPaths, parsedPaths);
        }
        else {
            paths.map((data) => this._pathsToProcess.filter(p => p.path.id === data.path.id).length === 0 && this._pathsToProcess.push(data));
        }
    }


    addPath(data) {
        return this.addPaths([data]);
    }

    deletePaths(pathIds) {
        this._paths = this._paths.filter(p => pathIds.findIndex(id => p.path.id === id) === -1);
        UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.deletePaths, pathIds);
    }

    deletePath(id) {
        this.deletePaths([id]);
    }

    save(imageType, transparent, folder, filename, includeImage, includeText, cropToImageSize) {
        UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.save, [imageType, folder, filename, transparent, includeImage, includeText, cropToImageSize])
    }

    getPaths() {
        return this._paths;
    }

    getBase64(imageType, transparent, includeImage, includeText, cropToImageSize, callback) {
        if (Platform.OS === 'ios') {
            SketchCanvasManager.transferToBase64(this._handle, imageType, transparent, includeImage, includeText, cropToImageSize, callback)
        } else {
            NativeModules.SketchCanvasModule.transferToBase64(this._handle, imageType, transparent, includeImage, includeText, cropToImageSize, callback)
        }
    }

    isPointOnPath(x, y, pathId, callback) {
        const nativeX = Math.round(x * this._screenScale);
        const nativeY = Math.round(y * this._screenScale);
        const normalizedPathId = typeof pathId === 'number' ? pathId : -1;
        const nativeMethod = (callback) => {
            if (Platform.OS === 'ios') {
                SketchCanvasManager.isPointOnPath(this._handle, nativeX, nativeY, normalizedPathId, callback);
            } else {
                NativeModules.SketchCanvasModule.isPointOnPath(this._handle, nativeX, nativeY, normalizedPathId, callback);
            }
        };

        if (callback) {
            nativeMethod(callback);
        }
        else {
            return new Promise((resolve, reject) => {
                nativeMethod((err, success) => {
                    if (err) reject(err);
                    resolve(success);
                });
            });
        }
    }

    setTouchRadius(radius, callback) {
        const r = typeof radius === 'number' ? Math.round(radius * this._screenScale) : 0;

        const nativeMethod = (callback) => {
            if (Platform.OS === 'ios') {
                //  need to implement native callback 
                //SketchCanvasManager.setTouchRadius(this._handle, r, callback);
            } else {
                NativeModules.SketchCanvasModule.setTouchRadius(this._handle, r, callback);
            }
        };

        if (callback) {
            nativeMethod(callback);
        }
        else {
            return new Promise((resolve, reject) => {
                nativeMethod((err, success) => {
                    if (err) reject(err);
                    resolve(success);
                });
            });
        }
    }


    _loadPanResponder() {
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
                const x = parseFloat((evt.nativeEvent.locationX).toFixed(2)), y = parseFloat((evt.nativeEvent.locationY).toFixed(2));

                UIManager.dispatchViewManagerCommand(
                    this._handle,
                    UIManager.RNSketchCanvas.Commands.newPath,
                    [
                        this._path.id,
                        processColor(this._path.color),
                        this._path.width * this._screenScale
                    ]
                )
                UIManager.dispatchViewManagerCommand(
                    this._handle,
                    UIManager.RNSketchCanvas.Commands.addPoint,
                    [
                        parseFloat(x * this._screenScale),
                        parseFloat(y * this._screenScale)
                    ]
                )
                this._path.data.push(`${x},${y}`)
                this.props.onStrokeStart(x, y)
            },
            onPanResponderMove: (evt, gestureState) => {
                if (!this.props.touchEnabled) return
                if (this._path) {
                    const x = parseFloat((evt.nativeEvent.locationX).toFixed(2)), y = parseFloat((evt.nativeEvent.locationY).toFixed(2));
                    UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.addPoint, [
                        parseFloat(x * this._screenScale),
                        parseFloat(y * this._screenScale)
                    ])

                    this._path.data.push(`${x},${y}`)
                    this.props.onStrokeChanged(x, y)
                }
            },
            onPanResponderRelease: (evt, gestureState) => {
                if (!this.props.touchEnabled) return
                if (this._path) {
                    this._paths.push({ path: this._path, size: this._size, drawer: this.props.user })
                    this.props.onStrokeEnd({ path: this._path, size: this._size, drawer: this.props.user })
                }
                UIManager.dispatchViewManagerCommand(this._handle, UIManager.RNSketchCanvas.Commands.endPath, [])
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
                    this._handle = ReactNative.findNodeHandle(ref);
                }}
                style={this.props.style}
                onLayout={e => {
                    this._size = { width: e.nativeEvent.layout.width, height: e.nativeEvent.layout.height };
                    this._initialized = true;
                    this._pathsToProcess.length > 0 && this._pathsToProcess.forEach(p => this.addPath(p));
                    this.props.onLayout(e);
                }}
                {...this.panResponder.panHandlers}
                onChange={(e) => {
                    if (!this._initialized) return;
                    if (e.nativeEvent.hasOwnProperty('pathsUpdate')) {
                        this.props.onPathsChange(e.nativeEvent.pathsUpdate);
                    } else if (e.nativeEvent.hasOwnProperty('success') && e.nativeEvent.hasOwnProperty('path')) {
                        this.props.onSketchSaved(e.nativeEvent.success, e.nativeEvent.path);
                    } else if (e.nativeEvent.hasOwnProperty('success')) {
                        this.props.onSketchSaved(e.nativeEvent.success);
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

SketchCanvas.MAIN_BUNDLE = Platform.OS === 'ios' ? UIManager.RNSketchCanvas.Constants.MainBundlePath : '';
SketchCanvas.DOCUMENT = Platform.OS === 'ios' ? UIManager.RNSketchCanvas.Constants.NSDocumentDirectory : '';
SketchCanvas.LIBRARY = Platform.OS === 'ios' ? UIManager.RNSketchCanvas.Constants.NSLibraryDirectory : '';
SketchCanvas.CACHES = Platform.OS === 'ios' ? UIManager.RNSketchCanvas.Constants.NSCachesDirectory : '';

module.exports = SketchCanvas;
