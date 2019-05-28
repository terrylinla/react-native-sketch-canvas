import * as React from 'react'
import {
    ViewProperties,
    StyleProp,
    ViewStyle
} from "react-native";
import { GestureHandlerProperties, PanGestureHandler } from "react-native-gesture-handler";

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

//export type TouchStates = true | false | 'draw' | 'touch' | 'none';

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
    paths?: Path[]
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

    /**
     * Android Only: set hardware acceleration. Defaults to false. If you prefer performance over functionality try setting to true
     */
    hardwareAccelerated?: boolean

    onStrokeStart?: (x: number, y: number) => void
    onStrokeChanged?: (x: number, y: number) => void
    onStrokeEnd?: (path: Path) => void
    onSketchSaved?: (result: boolean, path: string) => void
    onPathsChange?: (pathsCount: number) => void,


}

export type GestureHandlerProps = Pick<GestureHandlerProperties, "simultaneousHandlers" | "waitFor"> & { panHandler: React.RefObject<PanGestureHandler> };

export class SketchCanvas extends React.Component<SketchCanvasProps & GestureHandlerProps & ViewProperties> {
    clear(): void
    undo(): number
    addPath(data: Path): void
    addPaths(paths: Path[]): void
    deletePath(id: number): void
    deletePaths(pathIds: number[]): void

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

    /**
   * @param x Set it to `evt.nativeEvent.locationX`
   * @param y Set it to `evt.nativeEvent.locationY`
   * @param pathId Set to the pathId or undefined
   * @param callback If omitted the method returns a Promise
   */
    isPointOnPath(x: number, y: number, pathId: number, callback: (error: any, result?: boolean) => void): void
    isPointOnPath(x: number, y: number, callback: (error: any, result?: Array<number>) => void): void
    isPointOnPath(x: number, y: number, pathId: number): Promise<boolean>
    isPointOnPath(x: number, y: number): Promise<number[]>

    /**
     * start a new path
     * use this method to customize touch handling or to mock drawing animations
     * if customizing touch handling, be sure to pass `touchEnabled = false` to avoid duplicate drawing/touches
     * [startPath, addPoint, endPath]
     * 
     * @param x
     * @param y
     */
    startPath(x: number, y: number): void
    /**
     * add a point to the current path
     * use this method to customize touch handling or to mock drawing animations
     * if customizing touch handling, be sure to pass `touchEnabled = false` to avoid duplicate drawing/touches
     * [startPath, addPoint, endPath]
     * 
     * @param x
     * @param y
     */
    addPoint(x: number, y: number): void
    /**
     * close the current path
     * use this method to customize touch handling or to mock drawing animations
     * if customizing touch handling, be sure to pass `touchEnabled = false` to avoid duplicate drawing/touches
     * [startPath, addPoint, endPath]
     * */
    endPath(): void


    static MAIN_BUNDLE: string
    static DOCUMENT: string
    static LIBRARY: string
    static CACHES: string

    /**
     * Utility function that provides an id for future use
     * */
    static generatePathId(): number
}

export interface RNSketchCanvasProps {
  containerStyle?: StyleProp<ViewStyle>
  canvasStyle?: StyleProp<ViewStyle>
  onStrokeStart?: (x: number, y: number) => void
  onStrokeChanged?: (x: number, y: number) => void
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
