//
//  MotionEntity.m
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 24.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import "MotionEntity.h"

@implementation MotionEntity
{
    
}

- (instancetype)initAndSetup:(NSInteger)parentWidth parentHeight:(NSInteger)parentHeight width:(NSInteger)width height:(NSInteger)height {
    self = [super initWithFrame:CGRectMake(parentWidth / 2, parentHeight / 2, width, height)];
    if (self) {
        _parentWidth = parentWidth;
        _parentHeight = parentHeight;
        _isSelected = false;
        _initialRotationInRadians = 0.0;
        _rotationInRadians = 0.0;
        _initialCenterPoint = CGPointMake(parentWidth * 0.5f, parentHeight * 0.5f);
        _centerPoint = CGPointMake(parentWidth * 0.5f, parentHeight * 0.5f);
        _initialScale = 0.0;
        _scale = 0.0;
        _borderStyle = DASHED;
        self.backgroundColor = [UIColor clearColor];
    }
    return self;
}

- (BOOL)isEntitySelected {
    return _isSelected;
}

- (BOOL)isPointInEntity:(CGPoint)point {
    return false;
}

- (void)setIsSelected:(BOOL)isSelected {
    _isSelected = isSelected;
}

- (void)moveToParentCenter {
    [self moveCenterTo:CGPointMake(_parentWidth * 0.5f, _parentHeight * 0.5f)];
}

- (void)moveCenterTo:(CGPoint)moveToCenter {
    _centerPoint = self.center;
    self.center = moveToCenter;
}

- (void)rotateEntityBy:(CGFloat)rotationInRadians {
    [self setTransform:CGAffineTransformRotate(self.transform, rotationInRadians)];
}

- (void)moveEntityTo:(CGPoint)locationDiff {
    [self setTransform:CGAffineTransformTranslate(self.transform, locationDiff.x, locationDiff.y)];
}

- (void)scaleEntityBy:(CGFloat)newScale {
    [self setTransform:CGAffineTransformScale(self.transform, newScale, newScale)];
}

- (void)drawRect:(CGRect)rect {
    // Call drawContent here
    
    if ([self respondsToSelector:@selector(drawContent)]) {
        [self drawContent];
    }
    
    // Call drawBorder here
    
    
}

- (void) drawContent {
    NSAssert(NO, @"This is an abstract method and should be overridden");
}
@end
