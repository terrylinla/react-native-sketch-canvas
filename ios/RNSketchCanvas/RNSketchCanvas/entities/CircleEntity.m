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

- (instancetype)initAndSetup:(NSInteger)parentWidth parentHeight:(NSInteger)parentHeight width:(NSInteger)width height:(NSInteger)height bordersPadding:(CGFloat)bordersPadding {
    self = [super initAndSetup:parentWidth parentHeight:parentHeight width:width height:height];
    
    if (self) {
        self.bordersPadding = bordersPadding;
    }
    
    return self;
}

- (void)drawContent:(CGRect)rect withinContext:(CGContextRef)contextRef {
    CGContextSetLineWidth(contextRef, 5.0);
    CGContextSetRGBStrokeColor(contextRef, 255.0, 0.0, 0.0, 1.0);
    
    CGRect circleRect = CGRectMake(0, 0, rect.size.width, rect.size.height);
    circleRect = CGRectInset(circleRect, self.bordersPadding, self.bordersPadding);
    
    CGContextStrokeEllipseInRect(contextRef, circleRect);
}

@end
