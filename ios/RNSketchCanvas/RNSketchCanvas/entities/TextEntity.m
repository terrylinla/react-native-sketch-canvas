//
//  TextEntity.m
//  RNSketchCanvas
//
//  Created by Thomas Steinbrüchel on 30.10.18.
//  Copyright © 2018 Terry. All rights reserved.
//

#import "base/MotionEntity.h"
#import "TextEntity.h"

@implementation TextEntity
{
}

- (instancetype)initAndSetupWithParent: (NSInteger)parentWidth
                          parentHeight: (NSInteger)parentHeight
                         parentCenterX: (CGFloat)parentCenterX
                         parentCenterY: (CGFloat)parentCenterY
                     parentScreenScale: (CGFloat)parentScreenScale
                                 width: (NSInteger)width
                                height: (NSInteger)height
                                  text: (NSString *)text
                        bordersPadding: (CGFloat)bordersPadding
                           borderStyle: (enum BorderStyle)borderStyle
                     borderStrokeWidth: (CGFloat)borderStrokeWidth
                     borderStrokeColor: (UIColor *)borderStrokeColor
                     entityStrokeWidth: (CGFloat)entityStrokeWidth
                     entityStrokeColor: (UIColor *)entityStrokeColor {
    
    CGFloat realParentCenterX = parentCenterX - width / 4;
    CGFloat realParentCenterY = parentCenterY - height / 4;
    CGFloat realWidth = width / 2;
    CGFloat realHeight = height / 2;
    
    self = [super initAndSetupWithParent:parentWidth
                            parentHeight:parentHeight
                           parentCenterX:realParentCenterX
                           parentCenterY:realParentCenterY
                       parentScreenScale:parentScreenScale
                                   width:realWidth
                                  height:realHeight
                          bordersPadding:bordersPadding
                             borderStyle:borderStyle
                       borderStrokeWidth:borderStrokeWidth
                       borderStrokeColor:borderStrokeColor
                       entityStrokeWidth:entityStrokeWidth
                       entityStrokeColor:entityStrokeColor];
    
    if (self) {
        self.MIN_SCALE = 0.3f;
        self.text = text;
        self.style = [[NSParagraphStyle defaultParagraphStyle] mutableCopy];
        [self.style setAlignment:NSTextAlignmentCenter];
        self.textAttributes = @{
                           NSFontAttributeName: [UIFont systemFontOfSize:20],
                           NSForegroundColorAttributeName: self.entityStrokeColor,
                           NSParagraphStyleAttributeName: self.style
                           };
        self.textSize = [self.text sizeWithAttributes:self.textAttributes];
    }
    
    return self;
}

- (void)drawContent:(CGRect)rect withinContext:(CGContextRef)contextRef {
    self.textAttributes = @{
                            NSFontAttributeName: [UIFont systemFontOfSize:20],
                            NSForegroundColorAttributeName: self.entityStrokeColor,
                            NSParagraphStyleAttributeName: self.style
                            };
    self.textSize = [self.text sizeWithAttributes:self.textAttributes];
    
    UIGraphicsBeginImageContextWithOptions(rect.size, NO, 0.0f); // This (0.0f scale) fixes blurry text when scaling
    
    CGRect textRect = CGRectMake(rect.origin.x, rect.origin.y + (rect.size.height - self.textSize.height) / 2.0, rect.size.width, self.textSize.height);
    
    [self.text drawInRect:textRect withAttributes:self.textAttributes];
    
    UIImage *result = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    
    [result drawInRect:rect];
}

@end
