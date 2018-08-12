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
  ScrollView,
  Platform
} from 'react-native';
import { RNCamera } from 'react-native-camera';

import RNSketchCanvas from '@terrylinla/react-native-sketch-canvas';
import { SketchCanvas } from '@terrylinla/react-native-sketch-canvas';

export default class example extends Component {
  constructor(props) {
    super(props)

    this.state = {
      example: 0,
      color: '#FF0000',
      thickness: 5,
      message: '',
      photoPath: null,
      scrollEnabled: true
    }
  }

  takePicture = async function () {
    if (this.camera) {
      const options = { quality: 0.5, base64: true };
      const data = await this.camera.takePictureAsync(options)
      this.setState({
        photoPath: data.uri.replace('file://', '')
      })
    }
  };

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
            </TouchableOpacity>
            <TouchableOpacity onPress={() => {
              this.setState({ example: 4 })
            }}>
              <Text style={{ alignSelf: 'center', marginTop: 15, fontSize: 18 }}>- Example 4 -</Text>
              <Text>Take a photo first</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => {
              this.setState({ example: 5 })
            }}>
              <Text style={{ alignSelf: 'center', marginTop: 15, fontSize: 18 }}>- Example 5 -</Text>
              <Text>Load local image</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => {
              this.setState({ example: 6 })
            }}>
              <Text style={{ alignSelf: 'center', marginTop: 15, fontSize: 18 }}>- Example 6 -</Text>
              <Text>Draw text on canvas</Text>
            </TouchableOpacity>
            <TouchableOpacity onPress={() => {
              this.setState({ example: 7 })
            }}>
              <Text style={{ alignSelf: 'center', marginTop: 15, fontSize: 18 }}>- Example 7 -</Text>
              <Text>Multiple canvases in ScrollView</Text>
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
              closeComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Close</Text></View>}
              onClosePressed={() => {
                this.setState({ example: 0 })
              }}
              undoComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Undo</Text></View>}
              onUndoPressed={(id) => {
                // Alert.alert('do something')
              }}
              clearComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Clear</Text></View>}
              onClearPressed={() => {
                // Alert.alert('do something')
              }}
              eraseComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Eraser</Text></View>}
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
                  <View style={{
                    backgroundColor: 'white', marginHorizontal: 2.5,
                    width: Math.sqrt(w / 3) * 10, height: Math.sqrt(w / 3) * 10, borderRadius: Math.sqrt(w / 3) * 10 / 2
                  }} />
                </View>
                )
              }}
              defaultStrokeIndex={0}
              defaultStrokeWidth={5}
              saveComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Save</Text></View>}
              savePreference={() => {
                return {
                  folder: "RNSketchCanvas",
                  filename: String(Math.ceil(Math.random() * 100000000)),
                  transparent: false,
                  imageType: "png"
                }
              }}
              onSketchSaved={(success, path) => {
                Alert.alert(success ? 'Image saved!' : 'Failed to save image!', path)
              }}
              onPathsChange={(pathsCount) => {
                console.log('pathsCount', pathsCount)
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
                  <Text style={{ color: 'white' }}>Close</Text>
                </TouchableOpacity>
                <View style={{ flexDirection: 'row' }}>
                  <TouchableOpacity style={styles.functionButton} onPress={() => {
                    this.setState({ thickness: 10 })
                  }}>
                    <Text style={{ color: 'white' }}>Thick</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={styles.functionButton} onPress={() => {
                    this.setState({ thickness: 5 })
                  }}>
                    <Text style={{ color: 'white' }}>Thin</Text>
                  </TouchableOpacity>
                </View>
              </View>
              <SketchCanvas
                localSourceImage={{ filename: 'whale.png', directory: SketchCanvas.MAIN_BUNDLE, mode: 'AspectFit' }}
                // localSourceImage={{ filename: 'bulb.png', directory: RNSketchCanvas.MAIN_BUNDLE }}
                ref={ref => this.canvas = ref}
                style={{ flex: 1 }}
                strokeColor={this.state.color}
                strokeWidth={this.state.thickness}
                onStrokeStart={(x, y) => {
                  console.log('x: ', x, ', y: ', y)
                  this.setState({ message: 'Start' })
                }}
                onStrokeChanged={(x, y) => {
                  console.log('x: ', x, ', y: ', y)
                  this.setState({ message: 'Changed' })
                }}
                onStrokeEnd={() => {
                  this.setState({ message: 'End' })
                }}
                onPathsChange={(pathsCount) => {
                  console.log('pathsCount', pathsCount)
                }}
              />
              <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center' }}>
                <View style={{ flexDirection: 'row' }}>
                  <TouchableOpacity style={[styles.functionButton, { backgroundColor: 'red' }]} onPress={() => {
                    this.setState({ color: '#FF0000' })
                  }}>
                    <Text style={{ color: 'white' }}>Red</Text>
                  </TouchableOpacity>
                  <TouchableOpacity style={[styles.functionButton, { backgroundColor: 'black' }]} onPress={() => {
                    this.setState({ color: '#000000' })
                  }}>
                    <Text style={{ color: 'white' }}>Black</Text>
                  </TouchableOpacity>
                </View>
                <Text style={{ marginRight: 8, fontSize: 20 }}>{this.state.message}</Text>
                <TouchableOpacity style={[styles.functionButton, { backgroundColor: 'black', width: 90 }]} onPress={() => {
                  console.log(this.canvas.getPaths())
                  Alert.alert(JSON.stringify(this.canvas.getPaths()))
                  this.canvas.getBase64('jpg', false, true, true, (err, result) => {
                    console.log(result)
                  })
                }}>
                  <Text style={{ color: 'white' }}>Get Paths</Text>
                </TouchableOpacity>
              </View>
            </View>
          </View>
        }

        {
          this.state.example === 3 &&
          <View style={{ flex: 1, flexDirection: 'column' }}>
            <RNSketchCanvas
              ref={ref => this.canvas1 = ref}
              user={'user1'}
              containerStyle={{ backgroundColor: 'transparent', flex: 1 }}
              canvasStyle={{ backgroundColor: 'transparent', flex: 1 }}
              onStrokeEnd={data => {
              }}
              closeComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Close</Text></View>}
              onClosePressed={() => {
                this.setState({ example: 0 })
              }}
              undoComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Undo</Text></View>}
              onUndoPressed={(id) => {
                this.canvas2.deletePath(id)
              }}
              clearComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Clear</Text></View>}
              onClearPressed={() => {
                this.canvas2.clear()
              }}
              eraseComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Eraser</Text></View>}
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
                  <View style={{
                    backgroundColor: 'white', marginHorizontal: 2.5,
                    width: Math.sqrt(w / 3) * 10, height: Math.sqrt(w / 3) * 10, borderRadius: Math.sqrt(w / 3) * 10 / 2
                  }} />
                </View>
                )
              }}
              defaultStrokeIndex={0}
              defaultStrokeWidth={5}
              saveComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Save</Text></View>}
              savePreference={() => {
                return {
                  folder: 'RNSketchCanvas',
                  filename: String(Math.ceil(Math.random() * 100000000)),
                  transparent: true,
                  imageType: 'jpg'
                }
              }}
              onSketchSaved={(success, path) => {
                Alert.alert(success ? 'Image saved!' : 'Failed to save image!', path)
              }}
              onStrokeEnd={(path) => {
                this.canvas2.addPath(path)
              }}
              onPathsChange={(pathsCount) => {
                console.log('pathsCount(user1)', pathsCount)
              }}
            />
            <RNSketchCanvas
              ref={ref => this.canvas2 = ref}
              user={'user2'}
              containerStyle={{ backgroundColor: 'transparent', flex: 1 }}
              canvasStyle={{ backgroundColor: 'transparent', flex: 1 }}
              onStrokeEnd={data => {
              }}
              undoComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Undo</Text></View>}
              onUndoPressed={(id) => {
                this.canvas1.deletePath(id)
              }}
              clearComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Clear</Text></View>}
              onClearPressed={() => {
                this.canvas1.clear()
              }}
              eraseComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Eraser</Text></View>}
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
                  <View style={{
                    backgroundColor: 'white', marginHorizontal: 2.5,
                    width: Math.sqrt(w / 3) * 10, height: Math.sqrt(w / 3) * 10, borderRadius: Math.sqrt(w / 3) * 10 / 2
                  }} />
                </View>
                )
              }}
              defaultStrokeIndex={0}
              defaultStrokeWidth={5}
              saveComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Save</Text></View>}
              savePreference={() => {
                return {
                  folder: 'RNSketchCanvas',
                  filename: String(Math.ceil(Math.random() * 100000000)),
                  transparent: true,
                  imageType: 'jpg'
                }
              }}
              onSketchSaved={(success, path) => {
                Alert.alert(success ? 'Image saved!' : 'Failed to save image!', path)
              }}
              onStrokeEnd={(path) => {
                this.canvas1.addPath(path)
              }}
              onPathsChange={(pathsCount) => {
                console.log('pathsCount(user2)', pathsCount)
              }}
            />
          </View>
        }

        {
          this.state.example === 4 &&
          (this.state.photoPath === null ?
            <View style={styles.cameraContainer}>
              <RNCamera
                ref={ref => {
                  this.camera = ref;
                }}
                style={styles.preview}
                type={RNCamera.Constants.Type.back}
                flashMode={RNCamera.Constants.FlashMode.on}
                permissionDialogTitle={'Permission to use camera'}
                permissionDialogMessage={'We need your permission to use your camera phone'}
              />
              <View style={{ flex: 0, flexDirection: 'row', justifyContent: 'center', }}>
                <TouchableOpacity
                  onPress={this.takePicture.bind(this)}
                  style={styles.capture}
                >
                  <Text style={{ fontSize: 14 }}> SNAP </Text>
                </TouchableOpacity>
              </View>
            </View>
            :
            <View style={{ flex: 1, flexDirection: 'row' }}>
              <RNSketchCanvas
                localSourceImage={{ filename: this.state.photoPath, directory: null, mode: 'AspectFit' }}
                containerStyle={{ backgroundColor: 'transparent', flex: 1 }}
                canvasStyle={{ backgroundColor: 'transparent', flex: 1 }}
                onStrokeEnd={data => {
                }}
                closeComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Close</Text></View>}
                onClosePressed={() => {
                  this.setState({ example: 0 })
                }}
                undoComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Undo</Text></View>}
                onUndoPressed={(id) => {
                  // Alert.alert('do something')
                }}
                clearComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Clear</Text></View>}
                onClearPressed={() => {
                  // Alert.alert('do something')
                }}
                eraseComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Eraser</Text></View>}
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
                    <View style={{
                      backgroundColor: 'white', marginHorizontal: 2.5,
                      width: Math.sqrt(w / 3) * 10, height: Math.sqrt(w / 3) * 10, borderRadius: Math.sqrt(w / 3) * 10 / 2
                    }} />
                  </View>
                  )
                }}
                defaultStrokeIndex={0}
                defaultStrokeWidth={5}
                saveComponent={<View style={styles.functionButton}><Text style={{ color: 'white' }}>Save</Text></View>}
                savePreference={() => {
                  return {
                    folder: 'RNSketchCanvas',
                    filename: String(Math.ceil(Math.random() * 100000000)),
                    transparent: false,
                    imageType: 'png'
                  }
                }}
                onSketchSaved={(success, path) => {
                  Alert.alert(success ? 'Image saved!' : 'Failed to save image!', path)
                }}
                onPathsChange={(pathsCount) => {
                  console.log('pathsCount', pathsCount)
                }}
              />
            </View>)
        }

        {
          this.state.example === 5 &&
          <View style={{ flex: 1, flexDirection: 'row' }}>
            <RNSketchCanvas
              localSourceImage={{ filename: 'whale.png', directory: SketchCanvas.MAIN_BUNDLE, mode: 'AspectFit' }}
              // localSourceImage={{ filename: 'bulb.png', directory: RNSketchCanvas.MAIN_BUNDLE }}
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
              eraseComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Eraser</Text></View>}
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
                  includeImage: false,
                  cropToImageSize: false,
                  imageType: 'jpg'
                }
              }}
              onSketchSaved={(success, path) => {
                Alert.alert(success ? 'Image saved!' : 'Failed to save image!', path)
              }}
              onPathsChange={(pathsCount) => {
                console.log('pathsCount', pathsCount)
              }}
            />
          </View>
        }

        {
          this.state.example === 6 &&
          <View style={{ flex: 1, flexDirection: 'row' }}>
            <RNSketchCanvas
              text={[
                { text: 'Welcome to my GitHub', font: 'fonts/IndieFlower.ttf', fontSize: 30, position: { x: 0, y: 0 }, anchor: { x: 0, y: 0 }, coordinate: 'Absolute', fontColor: 'red' },
                { text: 'Center\nMULTILINE', fontSize: 25, position: { x: 0.5, y: 0.5 }, anchor: { x: 0.5, y: 0.5 }, coordinate: 'Ratio', overlay: 'SketchOnText', fontColor: 'black', alignment: 'Center', lineHeightMultiple: 1 },
                { text: 'Right\nMULTILINE', fontSize: 25, position: { x: 1, y: 0.25 }, anchor: { x: 1, y: 0.5 }, coordinate: 'Ratio', overlay: 'TextOnSketch', fontColor: 'black', alignment: 'Right', lineHeightMultiple: 1 },
                { text: 'Signature', font: 'Zapfino', fontSize: 40, position: { x: 0, y: 1 }, anchor: { x: 0, y: 1 }, coordinate: 'Ratio', overlay: 'TextOnSketch', fontColor: '#444444' }
              ]}
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
              eraseComponent={<View style={styles.functionButton}><Text style={{color: 'white'}}>Eraser</Text></View>}
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
                  includeImage: false,
                  includeText: false,
                  cropToImageSize: false,
                  imageType: 'jpg'
                }
              }}
              onSketchSaved={(success, path) => {
                Alert.alert(success ? 'Image saved!' : 'Failed to save image!', path)
              }}
              onPathsChange={(pathsCount) => {
                console.log('pathsCount', pathsCount)
              }}
            />
          </View>
        }

        {
          this.state.example === 7 &&
          <View style={{ flex: 1, flexDirection: 'row' }}>
            <ScrollView style={{ flex: 1 }} contentContainerStyle={{ padding: 36 }}
              scrollEnabled={this.state.scrollEnabled}
            >
              <TouchableOpacity onPress={() => this.setState({ example: 0 })}>
                <Text>Close</Text>
              </TouchableOpacity>
              <SketchCanvas
                text={[
                  { text: 'Page 1', position: { x: 20, y: 20 }, fontSize: Platform.select({ ios: 24, android: 48 }) },
                  { text: 'Signature', font: Platform.select({ ios: 'Zapfino', android: 'fonts/IndieFlower.ttf' }), position: { x: 20, y: 220 }, fontSize: Platform.select({ ios: 24, android: 48 }), fontColor: 'red' }
                ]}
                localSourceImage={{ filename: 'whale.png', directory: SketchCanvas.MAIN_BUNDLE, mode: 'AspectFit' }}
                style={styles.page}
                onStrokeStart={() => this.setState({ scrollEnabled: false })}
                onStrokeEnd={() => this.setState({ scrollEnabled: true })}
              />
              <SketchCanvas
                text={[{ text: 'Page 2', position: { x: 0.95, y: 0.05 }, anchor: { x: 1, y: 0 }, coordinate: 'Ratio', fontSize: Platform.select({ ios: 24, android: 48 }) }]}
                style={styles.page}
                onStrokeStart={() => this.setState({ scrollEnabled: false })}
                onStrokeEnd={() => this.setState({ scrollEnabled: true })}
              />
              <SketchCanvas
                text={[{ text: 'Page 3', position: { x: 0.5, y: 0.95 }, anchor: { x: 0.5, y: 1 }, coordinate: 'Ratio', fontSize: Platform.select({ ios: 24, android: 48 }) }]}
                style={styles.page}
                onStrokeStart={() => this.setState({ scrollEnabled: false })}
                onStrokeEnd={() => this.setState({ scrollEnabled: true })}
              />
              <SketchCanvas
                text={[{ text: 'Page 4', position: { x: 20, y: 20 }, fontSize: Platform.select({ ios: 24, android: 48 }) }]}
                style={styles.page}
                onStrokeStart={() => this.setState({ scrollEnabled: false })}
                onStrokeEnd={() => this.setState({ scrollEnabled: true })}
              />
            </ScrollView>
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
  },
  cameraContainer: {
    flex: 1,
    flexDirection: 'column',
    backgroundColor: 'black',
    alignSelf: 'stretch'
  },
  preview: {
    flex: 1,
    justifyContent: 'flex-end',
  },
  capture: {
    flex: 0,
    backgroundColor: '#fff',
    borderRadius: 5,
    padding: 15,
    paddingHorizontal: 20,
    alignSelf: 'center',
    margin: 20
  },
  page: {
    flex: 1,
    height: 300,
    elevation: 2,
    marginVertical: 8,
    backgroundColor: 'white',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.75,
    shadowRadius: 2
  }
});

AppRegistry.registerComponent('example', () => example);
