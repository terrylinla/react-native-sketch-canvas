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
{
    CGRect _dirty;
    UIBezierPath *_path;
}

- (instancetype)initWithId:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth {
    self = [super init];
    if (self) {
        _pathId = pathId;
        _strokeColor = strokeColor;
        _strokeWidth = strokeWidth;
        _points = [NSMutableArray new];
        _isTranslucent = CGColorGetComponents(strokeColor.CGColor)[3] != 1.0 &&
            ![Utility isSameColor:strokeColor color:[UIColor clearColor]];
        _path = _isTranslucent ? [UIBezierPath new] : nil;
        _dirty = CGRectZero;
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
        _isTranslucent = CGColorGetComponents(strokeColor.CGColor)[3] != 1.0 &&
            ![Utility isSameColor:strokeColor color:[UIColor clearColor]];
        _path = _isTranslucent ? [self evaluatePath] : nil;
        _dirty = CGRectZero;
    }
    return self;
}

- (CGRect)addPoint:(CGPoint) point {
    [_points addObject: [NSValue valueWithCGPoint: point]];
    
    CGRect updateRect;
    
    NSUInteger pointsCount = _points.count;

    if (_isTranslucent) {
        if (pointsCount >= 3) {
            [Utility addPointToPath: _path
                            toPoint: point
                      tertiaryPoint: [_points[_points.count - 3] CGPointValue]
                      previousPoint:[_points[_points.count - 2] CGPointValue]];
        } else if (pointsCount >= 2) {
            [Utility addPointToPath: _path
                            toPoint: point
                      tertiaryPoint: [_points[0] CGPointValue]
                      previousPoint: [_points[0] CGPointValue]];
        } else {
            [Utility addPointToPath: _path toPoint: point tertiaryPoint: point previousPoint: point];
        }
        
        CGFloat x = point.x, y = point.y;
        _dirty = CGRectIsEmpty(_dirty) ? CGRectMake(x, y, 1, 1) : CGRectUnion(_dirty, CGRectMake(x, y, 1, 1));
        updateRect = CGRectInset(_dirty, -_strokeWidth * 2, -_strokeWidth * 2);
    } else {
        if (pointsCount >= 3) {
            CGPoint a = _points[pointsCount - 3].CGPointValue;
            CGPoint b = _points[pointsCount - 2].CGPointValue;
            CGPoint c = point;
            CGPoint prevMid = midPoint(a, b);
            CGPoint currentMid = midPoint(b, c);
            
            updateRect = CGRectMake(prevMid.x, prevMid.y, 0, 0);
            updateRect = CGRectUnion(updateRect, CGRectMake(b.x, b.y, 0, 0));
            updateRect = CGRectUnion(updateRect, CGRectMake(currentMid.x, currentMid.y, 0, 0));
        } else if (pointsCount >= 2) {
            CGPoint a = _points[pointsCount - 2].CGPointValue;
            CGPoint b = point;
            CGPoint mid = midPoint(a, b);
            
            updateRect = CGRectMake(a.x, a.y, 0, 0);
            updateRect = CGRectUnion(updateRect, CGRectMake(mid.x, mid.y, 0, 0));
        } else {
            updateRect = CGRectMake(point.x, point.y, 0, 0);
        }
        
        updateRect = CGRectInset(updateRect, -_strokeWidth * 2, -_strokeWidth * 2);
    }
    
    return updateRect;
}

- (void)drawLastPointInContext:(CGContextRef)context {
    NSUInteger pointsCount = _points.count;
    if (pointsCount < 1) {
        return;
    };
    
    [self drawInContext:context pointIndex:pointsCount - 1];
}
- (void)drawInContext:(CGContextRef)context {
    if (_isTranslucent) {
        CGContextSetLineWidth(context, _strokeWidth);
        CGContextSetLineCap(context, kCGLineCapRound);
        CGContextSetLineJoin(context, kCGLineJoinRound);
        CGContextSetStrokeColorWithColor(context, [_strokeColor CGColor]);
        CGContextSetBlendMode(context, kCGBlendModeNormal);
        
        CGContextAddPath(context, _path.CGPath);
        CGContextStrokePath(context);
    } else {
        NSUInteger pointsCount = _points.count;
        for (NSUInteger i = 0; i < pointsCount; i++) {
            [self drawInContext:context pointIndex:i];
        }
    }
}

- (void)drawInContext:(CGContextRef)context pointIndex:(NSUInteger)pointIndex {
    NSUInteger pointsCount = _points.count;
    if (pointIndex >= pointsCount) {
        return;
    };

    BOOL isErase = [Utility isSameColor:_strokeColor color:[UIColor clearColor]];

    CGContextSetStrokeColorWithColor(context, _strokeColor.CGColor);
    CGContextSetLineWidth(context, _strokeWidth);
    CGContextSetLineCap(context, kCGLineCapRound);
    CGContextSetLineJoin(context, kCGLineJoinRound);
    CGContextSetBlendMode(context, isErase ? kCGBlendModeClear : kCGBlendModeNormal);
    CGContextBeginPath(context);

    if (pointsCount >= 3 && pointIndex >= 2) {
        CGPoint a = _points[pointIndex - 2].CGPointValue;
        CGPoint b = _points[pointIndex - 1].CGPointValue;
        CGPoint c = _points[pointIndex].CGPointValue;
        CGPoint prevMid = midPoint(a, b);
        CGPoint currentMid = midPoint(b, c);

        // Draw a curve
        CGContextMoveToPoint(context, prevMid.x, prevMid.y);
        CGContextAddQuadCurveToPoint(context, b.x, b.y, currentMid.x, currentMid.y);
    } else if (pointsCount >= 2 && pointIndex >= 1) {
        CGPoint a = _points[pointIndex - 1].CGPointValue;
        CGPoint b = _points[pointIndex].CGPointValue;
        CGPoint mid = midPoint(a, b);

        // Draw a line to the middle of points a and b
        // This is so the next draw which uses a curve looks correct and continues from there
        CGContextMoveToPoint(context, a.x, a.y);
        CGContextAddLineToPoint(context, mid.x, mid.y);
    } else if (pointsCount >= 1) {
        CGPoint a = _points[pointIndex].CGPointValue;

        // Draw a single point
        CGContextMoveToPoint(context, a.x, a.y);
        CGContextAddLineToPoint(context, a.x, a.y);
    }

    CGContextStrokePath(context);
}

// Translucent
- (UIBezierPath*) evaluatePath {
    NSUInteger pointsCount = _points.count;
    UIBezierPath *path = [UIBezierPath new];
    
    for(NSUInteger pointIndex=0; pointIndex<pointsCount; pointIndex++) {
        if (pointsCount >= 3 && pointIndex >= 2) {
            CGPoint a = _points[pointIndex - 2].CGPointValue;
            CGPoint b = _points[pointIndex - 1].CGPointValue;
            CGPoint c = _points[pointIndex].CGPointValue;
            CGPoint prevMid = midPoint(a, b);
            CGPoint currentMid = midPoint(b, c);
            
            // Draw a curve
            [path moveToPoint:prevMid];
            [path addQuadCurveToPoint:currentMid controlPoint:b];
        } else if (pointsCount >= 2 && pointIndex >= 1) {
            CGPoint a = _points[pointIndex - 1].CGPointValue;
            CGPoint b = _points[pointIndex].CGPointValue;
            CGPoint mid = midPoint(a, b);
            
            // Draw a line to the middle of points a and b
            // This is so the next draw which uses a curve looks correct and continues from there
            [path moveToPoint:a];
            [path addLineToPoint:mid];
        } else if (pointsCount >= 1) {
            CGPoint a = _points[pointIndex].CGPointValue;
            
            // Draw a single point
            [path moveToPoint:a];
            [path addLineToPoint:a];
        }
    }
    return path;
}


@end
