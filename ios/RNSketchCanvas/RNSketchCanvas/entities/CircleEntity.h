//
//  CircleEntity.h
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 24.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import "MotionEntity.h"

@interface CircleEntity : MotionEntity

@property (nonatomic) CGFloat bordersPadding;

- (instancetype)initAndSetup:(NSInteger)parentWidth parentHeight: (NSInteger)parentHeight width: (NSInteger)width height: (NSInteger)height bordersPadding: (CGFloat)bordersPadding;

@end
