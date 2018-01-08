#import "RNSketchCanvasManager.h"
#import "RNSketchCanvas.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTView.h>
#import <React/UIView+React.h>

@implementation RNSketchCanvasManager

RCT_EXPORT_MODULE()

#pragma mark - Lifecycle

- (instancetype)init
{
    if ((self = [super init])) {
        self.sketchCanvasView = nil;
    }

    return self;
}

- (UIView *)view
{
    if (!self.sketchCanvasView) {
        self.sketchCanvasView = [[RNSketchCanvas alloc] initWithEventDispatcher: self.bridge.eventDispatcher];
    }

    return self.sketchCanvasView;
}

#pragma mark - Exported methods


RCT_EXPORT_METHOD(save:(NSString*) type withTransparentBackground:(BOOL) transparent)
{
    [self.sketchCanvasView saveImageOfType: type withTransparentBackground: transparent];
}

RCT_EXPORT_METHOD(addPoint: (float)x : (float)y)
{
    [self.sketchCanvasView addPointX:x Y:y];
}

RCT_EXPORT_METHOD(addPath: (int) pathId strokeColor: (NSString*) strokeColor strokeWidth: (int) strokeWidth points: (NSArray*) points)
{
    NSMutableArray *cgPoints = [[NSMutableArray alloc] initWithCapacity: points.count];
    for (NSString *coor in points) {
        NSArray *coorInNumber = [coor componentsSeparatedByString: @","];
        [cgPoints addObject: [NSValue valueWithCGPoint: CGPointMake([coorInNumber[0] floatValue], [coorInNumber[1] floatValue])]];
    }
    [self.sketchCanvasView addPath: pathId strokeColor: strokeColor strokeWidth: strokeWidth points: cgPoints];
}

RCT_EXPORT_METHOD(newPath: (int) pathId strokeColor: (NSString*) strokeColor strokeWidth: (int) strokeWidth)
{
    [self.sketchCanvasView newPath: pathId strokeColor: strokeColor strokeWidth: strokeWidth];
}

RCT_EXPORT_METHOD(deletePath: (int) pathId)
{
    [self.sketchCanvasView deletePath: pathId];
}

RCT_EXPORT_METHOD(endPath)
{
    [self.sketchCanvasView endPath];
}

RCT_EXPORT_METHOD(clear)
{
    [self.sketchCanvasView clear];
}

RCT_EXPORT_METHOD(transferToBase64: (NSString*) type withTransparentBackground:(BOOL) transparent :(RCTResponseSenderBlock)callback)
{
    callback(@[[NSNull null], [self.sketchCanvasView transferToBase64OfType: type withTransparentBackground: transparent]]);
}

@end
