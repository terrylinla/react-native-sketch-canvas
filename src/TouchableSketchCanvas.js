'use strict';

import React from 'react';
import PropTypes from 'prop-types';
import ReactNative, {
    Platform,
    StyleSheet,
    View,
    TouchableWithoutFeedback,
    ViewPropTypes
} from 'react-native';

import SketchCanvas from './SketchCanvas';

const styles = StyleSheet.create({
    canvas: {
        ...StyleSheet.absoluteFillObject,
        opacity: 0
    },
    default: {
        flex: 1
    }
});

const TOUCH_STATES = {
    'true': true,
    'false': false,
    draw: 'draw',
    touch: 'touch',
    none: 'none'
}

class TouchableSketchCanvas extends React.Component {
    static TOUCH_STATES = TOUCH_STATES;

    static propTypes = {
        ...SketchCanvas.propTypes,
        touchEnabled: PropTypes.oneOf(Object.keys(TOUCH_STATES).map((key) => TOUCH_STATES[key])),
        touchableComponent: PropTypes.element,
        contentContainerStyle: ViewPropTypes.style,
        forwardedRef: PropTypes.func.isRequired,
    };

    static defaultProps = {
        ...SketchCanvas.defaultProps,
        touchableComponent: <TouchableWithoutFeedback />,
        contentContainerStyle: styles.default
    };

    static getLegacyTouchState(state) {
        return state === this.TOUCH_STATES.draw || state === this.TOUCH_STATES.true;
    }

    static getTouchState(state) {
        switch (state) {
            case this.TOUCH_STATES.true:
                return this.TOUCH_STATES.draw;
            case this.TOUCH_STATES.false:
                return this.TOUCH_STATES.none;
            default:
                return state;
        }
    }

    static getDerivedStateFromProps(nextProps, prevState) {
        return {
            touchEnabled: TouchableSketchCanvas.getTouchState(nextProps.touchEnabled)
        };
    }

    constructor(props) {
        super(props);

        this.refHandler = this.refHandler.bind(this);

        this.state = {
            touchEnabled: TouchableSketchCanvas.TOUCH_STATES.draw
        };

    }


    refHandler(ref) {
        this.sketchCanvasInstance = ref;
        this.props.forwardedRef(ref);
    }

    render() {
        const { touchableComponent, contentContainerStyle } = this.props;

        return (
            <View style={contentContainerStyle} pointerEvents='box-none'>
                <SketchCanvas
                    {...this.props}
                    ref={this.refHandler}
                    touchEnabled={TouchableSketchCanvas.getLegacyTouchState(this.state.touchEnabled)} />
                {this.state.touchEnabled === TouchableSketchCanvas.TOUCH_STATES.touch && touchableComponent &&
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