//
//  Utility.m
//  RNSketchCanvas
//
//  Created by TERRY on 2018/5/8.
//  Copyright © 2018年 Terry. All rights reserved.
//

#import "Utility.h"

CGPoint midPoint (CGPoint p1, CGPoint p2) {
    return CGPointMake((p1.x + p2.x) * 0.5, (p1.y + p2.y) * 0.5);
}

@implementation Utility

+ (void)drawPath:(UIBezierPath*)path
       inContext:(CGContextRef)context
     strokeWidth: (float)strokeWidth
     strokeColor: (UIColor*)strokeColor {
    
    bool isErase = [Utility isSameColor:strokeColor color:[UIColor clearColor]];

    CGContextSetLineWidth(context, strokeWidth);
    CGContextSetLineCap(context, kCGLineCapRound);
    CGContextSetLineJoin(context, kCGLineJoinRound);
    CGContextSetStrokeColorWithColor(context, [strokeColor CGColor]);
    CGContextSetBlendMode(context, isErase ? kCGBlendModeClear : kCGBlendModeNormal);

    CGContextAddPath(context, path.CGPath);
    CGContextStrokePath(context);
}

+ (void)addPointToPath: (UIBezierPath*)path
               toPoint: (CGPoint)point
         tertiaryPoint: (CGPoint)tPoint
         previousPoint: (CGPoint) pPoint {
    CGPoint mid1 = midPoint(pPoint, tPoint);
    CGPoint mid2 = midPoint(point, pPoint);
    [path moveToPoint: mid1];
    [path addQuadCurveToPoint: mid2 controlPoint: pPoint];
}

+ (BOOL)isSameColor:(UIColor *)color1 color:(UIColor *)color2 {
    CGFloat red1, green1, blue1, alpha1;
    [color1 getRed:&red1 green:&green1 blue:&blue1 alpha:&alpha1];
    CGFloat red2, green2, blue2, alpha2;
    [color2 getRed:&red2 green:&green2 blue:&blue2 alpha:&alpha2];
    if (red1 == red2 && green1 == green2 && blue1 == blue2 && alpha1 == alpha2) {
        return true;
    }
    return false;
}

@end
