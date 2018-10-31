//
//  TextEntity.h
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 30.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import "base/MotionEntity.h"

@interface TextEntity : MotionEntity

@property (nonatomic) NSString *text;
@property (nonatomic) NSDictionary *textAttributes;
@property (nonatomic) CGSize textSize;
@property (nonatomic) NSMutableParagraphStyle *style;
@property (nonatomic) CGFloat fontSize;
@property (nonatomic) NSString *fontType;
@property (nonatomic) UIFont *font;
@property (nonatomic) CGSize initialBoundsSize;

- (instancetype)initAndSetupWithParent: (NSInteger)parentWidth
                          parentHeight: (NSInteger)parentHeight
                         parentCenterX: (CGFloat)parentCenterX
                         parentCenterY: (CGFloat)parentCenterY
                     parentScreenScale: (CGFloat)parentScreenScale
                                  text: (NSString *)text
                              fontType: (NSString *)fontType
                              fontSize: (CGFloat)fontSize
                        bordersPadding: (CGFloat)bordersPadding
                           borderStyle: (enum BorderStyle)borderStyle
                     borderStrokeWidth: (CGFloat)borderStrokeWidth
                     borderStrokeColor: (UIColor *)borderStrokeColor
                     entityStrokeWidth: (CGFloat)entityStrokeWidth
                     entityStrokeColor: (UIColor *)entityStrokeColor;

- (void)updateText: (NSString *)newText;
- (void)updateFontSize: (CGFloat)newFontSize;

@end
