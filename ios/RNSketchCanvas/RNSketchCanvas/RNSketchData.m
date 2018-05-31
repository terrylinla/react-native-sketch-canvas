//
//  RNSketchCanvasData.m
//  RNSketchCanvas
//
//  Created by terry on 03/08/2017.
//  Copyright © 2017 Terry. All rights reserved.
//

#import "RNSketchData.h"
#import "Utility.h"

@interface RNSketchData ()

@property (nonatomic, readwrite) int pathId;
@property (nonatomic, readwrite) CGFloat strokeWidth;
@property (nonatomic, readwrite) UIColor* strokeColor;
@property (nonatomic, readwrite) NSMutableArray<NSValue*> *points;

@end

@implementation RNSketchData

- (instancetype)initWithId:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth {
    self = [super init];
    if (self) {
        _pathId = pathId;
        _strokeColor = strokeColor;
        _strokeWidth = strokeWidth;
        _points = [[NSMutableArray alloc] init];
    }
    return self;
}

- (instancetype)initWithId:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth points: (NSArray*) points {
    self = [super init];
    if (self) {
        _pathId = pathId;
        _strokeColor = strokeColor;
        _strokeWidth = strokeWidth;
        _points = [points mutableCopy];
    }
    return self;
}

- (CGRect)addPoint:(CGPoint) point {
    NSValue *lastPointValue = _points.lastObject;
    [_points addObject: [NSValue valueWithCGPoint: point]];

    CGRect updateRect = CGRectMake(point.x, point.y, 0, 0);

    if (lastPointValue) {
        CGPoint lastPoint = lastPointValue.CGPointValue;
        updateRect = CGRectUnion(updateRect, CGRectMake(lastPoint.x, lastPoint.y, 0, 0));
    }
    updateRect = CGRectInset(updateRect, -_strokeWidth * 2, -_strokeWidth * 2);

    return updateRect;
}

- (void)drawLastPointInContext:(CGContextRef)context {
    NSUInteger pointsCount = _points.count;
    if (pointsCount < 1) {
        return;
    };

    CGPoint fromPoint = _points[pointsCount - (pointsCount > 1 ? 2 : 1)].CGPointValue;
    CGPoint toPoint = _points[pointsCount - 1].CGPointValue;

    [self drawSegmentInContext:context from:fromPoint to:toPoint];
}
- (void)drawInContext:(CGContextRef)context {
    NSValue *prevPointValue;
    for (NSValue *pointValue in _points) {
        if (!prevPointValue) {
            prevPointValue = pointValue;
            continue;
        }

        [self drawSegmentInContext:context from:prevPointValue.CGPointValue to:pointValue.CGPointValue];
        prevPointValue = pointValue;
    }
}

- (void)drawSegmentInContext:(CGContextRef)context from:(CGPoint)fromPoint to:(CGPoint)toPoint {
    BOOL isErase = [Utility isSameColor:_strokeColor color:[UIColor clearColor]];

    CGContextSetStrokeColorWithColor(context, _strokeColor.CGColor);
    CGContextSetLineWidth(context, _strokeWidth);
    CGContextSetLineCap(context, kCGLineCapRound);
    CGContextSetLineJoin(context, kCGLineJoinRound);
    CGContextSetBlendMode(context, isErase ? kCGBlendModeClear : kCGBlendModeNormal);
    CGContextBeginPath(context);
    CGContextMoveToPoint(context, fromPoint.x, fromPoint.y);
    CGContextAddLineToPoint(context, toPoint.x, toPoint.y);
    CGContextStrokePath(context);
}

@end
