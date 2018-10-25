//
//  MotionEntity.h
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 23.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "Enumerations.h"

@protocol MotionEntityProtocol
- (void)drawContent:(CGRect)rect withinContext: (CGContextRef)contextRef;
@end

@interface MotionEntity : UIView <MotionEntityProtocol>

@property (nonatomic) BOOL isSelected;
@property (nonatomic) CGFloat initialRotationInRadians;
@property (nonatomic) CGFloat rotationInRadians;
@property (nonatomic) CGPoint initialCenterPoint;
@property (nonatomic) CGPoint centerPoint;
@property (nonatomic) CGFloat initialScale;
@property (nonatomic) CGFloat scale;
@property (nonatomic) NSInteger parentWidth;
@property (nonatomic) NSInteger parentHeight;
@property (nonatomic) enum BorderStyle borderStyle;


- (instancetype)initAndSetup:(NSInteger)parentWidth parentHeight: (NSInteger)parentHeight width: (NSInteger)width height: (NSInteger)height;
- (BOOL)isEntitySelected;
- (BOOL)isPointInEntity:(CGPoint)point;
- (void)setIsSelected:(BOOL)isSelected;
- (void)moveToParentCenter;
- (void)moveCenterTo:(CGPoint)moveToCenter;
- (void)rotateEntityBy:(CGFloat)rotationInRadians;
- (void)moveEntityTo:(CGPoint)locationDiff;
- (void)scaleEntityBy:(CGFloat)newScale;

@end

@interface Limits : NSObject
- (instancetype)initAndSetup;
@property (nonatomic) CGFloat MIN_SCALE;
@property (nonatomic) CGFloat MAX_SCALE;
@property (nonatomic) CGFloat INITIAL_ENTITY_SCALE;
@end
