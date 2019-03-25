'use strict';

import React from 'react';
import { View, ActivityIndicator, Text, Image,   ViewPropTypes, Alert } from 'react-native';
import PropTypes from 'prop-types'
import SketchCanvas from './src/SketchCanvas';
import ResponsiveView from './src/ResponsiveView';

class ResponsiveSketchCanvas extends React.Component {
	static defaultProps = {
		maxZoom: 1.5,
		minZoom: 0.2,
		scrollEnabled: true,
		startToDrawDelay: 75,
		activityIndicator: null,
		strokeWidth: 7,
		strokeColor: 'red',

		//RNSketchCanvas
		style: null,
		onPathsChange: () => { },
		onStrokeStart: () => { },
		onStrokeChanged: () => { },
		onStrokeEnd: () => { },
		onSketchSaved: () => { },
		user: null,
		requiredTouches: null,
		touchEnabled: true,
		text: null,
		localSourceImage: null,
		permissionDialogTitle: '',
		permissionDialogMessage: '',
	};

	static propTypes = {
		maxZoom: PropTypes.number,
		minZoom: PropTypes.number,
		scrollEnabled: PropTypes.bool,
		activityIndicator: PropTypes.func,
		requiredTouches: PropTypes.number,
		startToDrawDelay: PropTypes.number,

		//RNSketchCanvas PropTypes
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

	constructor(props) {
		super(props);
		if(props.contentStyle == null && props.localSourceImage && props.localSourceImage.filename) {
			this.getBackgroundImageSize(props.localSourceImage.filename);
		} else {
			console.warn('did not try to get image ', props);
		}
		this.state = {
			contentStyle: props.contentStyle
		};
	}

	componentWillReceiveProps(nextProps) {
		if(nextProps.contentStyle != null){
			this.setState({ contentStyle: nextProps.contentStyle });
		}
		
	  if(nextProps.contentStyle == null && nextProps.localSourceImage !== this.props.localSourceImage
         && nextProps.localSourceImage.filename
				 && (!this.props.localSourceImage || (nextProps.localSourceImage.filename !== this.props.localSourceImage.filename))){
    	this.getBackgroundImageSize(nextProps.localSourceImage.filename);
    }
  }

	getBackgroundImageSize(path) {
		Image.getSize(path, (width, height) => {
			this.setState({
				contentStyle: {
					height,
					width,
				},
				initialStyle: {
					height,
					width,
				},
			});
		});
	}

	renderActivityIndicator() {
		return (
			<View style={{ flex: 1, alignSelf: 'center', justifyContent: 'center' }}>
				<ActivityIndicator size={'large'} />
			</View>
		);
	}

	updateZoomLevel(zoom) {
		this.setState({ zoom });
	}

	render() {
		if(this.state.contentStyle){
			const { maxZoom, minZoom, scrollEnabled, ...sketchProps } = this.props;
			return (
				<ResponsiveView
					centerContent
					contentContainerStyle={[styles.scrollViewContainer,]}
					maxZoomScale={maxZoom}
					minZoomScale={minZoom}
					scrollEnabled={scrollEnabled}
					initialStyle={this.state.initialStyle ? this.state.initialStyle : this.state.contentStyle}
					updateZoomLevel={this.updateZoomLevel.bind(this)}

				>
					<SketchCanvas
						{...sketchProps}
						ref={ref => this.canvas = ref}
						style={[styles.sketch,this.props.style]}
						scale={this.state.zoom}
						requiredTouches={1}
					/>
				</ResponsiveView>
			);
		}
		return this.renderActivityIndicator();
	}
}

const styles = {
	sketch: {
		flex: 1,
		left: 0,
		right: 0,
		top: 0,
		bottom: 0,
		borderWidth: 1,
	},
	scrollViewContainer: {
		flexGrow: 1,
		justifyContent: 'center',
	},
}

export { ResponsiveSketchCanvas };
