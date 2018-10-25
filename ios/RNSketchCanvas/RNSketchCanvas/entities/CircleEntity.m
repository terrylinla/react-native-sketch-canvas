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

- (instancetype)initAndSetup:(NSInteger)parentWidth parentHeight:(NSInteger)parentHeight width:(NSInteger)width height:(NSInteger)height {
    self = [super initAndSetup:parentWidth parentHeight:parentHeight width:width height:height];
    
    return self;
}

- (void)drawContent {
}

@end
