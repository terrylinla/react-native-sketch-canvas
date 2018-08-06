//
//  RNSketchCanvasData.h
//  RNSketchCanvas
//
//  Created by terry on 03/08/2017.
//  Copyright © 2017 Terry. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface RNSketchData : NSObject

@property (nonatomic, readonly) int pathId;
@property (nonatomic, readonly) CGFloat strokeWidth;
@property (nonatomic, readonly) UIColor* strokeColor;
@property (nonatomic, readonly) NSArray<NSValue*> *points;
@property (nonatomic, readonly) BOOL isTranslucent;

- (instancetype)initWithId:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth points: (NSArray*) points;
- (instancetype)initWithId:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth;

- (CGRect)addPoint:(CGPoint) point;

- (void)drawLastPointInContext:(CGContextRef)context;
- (void)drawInContext:(CGContextRef)context;

@end
