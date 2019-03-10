import React, { Component } from "react";
import { StyleSheet, View, ViewPropTypes } from "react-native";
import PropTypes from "prop-types";
import SketchCanvas from "./SketchCanvas";

type Props = {};
export default class ScratchCard extends Component<Props> {
  static propTypes = {
    style: ViewPropTypes.style,
    strokeColor: PropTypes.string,
    fillColor: PropTypes.string,
    strokeWidth: PropTypes.number,
    bgImage: PropTypes.shape({
      filename: PropTypes.string,
      directory: PropTypes.string,
      mode: PropTypes.oneOf(["AspectFill", "AspectFit", "ScaleToFill"])
    })
  };

  render() {
    return (
      <View style={styles.container}>
        <View style={{ flex: 1, flexDirection: "row" }}>
          <SketchCanvas
            localSourceImage={this.props.bgImage}
            style={{ flex: 1 }}
            strokeColor={this.props.strokeColor}
            fillColor={this.props.fillColor}
            strokeWidth={this.props.strokeWidth}
          />
        </View>
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: "center",
    alignItems: "center",
    backgroundColor: "#F5FCFF"
  }
});
