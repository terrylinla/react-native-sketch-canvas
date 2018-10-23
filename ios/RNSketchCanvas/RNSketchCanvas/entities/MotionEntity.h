//
//  MotionEntity.h
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 23.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import "Layer.h"
#import "Enumerations.h"

@protocol MotionEntityProtocol <MotionEntity>

@end

@interface MotionEntity <MotionEntityProtocol> : UIView

@property (nonatomic) Layer layer;
@property (nonatomic) BOOL isSelected;
@property (nonatomic) CGFloat holyScale;
@property (nonatomic) NSInteger parentWidth;
@property (nonatomic) NSInteger parentHeight;
@property (nonatomic) BorderStyle borderStyle;


// Add some methods

@end
