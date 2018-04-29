//
//  RNSketchCanvasDelegate.m
//  RNSketchCanvas
//
//  Created by terry on 03/08/2017.
//  Copyright © 2017 Terry. All rights reserved.
//

#import "RNSketchCanvasDelegate.h"

@implementation RNSketchCanvasDelegate

CGPoint midPoint (CGPoint p1, CGPoint p2) {
    return CGPointMake((p1.x + p2.x) * 0.5, (p1.y + p2.y) * 0.5);
}

- (void)drawCurveFromPoints:(CGPoint *)points pointsCount:(NSInteger)pointsCount inContext:(CGContextRef)context {
    NSMutableArray *pointsArray = [NSMutableArray new];
    for (int i = 0; i < pointsCount; i++) {
        [pointsArray addObject:[NSValue valueWithCGPoint:points[i]]];
    }
    [self drawCurveFromPointsArray:pointsArray context:context];
}

- (void)drawCurveFromPointsArray:(NSArray *)points context:(CGContextRef)context {
    for (int i = 0; i < points.count; i++) {
        
        CGPoint currentPoint = [points[i] CGPointValue];
        CGPoint previousPoint = [points[i] CGPointValue];
        CGPoint tertiaryPoint = [points[i] CGPointValue];
        if (i == 1) {
            previousPoint = [points[i - 1] CGPointValue];
            tertiaryPoint = [points[i - 1] CGPointValue];
        } else if (i >= 2) {
            previousPoint = [points[i - 1] CGPointValue];
            tertiaryPoint = [points[i - 2] CGPointValue];
        }
        
        CGPoint mid1 = midPoint(previousPoint, tertiaryPoint);
        CGPoint mid2 = midPoint(currentPoint, previousPoint);
        CGContextMoveToPoint(context, mid1.x, mid1.y);
        CGContextAddQuadCurveToPoint(context,
                                     previousPoint.x,
                                     previousPoint.y,
                                     mid2.x,
                                     mid2.y);
        
        CGContextStrokePath(context);
        
    }
}

- (void)drawLayer:(CALayer *)layer inContext:(CGContextRef)context {
    for (int index=0; index<_paths.count; index++) {
        RNSketchData *data = _paths[index];
        bool isErase = [self isSameColor:data.strokeColor color:[UIColor clearColor]];
        CGContextSetLineWidth(context, (float)data.strokeWidth);
        CGContextSetLineCap(context, kCGLineCapRound);
        CGContextSetLineJoin(context, kCGLineJoinRound);
        CGContextSetStrokeColorWithColor(context, [data.strokeColor CGColor]);
        CGContextSetBlendMode(context, isErase ? kCGBlendModeClear : kCGBlendModeNormal);
        
        if (data.cgPoints) {
            if (data.pointCount > 1) {
                [self drawCurveFromPoints:data.cgPoints pointsCount:data.pointCount inContext:context];
            } else if (data.pointCount == 1) {
                CGPoint p = data.cgPoints[0];
                CGContextMoveToPoint(context, p.x, p.y);
                CGContextAddLineToPoint(context, p.x, p.y);
                CGContextStrokePath(context);
            }
        } else {
            [self drawCurveFromPointsArray:_currentPoints context:context];
        }
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
