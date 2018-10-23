//
//  Layer.m
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 23.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import "Layer.h"

@implementation Layer

{
    CGFloat rotationInDegrees;
    CGFloat scale;
    CGFloat x;
    CGFloat y;
    BOOL isFlipped;
    Limits *limits;
}

- (instancetype)initAndReset {
    self = [super init];
    if (self) {
        rotationInDegrees = 0.0;
        scale = 1.0;
        isFlipped = FALSE;
        x = 0.0;
        y = 0.0;
        limits = [[Limits init] initAndSetup];
    }
    return self;
}

- (CGFloat)getMinScale {
    return limits.MIN_SCALE;
}

- (CGFloat)getMaxScale {
    return limits.MAX_SCALE;
}

- (CGFloat)getInitialScale {
    return limits.INITIAL_ENTITY_SCALE;
}

- (void)postScale:(CGFloat)scaleDiff {
    CGFloat newVal = scale + scaleDiff;
    if (newVal >= [self getMinScale] && newVal <= [self getMaxScale]) {
        scale = newVal;
    }
}

- (void)postRotate:(CGFloat)rotationInDegreesDiff {
    rotationInDegrees += rotationInDegreesDiff;
    rotationInDegrees = fmod(rotationInDegrees, 360.0);
}

- (void)postTranslate:(CGFloat)dx dy:(CGFloat)dy {
    x += dx;
    y += dy;
}

- (void)flip {
    isFlipped = !isFlipped;
}

- (CGFloat)getRotationInDegrees {
    return rotationInDegrees;
}

- (void)setRotationInDegrees:(CGFloat)rotationInDegrees {
    self.rotationInDegrees = rotationInDegrees;
}

- (CGFloat)getScale {
    return scale;
}

- (void)setScale:(CGFloat)scale {
    self.scale = scale;
}

- (CGFloat)getX {
    return x;
}

- (void)setX:(CGFloat)x {
    self.x = x;
}

- (CGFloat)getY {
    return y;
}

- (void)setY:(CGFloat)y {
    self.y = y;
}

- (BOOL)isFlipped {
    return isFlipped;
}

- (void)setFlipped:(BOOL)flipped {
    isFlipped = flipped;
}
@end



@implementation Limits
- (instancetype)initAndSetup {
    self = [super init];
    if (self) {
        _MIN_SCALE = 0.06;
        _MAX_SCALE = 4.0;
        _INITIAL_ENTITY_SCALE = 0.4;
    }
    return self;
}
@end
