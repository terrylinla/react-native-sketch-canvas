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

#pragma mark - Events

RCT_EXPORT_VIEW_PROPERTY(onChange, RCTBubblingEventBlock);

#pragma mark - Props
RCT_CUSTOM_VIEW_PROPERTY(localSourceImagePath, NSString, RNSketchCanvas)
{
    RNSketchCanvas *currentView = !view ? defaultView : view;
    NSString *localFilePath = [RCTConvert NSString:json];
    dispatch_async(dispatch_get_main_queue(), ^{
        [currentView openSketchFile:localFilePath];
    });
}

#pragma mark - Lifecycle

- (UIView *)view
{
    return [[RNSketchCanvas alloc] initWithEventDispatcher: self.bridge.eventDispatcher];
}

#pragma mark - Exported methods


RCT_EXPORT_METHOD(save:(nonnull NSNumber *)reactTag type:(NSString*) type folder:(NSString*) folder filename:(NSString*) filename withTransparentBackground:(BOOL) transparent)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        [canvas saveImageOfType: type folder: folder filename: filename withTransparentBackground: transparent];
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

RCT_EXPORT_METHOD(transferToBase64:(nonnull NSNumber *)reactTag type: (NSString*) type withTransparentBackground:(BOOL) transparent :(RCTResponseSenderBlock)callback)
{
    [self runCanvas:reactTag block:^(RNSketchCanvas *canvas) {
        callback(@[[NSNull null], [canvas transferToBase64OfType: type withTransparentBackground: transparent]]);
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
