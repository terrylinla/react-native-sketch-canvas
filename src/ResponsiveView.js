import React, { Component } from 'react';
import { Animated, Dimensions, Easing, PanResponder, StyleSheet, View, Platform } from 'react-native';

export default class ResponsiveView extends Component {
	elasticity = 0.8;
	padding = 0;

	constructor(props, b) {
		super(props, b);
		this.state = {
			contentStyle: props.contentContainerStyle,
			oldTouch: null,
			initialDistance: null,
		};
		this.dual = new Animated.ValueXY({ x: 0, y: 0 });
		const initialZoom = this.calcInitialZoom(props.initialStyle);
		this.zoom = new Animated.Value(initialZoom);
		props.updateZoomLevel(initialZoom);
		this.$scrollerXBound = props.initialStyle.width / 2 + this.padding;
		this.$scrollerYBound = props.initialStyle.height / 2 + this.padding;
	}

	calcDistance(x1, y1, x2, y2) {
		const dx = Math.abs(x1 - x2);
		const dy = Math.abs(y1 - y2);
		return Math.sqrt(Math.pow(dx, 2) + Math.pow(dy, 2));
	}

	calcInitialZoom(initialStyle) {
		const zoomW = screenWidth / initialStyle.width;
		const zoomH = screenHeight / initialStyle.height;
		return zoomH < zoomW ? zoomH : zoomW;
	}

	componentDidMount() {
		this.$outboundX = this.$outboundY = false;
	}

	componentDidUpdate() {

	}

	componentWillMount() {
		const requiredTouches = 2;
		this.panGesture = PanResponder.create({
			onStartShouldSetPanResponder: (evt, gestureState) => gestureState.numberActiveTouches === requiredTouches,
			onStartShouldSetPanResponderCapture: (evt, gestureState) => gestureState.numberActiveTouches === requiredTouches,
			onMoveShouldSetPanResponder: (evt, gestureState) => gestureState.numberActiveTouches === requiredTouches,
			onMoveShouldSetPanResponderCapture: (evt, gestureState) => gestureState.numberActiveTouches === requiredTouches,

			onPanResponderGrant: (e, gestureState) => {
				this.$lastPt = { x: gestureState.x0, y: gestureState.y0 };
				return gestureState.numberActiveTouches === requiredTouches;
			},

			onPanResponderMove: (e, gestureState) => {
				const info = JSON.parse(JSON.stringify(gestureState));

				if (e.nativeEvent?.changedTouches?.length === 2) {
					const touch1 = e.nativeEvent?.changedTouches[0];
					const touch2 = e.nativeEvent?.changedTouches[1];

					const distance = this.calcDistance(touch1.locationX, touch1.locationY, touch2.locationX, touch2.locationY);
					if (this.state.initialDistance) {
						const deltaZoom = (distance / this.state.initialDistance - 1) / 2;
						if (Math.abs(deltaZoom) > 0.1) {
							let zoom = this.zoom._value + deltaZoom;
							if (zoom > this.props.maxZoomScale) {
								zoom = this.props.maxZoomScale;
							} else if (zoom < this.props.minZoomScale) {
								zoom = this.props.minZoomScale;
							}
							Animated.timing(this.zoom,
								{
									toValue: zoom,
									duration: 200,
									easing: Easing.out(Easing.ease),
								}).start();
							this.props.updateZoomLevel(zoom);
						}
					} else {
						this.setState({ initialDistance: distance });
					}
				}
				let mx = gestureState.moveX - this.$lastPt.x;
				let my = gestureState.moveY - this.$lastPt.y;

				if (this.dual.x._value < -this.$scrollerXBound * this.zoom) {
					mx *= (1 - this.elasticity);
					this.$outboundX = true;
				}

				if (this.dual.x._value > this.$scrollerXBound * this.zoom) {
					if (gestureState.vx < 0) {
						mx *= (1 - this.elasticity);
					}
					this.$outboundX = true;
				}

				if (this.dual.y._value < -this.$scrollerYBound * this.zoom) {
					my *= (1 - this.elasticity);
					this.$outboundY = true;
				}

				if (this.dual.y._value > this.$scrollerYBound * this.zoom) {
					// console.log( ' y 向上出界: ', JSON.parse(JSON.stringify(gestureState)) );
					if (gestureState.vy < 0) {
						my *= (1 - this.elasticity);
					}
					this.$outboundY = true;
				}

				if (my > 40) my = 10;
				if (my < -40) my = -10;

				this.dual.setValue({
					x: this.dual.x._value + mx,
					y: this.dual.y._value + my,
				});

				this.$lastPt = { x: gestureState.moveX, y: gestureState.moveY };
			},

			onPanResponderRelease: (e, gestureState) => {
				if (this.$outboundX || this.$outboundY) {
					this.checkBounds(gestureState);
				} else if (Math.abs(gestureState.vx) > 0.03 || Math.abs(gestureState.vy) > 0.03) {

					// cap the velocity
					const vx = Math.max(-0.5, Math.min(0.5, gestureState.vx));
					const vy = Math.max(-0.5, Math.min(0.5, gestureState.vy));

					Animated.decay(
						this.dual,
						{
							toValue: { x: 44, y: 44 },
							velocity: { x: vx, y: vy },
							deceleration: 0.996,
						},
					).start(() => this.checkBounds(gestureState));
				}
				this.setState({ initialDistance: null, oldTouch: null });
				this.$outboundX = this.$outboundY = false;
			},
		});
	}

	checkBounds(gestureState) {
		const obj = { x: this.dual.x._value, y: this.dual.y._value };
		if (this.dual.x._value < -this.$scrollerXBound * this.zoom) obj.x = -this.$scrollerXBound * this.zoom;

		if (this.dual.x._value > this.$scrollerXBound * this.zoom) obj.x = this.$scrollerXBound * this.zoom;

		if (this.dual.y._value < -this.$scrollerYBound * this.zoom) obj.y = -this.$scrollerYBound * this.zoom;

		if (this.dual.y._value > this.$scrollerYBound * this.zoom) obj.y = this.$scrollerYBound * this.zoom;

		Animated.timing(
			this.dual,
			{
				toValue: { x: obj.x, y: obj.y },
				duration: 200,
				easing: Easing.out(Easing.ease),
			},
		).start();
	}

	render() {
		return (
			<View
				style={styles.viewport}
				{...this.panGesture.panHandlers}
			>
				<Animated.View style={[this.props.initialStyle, this.dual.getLayout(), { transform: [{ scaleX: this.zoom }, { scaleY: this.zoom }] }]} ref='contentPane'>
					{this.props.children}
				</Animated.View>
			</View>
		);
	}
}

ResponsiveView.defaultProps = {
	maxZoomScale: 2,
	minZoomScale: 0.2,
};

const { height: screenHeight, width: screenWidth } = Dimensions.get('window');
const styles = StyleSheet.create({

	viewport: {
		flex: 1,
		backgroundColor: 'transparent',
		borderWidth: 0,
		overflow: 'hidden',
		justifyContent: 'center',
		alignItems: 'center',
	},
	contentPane: {
	},

});
