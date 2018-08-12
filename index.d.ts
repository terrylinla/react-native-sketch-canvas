import * as React from 'react'
import {
  ViewProperties,
  StyleProp,
  ViewStyle
} from "react-native"

type ImageType = 'png' | 'jpg'

type Size = {
  width: number
  height: number
}

type PathData = {
  id: number
  color: string
  width: number
  data: string[]
}

type Path = {
  drawer?: string
  size: Size
  path: PathData
}

type CanvasText = {
  text: string
  font?: string
  fontSize?: number
  fontColor?: string
  overlay?: 'TextOnSketch' | 'SketchOnText'
  anchor: { x: number, y: number }
  position: { x: number, y: number }
  coordinate?: 'Absolute' | 'Ratio'
  /**
   * If your text is multiline, `alignment` can align shorter lines with left/center/right.
   */
  alignment?: 'Left' | 'Center' | 'Right'
  /**
   * If your text is multiline, `lineHeightMultiple` can adjust the space between lines.
   */
  lineHeightMultiple?: number
}

export interface SavePreference {
  folder: string
  filename: string
  transparent: boolean
  imageType: ImageType
  includeImage?: boolean
  includeText?: boolean
  cropToImageSize?: boolean
}

export interface LocalSourceImage {
  path: string
  directory?: string
  mode?: 'AspectFill' | 'AspectFit' | 'ScaleToFill'
}

export interface SketchCanvasProps {
  style?: StyleProp<ViewStyle>
  strokeColor?: string
  strokeWidth?: number
  user?: string

  text?: CanvasText[]
  localSourceImage?: LocalSourceImage
  touchEnabled?: boolean

  /**
   * Android Only: Provide a Dialog Title for the Image Saving PermissionDialog. Defaults to empty string if not set
   */
  permissionDialogTitle?: string

  /**
   * Android Only: Provide a Dialog Message for the Image Saving PermissionDialog. Defaults to empty string if not set
   */
  permissionDialogMessage?: string

  onStrokeStart?: () => void
  onStrokeChanged?: () => void
  onStrokeEnd?: (path: Path) => void
  onSketchSaved?: (result: boolean, path: string) => void
  onPathsChange?: (pathsCount: number) => void
}

export class SketchCanvas extends React.Component<SketchCanvasProps & ViewProperties> {
  clear(): void
  undo(): number
  addPath(data: Path): void
  deletePath(id: number): void

  /**
   * @param imageType "png" or "jpg"
   * @param includeImage Set to `true` to include the image loaded from `LocalSourceImage`
   * @param includeText Set to `true` to include the text drawn from `Text`.
   * @param cropToImageSize Set to `true` to crop output image to the image loaded from `LocalSourceImage`
   */
  save(imageType: ImageType, transparent: boolean, folder: string, filename: string, includeImage: boolean, includeText: boolean, cropToImageSize: boolean): void
  getPaths(): Path[]

  /**
   * @param imageType "png" or "jpg"
   * @param includeImage Set to `true` to include the image loaded from `LocalSourceImage`
   * @param includeText Set to `true` to include the text drawn from `Text`.
   * @param cropToImageSize Set to `true` to crop output image to the image loaded from `LocalSourceImage`
   */
  getBase64(imageType: ImageType, transparent: boolean, includeImage: boolean, includeText: boolean, cropToImageSize: boolean, callback: (error: any, result?: string) => void): void

  static MAIN_BUNDLE: string
  static DOCUMENT: string
  static LIBRARY: string
  static CACHES: string
}

export interface RNSketchCanvasProps {
  containerStyle?: StyleProp<ViewStyle>
  canvasStyle?: StyleProp<ViewStyle>
  onStrokeStart?: () => void
  onStrokeChanged?: () => void
  onStrokeEnd?: (path: Path) => void
  onClosePressed?: () => void
  onUndoPressed?: (id: number) => void
  onClearPressed?: () => void
  onPathsChange?: (pathsCount: number) => void
  user?: string

  closeComponent?: JSX.Element,
  eraseComponent?: JSX.Element,
  undoComponent?: JSX.Element,
  clearComponent?: JSX.Element,
  saveComponent?: JSX.Element,
  strokeComponent?: (color: string) => JSX.Element
  strokeSelectedComponent?: (color: string, index: number, changed: boolean) => JSX.Element
  strokeWidthComponent?: (width: number) => JSX.Element

  strokeColors?: {color: string}[]
  defaultStrokeIndex?: number
  defaultStrokeWidth?: number

  minStrokeWidth?: number
  maxStrokeWidth?: number
  strokeWidthStep?: number

  /**
   * @param imageType "png" or "jpg"
   * @param includeImage default true
   * @param cropToImageSize default false
   */
  savePreference?: () => {folder: string, filename: string, transparent: boolean, imageType: ImageType, includeImage?: boolean, includeText?: boolean, cropToImageSize?: boolean}
  onSketchSaved?: (result: boolean, path: string) => void

  text?: CanvasText[]
  /**
   * {
   *    path: string, 
   *    directory: string, 
   *    mode: 'AspectFill' | 'AspectFit' | 'ScaleToFill'
   * }
   */
  localSourceImage?: LocalSourceImage
}

export default class RNSketchCanvas extends React.Component<RNSketchCanvasProps & ViewProperties> {
  clear(): void
  undo(): number
  addPath(data: Path): void
  deletePath(id: number): void
  save(): void
  nextStrokeWidth(): void

  static MAIN_BUNDLE: string
  static DOCUMENT: string
  static LIBRARY: string
  static CACHES: string
}
