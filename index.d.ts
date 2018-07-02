import * as React from 'react'
import { ViewProperties } from "react-native";

export interface SketchCanvasProps {
  strokeColor: string
  strokeWidth: number
  localSourceImagePath: string
  onStrokeStart(): void
  onStrokeChanged(): void
  onStrokeEnd(): void
  onSketchSaved(result: boolean, path: string): void
  onPathsChange(pathsCount: any): void
}

export class SketchCanvas extends React.Component<SketchCanvasProps & ViewProperties> {

}

export default class RNSketchCanvas extends React.Component<any> {
  static constants: any;
}

