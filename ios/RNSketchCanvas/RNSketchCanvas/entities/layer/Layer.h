//
//  Layer.h
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 23.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface Layer : NSObject

- (instancetype)initAndReset;
- (void)flip;
- (void)postScale:(CGFloat)scaleDiff;
- (void)postRotate:(CGFloat)rotationInDegreesDiff;
- (void)postTranslate:(CGFloat)dx dy:(CGFloat)dy;
- (void)setRotationInDegrees:(CGFloat)rotationInDegrees;
- (void)setScale:(CGFloat)scale;
- (void)setX:(CGFloat)x;
- (void)setY:(CGFloat)y;
- (void)setFlipped:(BOOL)flipped;
- (CGFloat)getInitialScale;
- (CGFloat)getRotationInDegrees;
- (CGFloat)getScale;
- (CGFloat)getX;
- (CGFloat)getY;
- (BOOL)isFlipped;

@end

@interface Limits : NSObject
- (instancetype)initAndSetup;
@property (nonatomic) CGFloat MIN_SCALE;
@property (nonatomic) CGFloat MAX_SCALE;
@property (nonatomic) CGFloat INITIAL_ENTITY_SCALE;
@end
