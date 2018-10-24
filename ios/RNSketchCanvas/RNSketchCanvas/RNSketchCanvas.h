#import <UIKit/UIKit.h>
#import "entities/Enumerations.h"

@class RCTEventDispatcher;

@interface RNSketchCanvas : UIView <UIGestureRecognizerDelegate>

@property (nonatomic, copy) RCTBubblingEventBlock onChange;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher;

- (BOOL)openSketchFile:(NSString *)filename directory:(NSString*) directory contentMode:(NSString*)mode;
- (void)setCanvasText:(NSArray *)text;
- (void)newPath:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth;
- (void)addPath:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth points:(NSArray*) points;
- (void)deletePath:(int) pathId;
- (void)addPointX: (float)x Y: (float)y isMove: (BOOL)isMove;
- (void)endPath;
- (void)clear;
- (void)saveImageOfType:(NSString*) type folder:(NSString*) folder filename:(NSString*) filename withTransparentBackground:(BOOL) transparent includeImage:(BOOL)includeImage includeText:(BOOL)includeText cropToImageSize:(BOOL)cropToImageSize;
- (NSString*) transferToBase64OfType: (NSString*) type withTransparentBackground: (BOOL) transparent includeImage:(BOOL)includeImage includeText:(BOOL)includeText cropToImageSize:(BOOL)cropToImageSize;
- (void)setShapeConfiguration:(NSDictionary *)dict;
- (void)addEntity:(NSString *)entityType textShapeFontType: (NSString *)textShapeFontType textShapeFontSize: (NSNumber *)textShapeFontSize textShapeText: (NSString *)textShapeText imageShapeAsset: (NSString *)imageShapeAsset;
- (void)releaseSelectedEntity;
- (void)increaseTextEntityFontSize;
- (void)decreaseTextEntityFontSize;
- (void)setTextEntityText:(NSString *)newText;
@end


@interface CanvasText : NSObject

@property (nonatomic) NSString *text;
@property (nonatomic) UIFont *font;
@property (nonatomic) UIColor *fontColor;
@property (nonatomic) CGPoint anchor, position;
@property (nonatomic) NSDictionary *attribute;
@property (nonatomic) BOOL isAbsoluteCoordinate;
@property (nonatomic) CGRect drawRect;
@end
