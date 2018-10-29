//
//  CircleEntity.m
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 24.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MotionEntity.h"
#import "CircleEntity.h"

@implementation CircleEntity
{
}

- (instancetype)initAndSetupWithParent:(NSInteger)parentWidth parentHeight:(NSInteger)parentHeight parentCenterX: (CGFloat)parentCenterX parentCenterY: (CGFloat)parentCenterY parentScreenScale: (CGFloat)parentScreenScale width:(NSInteger)width bordersPadding:(CGFloat)bordersPadding {
    self = [super initAndSetupWithParent:parentWidth parentHeight:parentHeight parentCenterX:parentCenterX parentCenterY:parentCenterY parentScreenScale:parentScreenScale width:width];
    
    if (self) {
        self.bordersPadding = bordersPadding;
    }
    
    return self;
}

- (void)drawContent:(CGRect)rect withinContext:(CGContextRef)contextRef {
    CGContextSetLineWidth(contextRef, (5.0 / self.parentScreenScale) / self.scale);
    CGContextSetRGBStrokeColor(contextRef, 255.0, 0.0, 0.0, 1.0);
    
    CGRect circleRect = CGRectMake(0, 0, rect.size.width, rect.size.height);
    circleRect = CGRectInset(circleRect, self.bordersPadding, self.bordersPadding);
    
    CGContextStrokeEllipseInRect(contextRef, circleRect);
}

@end
