#import "RNSketchCanvasManager.h"
#import "RNSketchCanvas.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTView.h>
#import <React/UIView+React.h>
#import <React/RCTUIManager.h>

@implementation RNSketchCanvasManager

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup
{
    return YES;
}

-(NSDictionary *)constantsToExport {
    return @{
             @"MainBundlePath": [[NSBundle mainBundle] bundlePath],
             @"NSDocumentDirectory": [NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES) firstObject],
             @"NSLibraryDirectory": [NSSearchPathForDirectoriesInDomains(NSLibraryDirectory, NSUserDomainMask, YES) firstObject],
             @"NSCachesDirectory": [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) firstObject],
             };
}

#pragma mark - Events

RCT_EXPORT_VIEW_PROPERTY(onChange, RCTBubblingEventBlock);

#pragma mark - Props
RCT_CUSTOM_VIEW_PROPERTY(localSourceImage, NSDictionary, RNSketchCanvas)
{
    RNSketchCanvas *currentView = !view ? defaultView : view;
    NSDictionary *dict = [RCTConvert NSDictionary:json];
    dispatch_async(dispatch_get_main_queue(), ^{
        [currentView openSketchFile:dict[@"filename"]
                          directory:[dict[@"directory"] isEqual: [NSNull null]] ? @"" : dict[@"directory"]
                        contentMode:[dict[@"mode"] isEqual: [NSNull null]] ? @"" : dict[@"mode"]];
    });
}

RCT_CUSTOM_VIEW_PROPERTY(text, NSArray, RNSketchCanvas)
{
    RNSketchCanvas *currentView = !view ? defaultView : view;
    NSArray *arr = [RCTConvert NSArray:json];
    dispatch_async(dispatch_get_main_queue(), ^{
        [currentView setCanvasText:arr];
    });
}

#pragma mark - Lifecycle

- (UIView *)view
{
    return [[RNSketchCanvas alloc] initWithEventDispatcher: self.bridge.eventDispatcher];
}

#pragma mark - Exported methods


RCT_EXPORT_METHOD(save:(nonnull NSNumber *)reactTag type:(NSString*) type folder:(NSString*) folder filename:(NSString*) filename withTransparentBackground:(BOOL) transparent includeImage:(BOOL)includeImage includeText:(BOOL)includeText cropToImageSize:(BOOL)cropToImageSize)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        [canvas saveImageOfType:type folder:folder filename:filename withTransparentBackground:transparent includeImage:includeImage includeText:includeText cropToImageSize:cropToImageSize];
    }];
}

RCT_EXPORT_METHOD(addPoint:(nonnull NSNumber *)reactTag x: (float)x y: (float)y)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        [canvas addPointX:x Y:y];
    }];
}

RCT_EXPORT_METHOD(addPath:(nonnull NSNumber *)reactTag pathId: (int) pathId strokeColor: (UIColor*) strokeColor strokeWidth: (int) strokeWidth points: (NSArray*) points)
{
    NSMutableArray *cgPoints = [[NSMutableArray alloc] initWithCapacity: points.count];
    for (NSString *coor in points) {
        NSArray *coorInNumber = [coor componentsSeparatedByString: @","];
        [cgPoints addObject: [NSValue valueWithCGPoint: CGPointMake([coorInNumber[0] floatValue], [coorInNumber[1] floatValue])]];
    }

    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        [canvas addPath: pathId strokeColor: strokeColor strokeWidth: strokeWidth points: cgPoints];
    }];
}

RCT_EXPORT_METHOD(newPath:(nonnull NSNumber *)reactTag pathId: (int) pathId strokeColor: (UIColor*) strokeColor strokeWidth: (int) strokeWidth)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        [canvas newPath: pathId strokeColor: strokeColor strokeWidth: strokeWidth];
    }];
}

RCT_EXPORT_METHOD(deletePath:(nonnull NSNumber *)reactTag pathId: (int) pathId)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        [canvas deletePath: pathId];
    }];
}

RCT_EXPORT_METHOD(endPath:(nonnull NSNumber *)reactTag)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        [canvas endPath];
    }];
}

RCT_EXPORT_METHOD(clear:(nonnull NSNumber *)reactTag)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        [canvas clear];
    }];
}

RCT_EXPORT_METHOD(transferToBase64:(nonnull NSNumber *)reactTag type: (NSString*) type withTransparentBackground:(BOOL) transparent includeImage:(BOOL)includeImage includeText:(BOOL)includeText cropToImageSize:(BOOL)cropToImageSize :(RCTResponseSenderBlock)callback)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        callback(@[[NSNull null], [canvas transferToBase64OfType: type withTransparentBackground: transparent includeImage:includeImage includeText:includeText cropToImageSize:cropToImageSize]]);
    }];
}

#pragma mark - Utils

- (void)runCanvas:(nonnull NSNumber *)reactTag block:(void (^)(RNSketchCanvas *canvas))block {
    [self.bridge.uiManager addUIBlock:
     ^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, RNSketchCanvas *> *viewRegistry){

         RNSketchCanvas *view = viewRegistry[reactTag];
         if (!view || ![view isKindOfClass:[RNSketchCanvas class]]) {
             RCTLogError(@"Cannot find RNSketchCanvas with tag #%@", reactTag);
             return;
         }

         block(view);
     }];
}

@end
