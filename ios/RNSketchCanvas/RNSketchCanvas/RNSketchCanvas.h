#import <UIKit/UIKit.h>

@class RCTEventDispatcher;

@interface RNSketchCanvas : UIView

@property (nonatomic, copy) RCTBubblingEventBlock onChange;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher;

- (void)newPath:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth;
- (void)addPath:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth points:(NSArray*) points;
- (void)deletePath:(int) pathId;
- (void)addPointX: (float)x Y: (float)y;
- (void)endPath;
- (void)clear;
- (void)saveImageOfType: (NSString*) type withTransparentBackground: (BOOL) transparent;
- (NSString*) transferToBase64OfType: (NSString*) type withTransparentBackground: (BOOL) transparent;

@end
