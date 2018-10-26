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
    
    self = [super initWithFrame:CGRectMake(parentWidth / 4 , parentHeight / 3, parentWidth / 2, parentWidth / 2)];
    
    if (self) {
        _parentWidth = parentWidth;
        _parentHeight = parentHeight;
        _isSelected = false;
        _initialRotationInRadians = 0.0;
        _rotationInRadians = 0.0;
        _initialCenterPoint = CGPointMake(parentWidth / 16, parentHeight / 16);
        _centerPoint = CGPointMake(parentWidth / 16, parentHeight / 16);
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
    [self moveCenterTo:_initialCenterPoint];
}

- (void)moveCenterTo:(CGPoint)moveToCenter {
    self.center = moveToCenter;
    _centerPoint = self.center;
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
    CGContextRef contextRef = UIGraphicsGetCurrentContext();
    if (contextRef) {
        CGContextSaveGState(contextRef);
        if ([self respondsToSelector:@selector(drawContent:withinContext:)]) {
            [self drawContent:self.bounds withinContext:contextRef];
        }
        CGContextRestoreGState(contextRef);
    }
    
    // Draw Border
    if (_isSelected) {
        if (contextRef) {
            CGContextSaveGState(contextRef);
            
            CGContextSetLineWidth(contextRef, 1.0);
            CGContextSetRGBStrokeColor(contextRef, 0.0, 0.0, 255.0, 1.0);
            if (_borderStyle == DASHED) {
                CGFloat dashPattern[]= {3.0, 2};
                CGContextSetLineDash(contextRef, 0.0, dashPattern, 2);
            }
            CGContextStrokeRect(contextRef, self.bounds);
            
            CGContextRestoreGState(contextRef);
        }
    }
}

- (void)drawBorder:(CGContextRef)contextRef {
    if (_isSelected) {
        if (contextRef) {
            CGContextSaveGState(contextRef);
            
            CGContextSetLineWidth(contextRef, 1.0);
            CGContextSetRGBStrokeColor(contextRef, 0.0, 0.0, 255.0, 1.0);
            if (_borderStyle == DASHED) {
                CGFloat dashPattern[]= {3.0, 2};
                CGContextSetLineDash(contextRef, 0.0, dashPattern, 2);
            }
            CGContextStrokeRect(contextRef, self.bounds);
            
            CGContextRestoreGState(contextRef);
        }
    }
}

- (void)drawContent:(CGRect)rect withinContext:(CGContextRef)contextRef {
    NSAssert(NO, @"This is an abstract method and should be overridden");
}

@end
