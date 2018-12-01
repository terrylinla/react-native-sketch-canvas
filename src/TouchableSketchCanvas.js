'use strict';

import React from 'react'
import PropTypes from 'prop-types'
import ReactNative, {
    Platform,
    StyleSheet,
    View,
    TouchableWithoutFeedback
} from 'react-native'

import SketchCanvas from './SketchCanvas';

const touchStates = {
    'true': true,
    'false': false,
    draw: 'draw',
    touch: 'touch',
    none: 'none'
}

class TouchableSketchCanvas extends React.Component {
    static touchStates = touchStates;

    static propTypes = {
        ...SketchCanvas.propTypes,
        touchEnabled: PropTypes.oneOf(Object.keys(touchStates).map((key) => touchStates[key])),
        touchableComponent: PropTypes.element,
        forwardedRef: PropTypes.func.isRequired,
    };

    static defaultProps = {
        ...SketchCanvas.defaultProps,
        touchableComponent: <TouchableWithoutFeedback />,
    };

    static getLegacyTouchState(state) {
        return state === this.touchStates.draw || state === this.touchStates.true;
    }

    static getTouchState(state) {
        switch (state) {
            case this.touchStates.true:
                return this.touchStates.draw;
                break;
            case this.touchStates.false:
                return this.touchStates.none;
                break;
            default:
                return state;
                break;
        }
    }

    constructor(props) {
        super(props);

        this.getCanvasChildIndex = this.getCanvasChildIndex.bind(this);
        this.getCanvasChild = this.getCanvasChild.bind(this);
        this.updateCanvasChild = this.updateCanvasChild.bind(this);
        this.deleteCanvasChild = this.deleteCanvasChild.bind(this);
        this.canvasChildRef = this.canvasChildRef.bind(this);
        this.pushCanvasChild = this.pushCanvasChild.bind(this);
        this.updatePaths = this.updatePaths.bind(this);
        this.onStrokeStart = this.onStrokeStart.bind(this);
        this.onStrokeEnd = this.onStrokeEnd.bind(this);
        this.onPathsChange = this.onPathsChange.bind(this);
        this.refHandler = this.refHandler.bind(this);

        this.state = {
            touchEnabled: TouchableSketchCanvas.touchStates.draw,
            pathIds: []
        }
        this._canvasChildren = [];
        this._drawing = false;
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        return {
            touchEnabled: TouchableSketchCanvas.getTouchState(nextProps.touchEnabled),
        }
    }

    isPointOnPath(x, y, pathId, callback) {
        if (Platform.OS === 'ios') {
            return this.sketchCanvasInstance._isPointOnPath(x, y, pathId, callback);
        } else {
            const invokeCallback = (retVal) => callback ? callback(retVal) : retVal;
            return this.sketchCanvasInstance._isPointOnPath(x, y)
                .then((isPointOnPath) => {
                    if (!isPointOnPath) return invokeCallback(pathId ? false : []);
                    const promiseArr = this._canvasChildren
                        .map(({ ref, key }) => ref._isPointOnPath(x, y)
                            .then((isPointOnPath) => {
                                return { key, isPointOnPath }
                            }));
                    return Promise.all(promiseArr)
                        .then((arr) => {
                            const retVal = arr.filter(({ isPointOnPath }) => isPointOnPath).map(({ key }) => key);
                            return invokeCallback(pathId ? retVal.findIndex((id) => pathId === id) > -1 : retVal);
                        })
                })
        }
    }

    getCanvasChildIndex(pathId) {
        return this._canvasChildren.findIndex(({ key }) => {
            return key === pathId;
        });
    }

    getCanvasChild(pathId) {
        return this._canvasChildren[this.getCanvasChildIndex(pathId)];
    }

    updateCanvasChild(pathId, key, value) {
        this._canvasChildren[this.getCanvasChildIndex(pathId)][key] = value;
    }

    deleteCanvasChild(pathId) {
        this._canvasChildren.splice(this.getCanvasChildIndex(pathId), 1);
    }

    canvasChildRef(path, ref) {
        const pathId = path.path.id;
        if (ref) {
            this.updateCanvasChild(pathId, 'ref', ref);
            ref.addPath(path);
        }
    }

    pushCanvasChild(path) {
        const pathId = path.path.id;
        this._canvasChildren.push({
            key: pathId,
            canvas: <SketchCanvas
                key={`SketchCanvas@${pathId}`}
                touchEnabled={false}
                style={styles.canvas}
                ref={this.canvasChildRef.bind(this, path)}
            />,
            ref: null,
        })
    }

    updatePaths() {
        if (Platform.OS === 'ios') return;
        const paths = this.sketchCanvasInstance.getPaths();
        let shouldSetState = false;
        //  add paths
        paths
            .filter(({ path }) => {
                const pathId = path.id;
                return this.getCanvasChildIndex(pathId) === -1;
            })
            .map((path) => {
                this.pushCanvasChild(path);
                shouldSetState = true;
            });

        //  delete paths
        this._canvasChildren
            .filter(({ key }) => {
                return paths.findIndex(({ path }) => key === path.id) === -1;
            })
            .map(({ key }) => {
                this.deleteCanvasChild(key);
                shouldSetState = true;
            });

        shouldSetState && this.setState({ pathIds: paths.map(({ path }) => path.id) });
    }

    onStrokeStart(...args) {
        this._drawing = true;
        this.props.onStrokeStart(...args);
    }

    onStrokeEnd(...args) {
        this.updatePaths();
        this._drawing = false;
        this.props.onStrokeEnd(...args);
    }

    onPathsChange(numPaths) {
        if (!this._drawing) this.updatePaths();
        this.props.onPathsChange(numPaths);
    }

    refHandler(ref) {
        this.sketchCanvasInstance = ref;
        this.props.forwardedRef(ref);
    }

    render() {
        const { touchableComponent } = this.props;

        return (
            <View style={styles.default} pointerEvents='box-none'>
                {this._canvasChildren.map(child => child.canvas)}
                <SketchCanvas
                    {...this.props}
                    ref={this.refHandler}
                    isPointOnPath={this.isPointOnPath.bind(this)}
                    onStrokeStart={this.onStrokeStart}
                    onStrokeEnd={this.onStrokeEnd}
                    onPathsChange={this.onPathsChange}
                    touchEnabled={TouchableSketchCanvas.getLegacyTouchState(this.state.touchEnabled)} />
                {this.state.touchEnabled === TouchableSketchCanvas.touchStates.touch && touchableComponent &&
                    React.cloneElement(touchableComponent,
                    {
                        ...touchableComponent.props,
                        style: [touchableComponent.props.style, StyleSheet.absoluteFill]
                    },
                    <View style={StyleSheet.absoluteFill} />
                )}
            </View>
        );
    }
}

function forwardRef(props, ref) {
    return <TouchableSketchCanvas {...props} forwardedRef={ref} />;
}
forwardRef.displayName = `TouchableSketchCanvas`;

export default React.forwardRef(forwardRef);

const styles = StyleSheet.create({
    canvas: {
        ...StyleSheet.absoluteFillObject,
        opacity: 0
    },
    default: {
        flex: 1
    }
});