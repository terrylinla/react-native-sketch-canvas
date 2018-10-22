//
//  Utility.h
//  RNSketchCanvas
//
//  Created by TERRY on 2018/5/8.
//  Copyright © 2018年 Terry. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "RNSketchData.h"

CGPoint midPoint (CGPoint p1, CGPoint p2);

@interface Utility : NSObject

+ (void)addPointToPath: (UIBezierPath*)path
               toPoint: (CGPoint)point
         tertiaryPoint: (CGPoint)tPoint
         previousPoint: (CGPoint) pPoint;
+ (BOOL)isSameColor:(UIColor *)color1 color:(UIColor *)color2;
+ (CGRect)fillImageWithSize:(CGSize)imgSize toSize:(CGSize)targetSize contentMode:(NSString*)mode;
+ (BOOL)pointInTriangle:(CGPoint)pt v1: (CGPoint)v1 v2: (CGPoint)v2 v3:(CGPoint)v3;
+ (CGFloat)crossProduct:(CGPoint)a withCGPointB: (CGPoint)b withCGPointC: (CGPoint)c;
+ (CGFloat)crossProduct:(CGFloat)ax ay: (CGFloat)ay bx: (CGFloat)bx by: (CGFloat)by cx: (CGFloat)cx cy: (CGFloat)cy;

@end
