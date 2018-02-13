#import "RNSketchCanvasManager.h"
#import "RNSketchCanvas.h"
#import "RNSketchData.h"
#import "RNSketchCanvasDelegate.h"
#import <React/RCTEventDispatcher.h>
#import <React/RCTView.h>
#import <React/UIView+React.h>

@implementation RNSketchCanvas
{
    RCTEventDispatcher *_eventDispatcher;
    NSMutableArray *_paths;
    RNSketchData *_currentPath;
    NSArray *_currentPoints;
    
    CAShapeLayer* _layer;
    RNSketchCanvasDelegate *delegate;
}

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher
{
    self = [super init];
    if (self) {
        _eventDispatcher = eventDispatcher;
        _paths = [NSMutableArray new];
    }
    return self;
}

-(void)layoutSubviews {
    [super layoutSubviews];
    if (!_layer) {
        CGRect bounds = self.bounds;
        
        delegate = [RNSketchCanvasDelegate new];
        _layer = [CAShapeLayer layer];
        _layer.frame = bounds;
        _layer.delegate = delegate;
        _layer.contentsScale = [UIScreen mainScreen].scale;

        [self.layer addSublayer: _layer];
    }
}

- (void)newPath:(int) pathId strokeColor:(UIColor*) strokeColor strokeWidth:(int) strokeWidth {
    if (_currentPath) {
        [_currentPath end];
    }
    _currentPath = [[RNSketchData alloc]
                    initWithId: pathId
                    strokeColor: strokeColor
                    strokeWidth: strokeWidth];
    [_paths addObject: _currentPath];
    [self invalidate: YES];
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
        [_paths addObject: [[RNSketchData alloc]
                            initWithId: pathId
                            strokeColor: strokeColor
                            strokeWidth: strokeWidth
                            points: points]];
        [self invalidate: YES];
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
        [self invalidate: YES];
    }
}

- (void)addPointX: (float)x Y: (float)y {
    _currentPoints = [_currentPath addPoint: CGPointMake(x, y)];
    [self invalidate: NO];
}

- (void)endPath {
    if (_currentPath) {
        [_currentPath end];
    }
}

- (void) clear {
    [_paths removeAllObjects];
    _currentPath = nil;
    _currentPoints = nil;
    [self invalidate: YES];
}

- (void) saveImageOfType: (NSString*) type withTransparentBackground: (BOOL) transparent {
    CGRect rect = self.frame;
    UIGraphicsBeginImageContextWithOptions(rect.size, !transparent, 0);
    CGContextRef context = UIGraphicsGetCurrentContext();
    if ([type isEqualToString: @"png"] && !transparent) {
        CGContextSetRGBFillColor(context, 1.0f, 1.0f, 1.0f, 1.0f);
        CGContextFillRect(context, CGRectMake(0, 0, rect.size.width, rect.size.height));
    }
    [_layer renderInContext:context];
    UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    if ([type isEqualToString: @"jpg"]) {
        UIImageWriteToSavedPhotosAlbum(img, self, @selector(image:didFinishSavingWithError:contextInfo:), nil);
    } else {
        UIImageWriteToSavedPhotosAlbum([UIImage imageWithData: UIImagePNGRepresentation(img)], self, @selector(image:didFinishSavingWithError:contextInfo:), nil);
    }
}

- (NSString*) transferToBase64OfType: (NSString*) type withTransparentBackground: (BOOL) transparent {
    CGRect rect = self.frame;
    
    UIGraphicsBeginImageContextWithOptions(rect.size, !transparent, 0);
    CGContextRef context = UIGraphicsGetCurrentContext();
    if ([type isEqualToString: @"png"] && !transparent) {
        CGContextSetRGBFillColor(context, 1.0f, 1.0f, 1.0f, 1.0f);
        CGContextFillRect(context, CGRectMake(0, 0, rect.size.width, rect.size.height));
    }
    [_layer renderInContext:context];
    UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    if ([type isEqualToString: @"jpg"]) {
        return [UIImageJPEGRepresentation(img, 0.9) base64EncodedStringWithOptions: NSDataBase64Encoding64CharacterLineLength];
    } else {
        return [UIImagePNGRepresentation(img) base64EncodedStringWithOptions: NSDataBase64Encoding64CharacterLineLength];
    }
}

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo: (void *) contextInfo {
    if (_onChange) {
        _onChange(@{ @"success": error != nil ? @NO : @YES });
    }
}

- (void) invalidate:(BOOL)shouldDispatchEvent {
    dispatch_async(dispatch_get_main_queue(), ^{
        if (_onChange && shouldDispatchEvent) {
            _onChange(@{ @"pathsUpdate": @(_paths.count) });
        }
        
        delegate.currentPoints = _currentPoints;
        delegate.paths = _paths;
        [_layer setNeedsDisplay];
    });
}


#pragma CALayerDelegate


@end
