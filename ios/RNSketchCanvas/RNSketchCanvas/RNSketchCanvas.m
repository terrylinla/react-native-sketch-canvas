#import "RNSketchCanvasManager.h"
#import "RNSketchCanvas.h"
#import "RNSketchData.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTView.h>
#import <React/UIView+React.h>
#import "Utility.h"

@implementation RNSketchCanvas
{
    RCTEventDispatcher *_eventDispatcher;
    NSMutableArray *_paths;
    RNSketchData *_currentPath;
    
    CGSize _lastSize;

    CGContextRef _drawingContext;
    CGImageRef _frozenImage;
    BOOL _needsFullRedraw;
}

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    self = [super init];
    if (self) {
        _eventDispatcher = eventDispatcher;
        _paths = [NSMutableArray new];
        _needsFullRedraw = YES;

        self.backgroundColor = [UIColor clearColor];
        self.clearsContextBeforeDrawing = YES;
    }
    return self;
}

- (void)drawRect:(CGRect)rect {
    CGContextRef context = UIGraphicsGetCurrentContext();

    if (_needsFullRedraw) {
        [self setFrozenImageNeedsUpdate];
        CGContextClearRect(_drawingContext, self.bounds);
        for (RNSketchData *path in _paths) {
            [path drawInContext:_drawingContext];
        }
        _needsFullRedraw = NO;
    }

    if (!_frozenImage) {
        _frozenImage = CGBitmapContextCreateImage(_drawingContext);
    }

    if (_frozenImage) {
        CGContextDrawImage(context, self.bounds, _frozenImage);
    }
}

- (void)layoutSubviews {
    [super layoutSubviews];

    if (!CGSizeEqualToSize(self.bounds.size, _lastSize)) {
        CGContextRelease(_drawingContext);
        _drawingContext = nil;
        [self createDrawingContext];

        _lastSize = self.bounds.size;
    }
}

- (void)createDrawingContext {
    CGFloat scale = self.window.screen.scale;
    CGSize size = self.bounds.size;
    size.width *= scale;
    size.height *= scale;
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    _drawingContext = CGBitmapContextCreate(nil, size.width, size.height, 8, 0, colorSpace, kCGImageAlphaPremultipliedLast);
    CGColorSpaceRelease(colorSpace);

    CGContextConcatCTM(_drawingContext, CGAffineTransformMakeScale(scale, scale));
}

- (void)setFrozenImageNeedsUpdate {
    CGImageRelease(_frozenImage);
    _frozenImage = nil;
}

- (void)newPath:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth {
    _currentPath = [[RNSketchData alloc]
                    initWithId: pathId
                    strokeColor: strokeColor
                    strokeWidth: strokeWidth];
    [_paths addObject: _currentPath];
}

- (void) addPath:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth points:(NSArray*) points {
    bool exist = false;
    for(int i=0; i<_paths.count; i++) {
        if (((RNSketchData*)_paths[i]).pathId == pathId) {
            exist = true;
            break;
        }
    }
    
    if (!exist) {
        RNSketchData *data = [[RNSketchData alloc] initWithId: pathId
                                                  strokeColor: strokeColor
                                                  strokeWidth: strokeWidth
                                                       points: points];
        [_paths addObject: data];
        _needsFullRedraw = YES;
        [self setNeedsDisplay];
//        [self invalidate];
    }
}

- (void)deletePath:(int) pathId {
    int index = -1;
    for(int i=0; i<_paths.count; i++) {
        if (((RNSketchData*)_paths[i]).pathId == pathId) {
            index = i;
            break;
        }
    }
    
    if (index > -1) {
        [_paths removeObjectAtIndex: index];
        _needsFullRedraw = YES;
        [self setNeedsDisplay];
//        [self invalidate];
    }
}

- (void)addPointX: (float)x Y: (float)y {
    CGPoint newPoint = CGPointMake(x, y);
    CGRect updateRect = [_currentPath addPoint: newPoint];

    [_currentPath drawLastPointInContext:_drawingContext];

    [self setFrozenImageNeedsUpdate];
    [self setNeedsDisplayInRect:updateRect];
}

- (void)endPath {
    _currentPath = nil;
}

- (void) clear {
    [_paths removeAllObjects];
    _currentPath = nil;
    _needsFullRedraw = YES;
    [self setNeedsDisplay];
//    [self invalidate];
}

- (void) saveImageOfType: (NSString*) type withTransparentBackground: (BOOL) transparent {
//    CGRect rect = _layer.frame;
//    UIGraphicsBeginImageContextWithOptions(rect.size, !transparent, 0);
//    CGContextRef context = UIGraphicsGetCurrentContext();
//    if ([type isEqualToString: @"png"] && !transparent) {
//        CGContextSetRGBFillColor(context, 1.0f, 1.0f, 1.0f, 1.0f);
//        CGContextFillRect(context, CGRectMake(0, 0, rect.size.width, rect.size.height));
//    }
//    [_layer renderInContext:context];
//    UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
//    UIGraphicsEndImageContext();
//    if ([type isEqualToString: @"jpg"]) {
//        UIImageWriteToSavedPhotosAlbum(img, self, @selector(image:didFinishSavingWithError:contextInfo:), nil);
//    } else {
//        UIImageWriteToSavedPhotosAlbum([UIImage imageWithData: UIImagePNGRepresentation(img)], self, @selector(image:didFinishSavingWithError:contextInfo:), nil);
//    }
}

- (NSString*) transferToBase64OfType: (NSString*) type withTransparentBackground: (BOOL) transparent {
//    CGRect rect = _layer.frame;
//    UIGraphicsBeginImageContextWithOptions(rect.size, !transparent, 0);
//    CGContextRef context = UIGraphicsGetCurrentContext();
//    if ([type isEqualToString: @"png"] && !transparent) {
//        CGContextSetRGBFillColor(context, 1.0f, 1.0f, 1.0f, 1.0f);
//        CGContextFillRect(context, CGRectMake(0, 0, rect.size.width, rect.size.height));
//    }
//    [_layer renderInContext:context];
//    UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
//    UIGraphicsEndImageContext();
//    if ([type isEqualToString: @"jpg"]) {
//        return [UIImageJPEGRepresentation(img, 0.9) base64EncodedStringWithOptions: NSDataBase64Encoding64CharacterLineLength];
//    } else {
//        return [UIImagePNGRepresentation(img) base64EncodedStringWithOptions: NSDataBase64Encoding64CharacterLineLength];
//    }
    return @"";
}

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo: (void *) contextInfo {
    if (_onChange) {
        _onChange(@{ @"success": error != nil ? @NO : @YES });
    }
}

//- (void) invalidate {
//    if (_onChange) {
//        _onChange(@{ @"pathsUpdate": @(_paths.count) });
//    }
//    [_layer setNeedsDisplay];
//}
//
//- (void) invalidateInRect: (CGRect) rect {
//    [_layer setNeedsDisplayInRect: rect ];
//}


@end
