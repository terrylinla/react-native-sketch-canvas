//
//  RNSketchCanvasDelegate.m
//  RNSketchCanvas
//
//  Created by terry on 03/08/2017.
//  Copyright © 2017 Terry. All rights reserved.
//

#import "RNSketchCanvasDelegate.h"

@implementation RNSketchCanvasDelegate

- (void)drawLayer:(CALayer *)layer inContext:(CGContextRef)context {
    for (int index=0; index<_paths.count; index++) {
        RNSketchData *data = _paths[index];
        bool first = true;
        bool isErase = [self isSameColor:data.strokeColor color:[UIColor clearColor]];
        CGContextSetLineWidth(context, (float)data.strokeWidth);
        CGContextSetLineCap(context, kCGLineCapRound);
        CGContextSetLineJoin(context, kCGLineJoinRound);
        CGContextSetStrokeColorWithColor(context, [data.strokeColor CGColor]);
        CGContextSetBlendMode(context, isErase ? kCGBlendModeClear : kCGBlendModeNormal);
        
        if (data.cgPoints) {
            if (data.pointCount > 1) {
                CGContextAddLines(context, data.cgPoints, data.pointCount);
            } else if (data.pointCount == 1) {
                CGPoint p = data.cgPoints[0];
                CGContextMoveToPoint(context, p.x, p.y);
                CGContextAddLineToPoint(context, p.x, p.y);
            }
        } else {
            for (int i=0; i<_currentPoints.count; i++) {
                CGPoint point = [_currentPoints[i] CGPointValue];
                if (first)  CGContextMoveToPoint(context, point.x, point.y);
                first = false;
                CGContextAddLineToPoint(context, point.x, point.y);
            }
        }
        
        CGContextStrokePath(context);
    }
}

- (BOOL)isSameColor:(UIColor *)color1 color:(UIColor *)color2 {
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
