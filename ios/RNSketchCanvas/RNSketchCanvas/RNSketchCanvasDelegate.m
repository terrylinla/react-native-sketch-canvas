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
        CGContextSetLineWidth(context, (float)data.strokeWidth / 2.0);
        CGContextSetStrokeColorWithColor(context, [data.strokeColor CGColor]);
        
        if (data.cgPoints) {
            CGContextAddLines(context, data.cgPoints, data.pointCount);
        } else {
            for (int i=0; i<_currentPoints.count; i++) {
                CGPoint point = [_currentPoints[i] CGPointValue];
                if (first)  CGContextMoveToPoint(context, point.x, point.y);
                else  CGContextAddLineToPoint(context, point.x, point.y);
                first = false;
            }
        }
        
        CGContextStrokePath(context);
    }
}

@end
