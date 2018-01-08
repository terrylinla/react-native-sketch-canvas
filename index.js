import React from 'react'
import PropTypes from 'prop-types'
import ReactNative, {
  View,
  Text,
  TouchableOpacity,
  FlatList,
} from 'react-native'
import SketchCanvas from './src/SketchCanvas'

export default class extends React.Component {
  static propTypes = {
    containerStyle: View.propTypes.style,
    canvasStyle: View.propTypes.style,
    onStrokeStart: PropTypes.func,
    onStrokeChanged: PropTypes.func,
    onStrokeEnd: PropTypes.func,
    onClosePressed: PropTypes.func,
    onInfoPressed: PropTypes.func,
    onUndoPressed: PropTypes.func,
    onClearPressed: PropTypes.func,
    user: PropTypes.string,

    closeComponent: PropTypes.node,
    infoComponent: PropTypes.node,
    undoComponent: PropTypes.node,
    clearComponent: PropTypes.node,
    saveComponent: PropTypes.node,
    strokeComponent: PropTypes.func,
    strokeSelectedComponent: PropTypes.func,
    strokeWidthComponent: PropTypes.func,

    strokeColors: PropTypes.arrayOf(PropTypes.shape({ color: PropTypes.string})),
    defaultStrokeIndex: PropTypes.number,
    defaultStrokeWidth: PropTypes.number,

    minStrokeWidth: PropTypes.number,
    maxStrokeWidth: PropTypes.number,
    strokeWidthStep: PropTypes.number,

    savePreference: PropTypes.func,
    onSketchSaved: PropTypes.func,
  };

  static defaultProps = {
    containerStyle: null,
    canvasStyle: null,
    onStrokeStart: () => {},
    onStrokeChanged: () => {},
    onStrokeEnd: () => {},
    onClosePressed: () => {},
    onInfoPressed: () => {},
    onUndoPressed: () => {},
    onClearPressed: () => {},
    user: null,

    closeComponent: null,
    infoComponent: null,
    undoComponent: null,
    clearComponent: null,
    saveComponent: null,
    strokeComponent: null,
    strokeSelectedComponent: null,
    strokeWidthComponent: null,

    strokeColors: [
      { color: '#000000' }, 
      { color: '#FF0000' }, 
      { color: '#00FFFF' }, 
      { color: '#0000FF' }, 
      { color: '#0000A0' }, 
      { color: '#ADD8E6' }, 
      { color: '#800080' },
      { color: '#FFFF00' },
      { color: '#00FF00' },
      { color: '#FF00FF' },
      { color: '#FFFFFF' }, 
      { color: '#C0C0C0' }, 
      { color: '#808080' }, 
      { color: '#FFA500' }, 
      { color: '#A52A2A' },
      { color: '#800000' },
      { color: '#008000' },
      { color: '#808000' } ],
    defaultStrokeIndex: 0,
    defaultStrokeWidth: 3,

    minStrokeWidth: 3,
    maxStrokeWidth: 15,
    strokeWidthStep: 3,

    savePreference: null,
    onSketchSaved: () => {},
  };


  constructor(props) {
    super(props)

    this.state = {
      color: props.strokeColors[props.defaultStrokeIndex],
      strokeWidth: props.defaultStrokeWidth,
    }

    this._colorChanged = false
    this._strokeWidthStep = props.strokeWidthStep
  }

  clear() {
    this._sketchCanvas.clear()
  }

  undo() {
    return this._sketchCanvas.undo()
  }

  addPath(data) {
    this._sketchCanvas.addPath(data)
  }

  deletePath(id) {
    this._sketchCanvas.deletePath(id)
  }

  save() {
    if (this.props.savePreference) {
      const p = this.props.savePreference()
      this._sketchCanvas.save(p.imageType, p.transparent, p.folder ? p.folder : '', p.filename)
    } else {
      const date = new Date()
      this._sketchCanvas.save('png', '', false, date.getFullYear() + '-' + (date.getMonth() + 1) + '-' + ('0' + date.getDate()).slice(-2) + ' ' + ('0' + date.getHours()).slice(-2) + '-' + ('0' + date.getMinutes()).slice(-2) + '-' + ('0' + date.getSeconds()).slice(-2))
    }
  }

  nextStrokeWidth() {
    if ((this.state.strokeWidth >= this.props.maxStrokeWidth && this._strokeWidthStep > 0) || 
        (this.state.strokeWidth <= this.props.minStrokeWidth && this._strokeWidthStep < 0))
      this._strokeWidthStep = -this._strokeWidthStep
    this.setState({ strokeWidth: this.state.strokeWidth + this._strokeWidthStep })
  }

  _renderItem = ({item, index}) => (
    <TouchableOpacity style={{ marginHorizontal: 2.5 }} onPress={() => {
      this.setState({ color: item })
      this._colorChanged = true
    }}>
      { this.state.color.color !== item.color && this.props.strokeComponent && this.props.strokeComponent(item.color) }
      { this.state.color.color === item.color && this.props.strokeSelectedComponent && this.props.strokeSelectedComponent(item.color, index, this._colorChanged) }
    </TouchableOpacity>
  )

  componentDidUpdate() {
    this._colorChanged = false
  }

  render() {
    return (
      <View style={this.props.containerStyle}>
        <View style={{ flexDirection: 'row' }}>
          <View style={{ flexDirection: 'row', flex: 1, justifyContent: 'flex-start' }}>
            { this.props.closeComponent && (
              <TouchableOpacity onPress={() => { this.props.onClosePressed() }}>
                { this.props.closeComponent }
              </TouchableOpacity>)
            }

            { this.props.infoComponent && (
              <TouchableOpacity onPress={() => { this.props.onInfoPressed() }}>
                { this.props.infoComponent }
              </TouchableOpacity>)
            }
          </View>
          <View style={{ flexDirection: 'row', flex: 1, justifyContent: 'flex-end' }}>
            { this.props.strokeWidthComponent && (
              <TouchableOpacity onPress={() => { this.nextStrokeWidth() }}>
                {this.props.strokeWidthComponent(this.state.strokeWidth)}
              </TouchableOpacity>)
            }
              
            { this.props.undoComponent && (
              <TouchableOpacity onPress={() => {this.props.onUndoPressed(this.undo())}}>
                { this.props.undoComponent }
              </TouchableOpacity>)
            }

            { this.props.clearComponent && (
              <TouchableOpacity onPress={() => { this.clear(); this.props.onClearPressed() }}>
                { this.props.clearComponent }
              </TouchableOpacity>)
            }

            { this.props.saveComponent && (
              <TouchableOpacity onPress={() => { this.save() }}>
                { this.props.saveComponent }
              </TouchableOpacity>)
            }
          </View>
        </View>
        <SketchCanvas
          ref={ref => this._sketchCanvas = ref}
          style={this.props.canvasStyle} 
          strokeColor={this.state.color.color}
          onStrokeStart={this.props.onStrokeStart}
          onStrokeChanged={this.props.onStrokeChanged}
          onStrokeEnd={this.props.onStrokeEnd}
          user={this.props.user}
          strokeWidth={this.state.strokeWidth}
          onSketchSaved={success => this.props.onSketchSaved(success)}
        />
        <View style={{ flexDirection: 'row' }}>
          <FlatList
            data={this.props.strokeColors}
            extraData={this.state.color}
            keyExtractor={() => Math.ceil(Math.random() * 10000000)}
            renderItem={this._renderItem}
            horizontal
            showsHorizontalScrollIndicator={false}
          />
        </View>
      </View>
    );
  }
};

export {
  SketchCanvas
}