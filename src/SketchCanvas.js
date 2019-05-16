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
    processColor,
    View
} from 'react-native';
import { requestPermissions } from './handlePermissions';
let PanGestureHandler, GHState;
try {
    const { PanGestureHandler: Handler, State } = require('react-native-gesture-handler');
    PanGestureHandler = Handler;
    GHState = State;
}
catch (err) {
    console.warn(err);
}

const RNSketchCanvas = requireNativeComponent('RNSketchCanvas', SketchCanvas, {
    nativeOnly: {
        nativeID: true,
        onChange: true
    }
});
const SketchCanvasManager = NativeModules.RNSketchCanvasManager || {};
const { Commands, Constants } = UIManager.getViewManagerConfig ? UIManager.getViewManagerConfig('RNSketchCanvas') : UIManager.RNSketchCanvas;

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

        hardwareAccelerated: PropTypes.bool,

        panHandler: PropTypes.any
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

        hardwareAccelerated: Platform.OS === 'android' ? false : undefined,

        panHandler: React.createRef()
    };

    static generatePathId() {
        return Math.round(Math.random() * 100000000);
    }
    
    constructor(props) {
        super(props);
        this._pathsToProcess = [];
        this._paths = [];
        this._path = null;
        this._handle = null;
        this._screenScale = Platform.OS === 'ios' ? 1 : PixelRatio.get();
        this._offset = { x: 0, y: 0 };
        this._size = { width: 0, height: 0 };
        this._initialized = false;
        
        this._loadPanResponder.call(this);
        this.isPointOnPath = this.isPointOnPath.bind(this);

        this.state = {
            text: this._processText(props.text ? props.text.map(t => Object.assign({}, t)) : null)
        };
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        return {
            text: SketchCanvas._processText(nextProps.text ? nextProps.text.map(t => Object.assign({}, t)) : null)
        };
    }

    async componentDidMount() {
        const isStoragePermissionAuthorized = await requestPermissions(
            this.props.permissionDialogTitle,
            this.props.permissionDialogMessage,
        );
    }

    static _processText(text) {
        text && text.forEach(t => t.fontColor = processColor(t.fontColor));
        return text;
    }

    _processText(text) {
        return SketchCanvas._processText(text);
    }

    clear() {
        this._paths = [];
        this._path = null;
        UIManager.dispatchViewManagerCommand(this._handle, Commands.clear, []);
    }

    undo() {
        let lastId = -1;
        this._paths.forEach(d => lastId = d.drawer === this.props.user ? d.path.id : lastId);
        if (lastId >= 0) this.deletePath(lastId);
        return lastId;
    }

    addPaths(paths) {
        if (this._initialized) {
            const parsedPaths = paths.map((data) => {
                if (this._paths.filter(p => p.path.id === data.path.id).length === 0) this._paths.push(data);
                /*
                return {
                    id: data.path.id,
                    color: processColor(data.path.color),
                    strokeWidth: data.path.width * this._screenScale,
                    coords: data.path.data.map(p => {
                        const coor = p.split(',').map(pp => parseFloat(pp).toFixed(2));
                        return `${coor[0] * this._screenScale * this._size.width / data.size.width},${coor[1] * this._screenScale * this._size.height / data.size.height}`;
                    })
                };
                */

                return [
                    data.path.id,
                    processColor(data.path.color),
                    data.path.width * this._screenScale,
                    data.path.data.map(p => {
                        const coor = p.split(',').map(pp => parseFloat(pp).toFixed(2));
                        return `${coor[0] * this._screenScale * this._size.width / data.size.width},${coor[1] * this._screenScale * this._size.height / data.size.height}`;
                    })
                ];
            });
            
            UIManager.dispatchViewManagerCommand(this._handle, Commands.addPaths, parsedPaths);
        }
        else {
            paths.map((data) => this._pathsToProcess.filter(p => p.path.id === data.path.id).length === 0 && this._pathsToProcess.push(data));
        }
    }
    /*
    addPath(data) {
        if (this._initialized) {
            if (this._paths.filter(p => p.path.id === data.path.id).length === 0) this._paths.push(data)
            const pathData = data.path.data.map(p => {
                const coor = p.split(',').map(pp => parseFloat(pp).toFixed(2))
                return `${coor[0] * this._screenScale * this._size.width / data.size.width},${coor[1] * this._screenScale * this._size.height / data.size.height}`;
            })
            UIManager.dispatchViewManagerCommand(this._handle, Commands.addPath, [
                data.path.id, processColor(data.path.color), data.path.width * this._screenScale, pathData
            ])
        } else {
            this._pathsToProcess.filter(p => p.path.id === data.path.id).length === 0 && this._pathsToProcess.push(data)
        }
    }
    */

    addPath(data) {
        return this.addPaths([data]);
    }

    deletePaths(pathIds) {
        this._paths = this._paths.filter(p => pathIds.findIndex(id => p.path.id === id) === -1);
        UIManager.dispatchViewManagerCommand(this._handle, Commands.deletePaths, pathIds);
    }

    deletePath(id) {
        this.deletePaths([id]);
    }

    save(imageType, transparent, folder, filename, includeImage, includeText, cropToImageSize) {
        UIManager.dispatchViewManagerCommand(this._handle, Commands.save, [imageType, folder, filename, transparent, includeImage, includeText, cropToImageSize]);
    }

    getPaths() {
        return this._paths.map(p => p);
    }

    getBase64(imageType, transparent, includeImage, includeText, cropToImageSize, callback) {
        if (Platform.OS === 'ios') {
            SketchCanvasManager.transferToBase64(this._handle, imageType, transparent, includeImage, includeText, cropToImageSize, callback);
        } else {
            NativeModules.SketchCanvasModule.transferToBase64(this._handle, imageType, transparent, includeImage, includeText, cropToImageSize, callback);
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

    startPath(x, y) {
        this._path = {
            id: SketchCanvas.generatePathId(),
            color: this.props.strokeColor,
            width: this.props.strokeWidth,
            data: []
        };
        const pointX = parseFloat(x.toFixed(2));
        const pointY = parseFloat(y.toFixed(2));

        UIManager.dispatchViewManagerCommand(
            this._handle,
            Commands.newPath,
            [
                this._path.id,
                processColor(this._path.color),
                this._path.width * this._screenScale
            ]
        );

        UIManager.dispatchViewManagerCommand(
            this._handle,
            Commands.addPoint,
            [
                parseFloat(pointX * this._screenScale),
                parseFloat(pointY * this._screenScale)
            ]
        );

        this._path.data.push(`${pointX},${pointY}`);
        this.props.onStrokeStart(pointX, pointY);
    }

    addPoint(x, y) {
        if (this._path) {
            const pointX = parseFloat(x.toFixed(2));
            const pointY = parseFloat(y.toFixed(2));

            UIManager.dispatchViewManagerCommand(this._handle, Commands.addPoint, [
                parseFloat(pointX * this._screenScale),
                parseFloat(pointY * this._screenScale)
            ]);

            this._path.data.push(`${pointX},${pointY}`);
            this.props.onStrokeChanged(pointX, pointY);
        }
    }

    closePath() {
        if (this._path) {
            this._paths.push({ path: this._path, size: this._size, drawer: this.props.user });
            this.props.onStrokeEnd({ path: this._path, size: this._size, drawer: this.props.user });
        }
        UIManager.dispatchViewManagerCommand(this._handle, Commands.endPath, []);
    }

    _grantResponder = (evt, gestureState) => this.props.touchEnabled && evt.nativeEvent.touches.length === 1;//gestureState.numberActiveTouches === 1;


    _loadPanResponder() {
        this.panResponder = PanResponder.create({
            // Ask to be the responder:
            onStartShouldSetPanResponder: this._grantResponder,
            onStartShouldSetPanResponderCapture: this._grantResponder,
            onMoveShouldSetPanResponder: this._grantResponder,
            onMoveShouldSetPanResponderCapture: this._grantResponder,

            onPanResponderGrant: (evt, gestureState) => {
                const e = evt.nativeEvent;
                this._offset = { x: e.pageX - e.locationX, y: e.pageY - e.locationY };
                this.startPath(e.locationX, e.locationY);
            },
            onPanResponderMove: (evt, gestureState) => {
                this.addPoint(evt.nativeEvent.locationX, evt.nativeEvent.locationY);
            },
            onPanResponderRelease: (evt, gestureState) => {
                this.closePath();
            },

            onShouldBlockNativeResponder: (evt, gestureState) => {
                return true;
            }
        });
    }

    onHandlerStateChange = (e) => {
        if (e.nativeEvent.state === GHState.BEGAN) this.startPath(e.nativeEvent.x, e.nativeEvent.y);
        if (e.nativeEvent.oldState === GHState.ACTIVE) this.closePath();
    }

    onGestureEvent = (e) => {
        this.addPoint(e.nativeEvent.x, e.nativeEvent.y);
    }
    
    _handleRef = (ref) => {
        this._handle = ReactNative.findNodeHandle(ref);
    }

    onLayout = (e) => {
        this._size = { width: e.nativeEvent.layout.width, height: e.nativeEvent.layout.height };
        this._initialized = true;
        this._pathsToProcess.length > 0 && this._pathsToProcess.forEach(p => this.addPath(p));
        this.props.onLayout(e);
    }

    onChange = (e) => {
        if (!this._initialized) return;
        if (e.nativeEvent.hasOwnProperty('pathsUpdate')) {
            this.props.onPathsChange(e.nativeEvent.pathsUpdate);
        } else if (e.nativeEvent.hasOwnProperty('success') && e.nativeEvent.hasOwnProperty('path')) {
            this.props.onSketchSaved(e.nativeEvent.success, e.nativeEvent.path);
        } else if (e.nativeEvent.hasOwnProperty('success')) {
            this.props.onSketchSaved(e.nativeEvent.success);
        }
    }

    renderBaseView() {
        return (
            <RNSketchCanvas
                ref={this._handleRef}
                style={this.props.style}
                onLayout={this.onLayout}
                onChange={this.onChange}
                localSourceImage={this.props.localSourceImage}
                permissionDialogTitle={this.props.permissionDialogTitle}
                permissionDialogMessage={this.props.permissionDialogMessage}
                text={this.state.text}
                hardwareAccelerated={this.props.hardwareAccelerated}
            />
        );
    }

    renderWithPanResponder() {
        return React.cloneElement(this.renderBaseView(), this.panResponder.panHandlers);
    }

    renderWithGestureHandler() {
        const { panHandler, touchEnabled, simultaneousHandlers, waitFor } = this.props;
        return (
            <PanGestureHandler
                ref={panHandler}
                enabled={touchEnabled}
                maxPointers={1}
                simultaneousHandlers={simultaneousHandlers}
                waitFor={waitFor}
                onHandlerStateChange={this.onHandlerStateChange}
                onGestureEvent={this.onGestureEvent}
            >
                {this.renderBaseView()}
            </PanGestureHandler>
        );
    }

    render() {
        return PanGestureHandler ? this.renderWithGestureHandler() : this.renderWithPanResponder();
    }
}

SketchCanvas.MAIN_BUNDLE = Platform.OS === 'ios' ? Constants.MainBundlePath : '';
SketchCanvas.DOCUMENT = Platform.OS === 'ios' ? Constants.NSDocumentDirectory : '';
SketchCanvas.LIBRARY = Platform.OS === 'ios' ? Constants.NSLibraryDirectory : '';
SketchCanvas.CACHES = Platform.OS === 'ios' ? Constants.NSCachesDirectory : '';

module.exports = SketchCanvas;
