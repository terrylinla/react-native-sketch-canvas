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
        CGContextSetLineWidth(context, (float)data.strokeWidth);
        CGContextSetLineCap(context, kCGLineCapRound);
        CGContextSetStrokeColorWithColor(context, [data.strokeColor CGColor]);
        
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

@end
