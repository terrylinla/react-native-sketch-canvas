/**
 * Sample React Native App
 * https://github.com/facebook/react-native
 * @flow
 */

import React, { Component } from 'react';
import {
  AppRegistry,
  StyleSheet,
  Text,
  View,
  Alert,
  TouchableOpacity,
} from 'react-native';

import RNSketchCanvas from '@terrylinla/react-native-sketch-canvas';
import { SketchCanvas } from '@terrylinla/react-native-sketch-canvas';

export default class example extends Component {
  constructor(props) {
    super(props)

    this.state = {
      example: 0,
      color: '#FF0000',
      thickness: 5,
      message: ''
    }
  }

  render() {
    return (
      <View style={styles.container}>
        {
          this.state.example === 0 &&
          <View style={{ justifyContent: 'center', alignItems: 'center', width: 340 }}>
            <TouchableOpacity onPress={() => {
              this.setState({ example: 1 })
            }}>
              <Text style={{ alignSelf: 'center', marginTop: 15, fontSize: 18 }}>- Example 1 -</Text>
              <Text>Use build-in UI components</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => {
              this.setState({ example: 2 })
            }}>
              <Text style={{ alignSelf: 'center', marginTop: 15, fontSize: 18 }}>- Example 2 -</Text>
              <Text>Use canvas only and customize UI components</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => {
              this.setState({ example: 3 })
            }}>
              <Text style={{ alignSelf: 'center', marginTop: 15, fontSize: 18 }}>- Example 3 -</Text>
              <Text>Sync two canvases</Text>
              <Text>(but support only one canvas per iOS device)</Text>
            </TouchableOpacity>
          </View>
        }

        {
          this.state.example === 1 &&
          <View style={{ flex: 1, flexDirection: 'row' }}>
            <RNSketchCanvas
              containerStyle={{ backgroundColor: 'transparent', flex: 1 }}
              canvasStyle={{ backgroundColor: 'transparent', flex: 1 }}
              onStrokeEnd={data => {
              }}
              closeComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Close</Text></View>}
              onClosePressed={() => {
                this.setState({ example: 0 })
              }}
              undoComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Undo</Text></View>}
              onUndoPressed={(id) => {
                // Alert.alert('do something')
              }}
              clearComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Clear</Text></View>}
              onClearPressed={() => {
                // Alert.alert('do something')
              }}
              infoComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Info</Text></View>}
              onInfoPressed={() => {
                // Alert.alert('some info')
              }}
              strokeComponent={color => (
                <View style={[{ backgroundColor: color }, styles.strokeColorButton]} />
              )}
              strokeSelectedComponent={(color, index, changed) => {
                return (
                  <View style={[{ backgroundColor: color, borderWidth: 2 }, styles.strokeColorButton]} />
                )
              }}
              strokeWidthComponent={(w) => {
                return (<View style={styles.strokeWidthButton}>
                  <View  style={{
                  backgroundColor: 'white', marginHorizontal: 2.5,
                  width: Math.sqrt(w / 3) * 10, height: Math.sqrt(w / 3) * 10, borderRadius: Math.sqrt(w / 3) * 10 / 2
                  }} />
                </View>
              )}}
              defaultStrokeIndex={0}
              defaultStrokeWidth={5}
              saveComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Save</Text></View>}
              savePreference={() => {
                return {
                  folder: 'RNSketchCanvas',
                  filename: String(Math.ceil(Math.random() * 100000000)),
                  transparent: false,
                  imageType: 'png'
                }
              }}
              onSketchSaved={success => {
                Alert.alert('Image saved!')
                // Alert.alert(String(success))
              }}
            />
          </View>
        }

        {
          this.state.example === 2 &&
          <View style={{ flex: 1, flexDirection: 'row' }}>
            <View style={{ flex: 1, flexDirection: 'column' }}>
              <View style={{ flexDirection: 'row', justifyContent: 'space-between' }}>
                <TouchableOpacity style={styles.functionButton} onPress={() => {
                  this.setState({ example: 0 })
                }}>
                  <Text style={{color: 'white'}}>Close</Text>
                </TouchableOpacity>
                <View style={{flexDirection: 'row'}}>
                  <TouchableOpacity style={styles.functionButton} onPress={() => {
                    this.setState({ thickness: 10 })
                  }}>
                    <Text style={{color: 'white'}}>Thick</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.functionButton} onPress={() => {
                    this.setState({ thickness: 5 })
                  }}>
                    <Text style={{color: 'white'}}>Thin</Text>
                  </TouchableOpacity>
                </View>
              </View>
              <SketchCanvas
                ref={ref => this.canvas=ref}
                style={{ flex: 1 }}
                strokeColor={this.state.color}
                strokeWidth={this.state.thickness}
                onStrokeStart={() => {
                  this.setState({ message: 'Start' })
                }}
                onStrokeChanged={() => {
                  this.setState({ message: 'Changed' })
                }}
                onStrokeEnd={() => {
                  this.setState({ message: 'End' })
                }}
                onBase64={(base64) => {
                  console.log(base64) // For Android
                }}
              />
              <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                <View style={{flexDirection: 'row'}}>
                  <TouchableOpacity style={[styles.functionButton, {backgroundColor: 'red'}]} onPress={() => {
                    this.setState({ color: '#FF0000' })
                  }}>
                    <Text style={{color: 'white'}}>Red</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={[styles.functionButton, {backgroundColor: 'black'}]} onPress={() => {
                    this.setState({ color: '#000000' })
                  }}>
                    <Text style={{color: 'white'}}>Black</Text>
                  </TouchableOpacity>
                </View>
                <Text style={{marginRight: 8, fontSize: 20}}>{ this.state.message }</Text>
                <TouchableOpacity style={[styles.functionButton, {backgroundColor: 'black', width: 90}]} onPress={() => {
                  console.log(this.canvas.getPaths())
                  Alert.alert(JSON.stringify(this.canvas.getPaths()))
                  this.canvas.getBase64('jpg', false, (err, result) => {
                    console.log(result) // For iOS
                  })
                }}>
                  <Text style={{color: 'white'}}>Get Paths</Text>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        }

        {
          this.state.example === 3 &&
          <View style={{ flex: 1, flexDirection: 'column' }}>
            <RNSketchCanvas
              ref={ref => this.canvas1=ref}
              user={'user1'}
              containerStyle={{ backgroundColor: 'transparent', flex: 1 }}
              canvasStyle={{ backgroundColor: 'transparent', flex: 1 }}
              onStrokeEnd={data => {
              }}
              closeComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Close</Text></View>}
              onClosePressed={() => {
                this.setState({ example: 0 })
              }}
              undoComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Undo</Text></View>}
              onUndoPressed={(id) => {
                this.canvas2.deletePath(id)
              }}
              clearComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Clear</Text></View>}
              onClearPressed={() => {
                this.canvas2.clear()
              }}
              strokeComponent={color => (
                <View style={[{ backgroundColor: color }, styles.strokeColorButton]} />
              )}
              strokeSelectedComponent={(color, index, changed) => {
                return (
                  <View style={[{ backgroundColor: color, borderWidth: 2 }, styles.strokeColorButton]} />
                )
              }}
              strokeWidthComponent={(w) => {
                return (<View style={styles.strokeWidthButton}>
                  <View  style={{
                  backgroundColor: 'white', marginHorizontal: 2.5,
                  width: Math.sqrt(w / 3) * 10, height: Math.sqrt(w / 3) * 10, borderRadius: Math.sqrt(w / 3) * 10 / 2
                  }} />
                </View>
              )}}
              defaultStrokeIndex={0}
              defaultStrokeWidth={5}
              saveComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Save</Text></View>}
              savePreference={() => {
                return {
                  folder: 'RNSketchCanvas',
                  filename: String(Math.ceil(Math.random() * 100000000)),
                  transparent: true,
                  imageType: 'jpg'
                }
              }}
              onSketchSaved={success => {
                Alert.alert(String(success))
              }}
              onStrokeEnd={(path) => {
                this.canvas2.addPath(path)
              }}
            />
            <RNSketchCanvas
              ref={ref => this.canvas2=ref}
              user={'user2'}
              containerStyle={{ backgroundColor: 'transparent', flex: 1 }}
              canvasStyle={{ backgroundColor: 'transparent', flex: 1 }}
              onStrokeEnd={data => {
              }}
              undoComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Undo</Text></View>}
              onUndoPressed={(id) => {
                this.canvas1.deletePath(id)
              }}
              clearComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Clear</Text></View>}
              onClearPressed={() => {
                this.canvas1.clear()
              }}
              strokeComponent={color => (
                <View style={[{ backgroundColor: color }, styles.strokeColorButton]} />
              )}
              strokeSelectedComponent={(color, index, changed) => {
                return (
                  <View style={[{ backgroundColor: color, borderWidth: 2 }, styles.strokeColorButton]} />
                )
              }}
              strokeWidthComponent={(w) => {
                return (<View style={styles.strokeWidthButton}>
                  <View  style={{
                  backgroundColor: 'white', marginHorizontal: 2.5,
                  width: Math.sqrt(w / 3) * 10, height: Math.sqrt(w / 3) * 10, borderRadius: Math.sqrt(w / 3) * 10 / 2
                  }} />
                </View>
              )}}
              defaultStrokeIndex={0}
              defaultStrokeWidth={5}
              saveComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Save</Text></View>}
              savePreference={() => {
                return {
                  folder: 'RNSketchCanvas',
                  filename: String(Math.ceil(Math.random() * 100000000)),
                  transparent: true,
                  imageType: 'jpg'
                }
              }}
              onSketchSaved={success => {
                Alert.alert(String(success))
              }}
              onStrokeEnd={(path) => {
                this.canvas1.addPath(path)
              }}
            />
          </View>
        }
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#F5FCFF',
  },
  strokeColorButton: {
    marginHorizontal: 2.5,
    marginVertical: 8,
    width: 30,
    height: 30,
    borderRadius: 15,
  },
  strokeWidthButton: {
    marginHorizontal: 2.5,
    marginVertical: 8,
    width: 30,
    height: 30,
    borderRadius: 15,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#39579A'
  },
  functionButton: {
    marginHorizontal: 2.5,
    marginVertical: 8,
    height: 30,
    width: 60,
    backgroundColor: '#39579A',
    justifyContent: 'center',
    alignItems: 'center',
    borderRadius: 5,
  }
});

AppRegistry.registerComponent('example', () => example);
