//
//  RNSketchCanvasData.m
//  RNSketchCanvas
//
//  Created by terry on 03/08/2017.
//  Copyright © 2017 Terry. All rights reserved.
//

#import "RNSketchData.h"
#import "Utility.h"

@implementation RNSketchData
{
    NSMutableArray* _points;
}

- (instancetype)initWithId:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth
{
    self = [super init];
    if (self) {
        _pathId = pathId;
        _strokeColor = strokeColor;
        _strokeWidth = strokeWidth;
        _points = [[NSMutableArray alloc] init];
        _path = [UIBezierPath new];
    }
    return self;
}

- (instancetype)initWithId:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth points: (NSArray*) points
{
    self = [super init];
    if (self) {
        _pathId = pathId;
        _strokeColor = strokeColor;
        _strokeWidth = strokeWidth;
        _points = [points mutableCopy];
        _path = [self evaluatePath];
    }
    return self;
}

- (void)addPoint:(CGPoint) point {
    [_points addObject: [NSValue valueWithCGPoint: point]];
    if (_points.count == 1) {
        [Utility addPointToPath: _path toPoint: point tertiaryPoint: point previousPoint: point];
    } else if (_points.count == 2) {
        [Utility addPointToPath: _path
                        toPoint: point
                  tertiaryPoint: [_points[0] CGPointValue]
                  previousPoint: [_points[0] CGPointValue]];
    } else {
        [Utility addPointToPath: _path
                        toPoint: point
                  tertiaryPoint: [_points[_points.count - 3] CGPointValue]
                  previousPoint:[_points[_points.count - 2] CGPointValue]];
    }
}

- (void)end {
}

- (UIBezierPath*) evaluatePath {
    UIBezierPath *path = [UIBezierPath new];
    CGPoint* points = malloc(sizeof(CGPoint) * _points.count);
    if (_points.count >= 1) {
        points[0] = [_points[0] CGPointValue];
        [Utility addPointToPath: path toPoint: points[0] tertiaryPoint: points[0] previousPoint: points[0]];
    }
    if (_points.count >= 2) {
        points[1] = [_points[1] CGPointValue];
        [Utility addPointToPath: path
                        toPoint: [_points[1] CGPointValue]
                  tertiaryPoint: [_points[0] CGPointValue]
                  previousPoint: [_points[0] CGPointValue]];
    }
    for (int i = 2; i < _points.count; i++) {
        points[i] = [_points[i] CGPointValue];
        CGPoint currentPoint = points[i];
        CGPoint previousPoint = points[i - 1];
        CGPoint tertiaryPoint = points[i - 2];
        [Utility addPointToPath: path toPoint: currentPoint tertiaryPoint: tertiaryPoint previousPoint: previousPoint];
    }
    free(points);
    return path;
}

@end
