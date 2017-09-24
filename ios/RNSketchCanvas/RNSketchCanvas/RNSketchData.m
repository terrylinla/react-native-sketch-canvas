//
//  RNSketchCanvasData.m
//  RNSketchCanvas
//
//  Created by terry on 03/08/2017.
//  Copyright © 2017 Terry. All rights reserved.
//

#import "RNSketchData.h"

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
        _cgPoints = NULL;
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
        _pointCount = points.count;
        _cgPoints = malloc(sizeof(CGPoint) * _pointCount);
        for (int i=0; i<_pointCount; i++) {
            _cgPoints[i] = [points[i] CGPointValue];
        }
    }
    return self;
}

-(void)dealloc {
    if (_cgPoints) {
        free(_cgPoints);
    }
}

- (NSArray*)addPoint:(CGPoint) point {
    [_points addObject: [NSValue valueWithCGPoint: point]];
    return _points;
}

- (void)end {
    if (!_cgPoints) {
        _pointCount = _points.count;
        _cgPoints = malloc(sizeof(CGPoint) * _pointCount);
        for (int i=0; i<_pointCount; i++) {
            _cgPoints[i] = [_points[i] CGPointValue];
        }
        [_points removeAllObjects];
        _points = nil;
    }
}

@end
