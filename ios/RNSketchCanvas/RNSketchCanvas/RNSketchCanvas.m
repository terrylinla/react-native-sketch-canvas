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

    CGContextRef _drawingContext, _translucentDrawingContext;
    CGImageRef _frozenImage, _translucentFrozenImage;
    BOOL _needsFullRedraw;

    UIImage *_backgroundImage;
    UIImage *_backgroundImageScaled;
    NSString *_backgroundImageContentMode;
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

    CGRect bounds = self.bounds;

    if (_needsFullRedraw) {
        [self setFrozenImageNeedsUpdate];
        CGContextClearRect(_drawingContext, bounds);
        for (RNSketchData *path in _paths) {
            [path drawInContext:_drawingContext];
        }
        _needsFullRedraw = NO;
    }

    if (!_frozenImage) {
        _frozenImage = CGBitmapContextCreateImage(_drawingContext);
    }
    
    if (!_translucentFrozenImage && _currentPath.isTranslucent) {
        _translucentFrozenImage = CGBitmapContextCreateImage(_translucentDrawingContext);
    }

    if (_backgroundImage) {
        if (!_backgroundImageScaled) {
            _backgroundImageScaled = [self scaleImage:_backgroundImage toSize:bounds.size contentMode: _backgroundImageContentMode];
        }

        [_backgroundImageScaled drawInRect:bounds];
    }

    if (_frozenImage) {
        CGContextDrawImage(context, bounds, _frozenImage);
    }

    if (_translucentFrozenImage && _currentPath.isTranslucent) {
        CGContextDrawImage(context, bounds, _translucentFrozenImage);
    }
}

- (void)layoutSubviews {
    [super layoutSubviews];

    if (!CGSizeEqualToSize(self.bounds.size, _lastSize)) {
        _lastSize = self.bounds.size;
        CGContextRelease(_drawingContext);
        _drawingContext = nil;
        [self createDrawingContext];
        _needsFullRedraw = YES;
        _backgroundImageScaled = nil;
        [self setNeedsDisplay];
    }
}

- (void)createDrawingContext {
    CGFloat scale = self.window.screen.scale;
    CGSize size = self.bounds.size;
    size.width *= scale;
    size.height *= scale;
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    _drawingContext = CGBitmapContextCreate(nil, size.width, size.height, 8, 0, colorSpace, kCGImageAlphaPremultipliedLast);
    _translucentDrawingContext = CGBitmapContextCreate(nil, size.width, size.height, 8, 0, colorSpace, kCGImageAlphaPremultipliedLast);
    CGColorSpaceRelease(colorSpace);

    CGContextConcatCTM(_drawingContext, CGAffineTransformMakeScale(scale, scale));
    CGContextConcatCTM(_translucentDrawingContext, CGAffineTransformMakeScale(scale, scale));
}

- (void)setFrozenImageNeedsUpdate {
    CGImageRelease(_frozenImage);
    CGImageRelease(_translucentFrozenImage);
    _frozenImage = nil;
    _translucentFrozenImage = nil;
}

- (BOOL)openSketchFile:(NSString *)filename directory:(NSString*) directory contentMode:(NSString*)mode {
    if (filename) {
        UIImage *image = [UIImage imageWithContentsOfFile: [directory stringByAppendingPathComponent: filename]];
        image = image ? image : [UIImage imageNamed: filename];
        if(image) {
            if (image.imageOrientation != UIImageOrientationUp) {
                UIGraphicsBeginImageContextWithOptions(image.size, NO, image.scale);
                [image drawInRect:(CGRect){0, 0, image.size}];
                UIImage *normalizedImage = UIGraphicsGetImageFromCurrentImageContext();
                UIGraphicsEndImageContext();
                image = normalizedImage;
            }
            _backgroundImage = image;
            _backgroundImageScaled = nil;
            _backgroundImageContentMode = mode;
            [self setNeedsDisplay];

            return YES;
        }
    }
    return NO;
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
        [data drawInContext:_drawingContext];
        [self setFrozenImageNeedsUpdate];
        [self setNeedsDisplay];
        [self notifyPathsUpdate];
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
        [self notifyPathsUpdate];
    }
}

- (void)addPointX: (float)x Y: (float)y {
    CGPoint newPoint = CGPointMake(x, y);
    CGRect updateRect = [_currentPath addPoint: newPoint];

    if (_currentPath.isTranslucent) {
        CGContextClearRect(_translucentDrawingContext, self.bounds);
        [_currentPath drawInContext:_translucentDrawingContext];
    } else {
        [_currentPath drawLastPointInContext:_drawingContext];
    }

    [self setFrozenImageNeedsUpdate];
    [self setNeedsDisplayInRect:updateRect];
}

- (void)endPath {
    if (_currentPath.isTranslucent) {
        [_currentPath drawInContext:_drawingContext];
    }
    _currentPath = nil;
}

- (void) clear {
    [_paths removeAllObjects];
    _currentPath = nil;
    _needsFullRedraw = YES;
    [self setNeedsDisplay];
    [self notifyPathsUpdate];
}

- (UIImage*)createImageWithTransparentBackground: (BOOL) transparent includeImage:(BOOL)includeImage cropToImageSize:(BOOL)cropToImageSize {
    if (_backgroundImage && cropToImageSize) {
        CGRect rect = CGRectMake(0, 0, _backgroundImage.size.width, _backgroundImage.size.height);
        UIGraphicsBeginImageContextWithOptions(rect.size, !transparent, 1);
        CGContextRef context = UIGraphicsGetCurrentContext();
        if (!transparent) {
            CGContextSetRGBFillColor(context, 1.0f, 1.0f, 1.0f, 1.0f);
            CGContextFillRect(context, rect);
        }
        CGRect targetRect = [Utility fillImageWithSize:self.bounds.size toSize:rect.size contentMode:@"AspectFill"];
        if (includeImage) {
            [_backgroundImage drawInRect:rect];
        }
        CGContextDrawImage(context, targetRect, _frozenImage);
        CGContextDrawImage(context, targetRect, _translucentFrozenImage);
        UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        return img;
    } else {
        CGRect rect = self.bounds;
        UIGraphicsBeginImageContextWithOptions(rect.size, !transparent, 0);
        CGContextRef context = UIGraphicsGetCurrentContext();
        if (!transparent) {
            CGContextSetRGBFillColor(context, 1.0f, 1.0f, 1.0f, 1.0f);
            CGContextFillRect(context, rect);
        }
        if (_backgroundImage && includeImage) {
            CGRect targetRect = [Utility fillImageWithSize:_backgroundImage.size toSize:rect.size contentMode:_backgroundImageContentMode];
            [_backgroundImage drawInRect:targetRect];
        }
        CGContextDrawImage(context, rect, _frozenImage);
        CGContextDrawImage(context, rect, _translucentFrozenImage);
        UIImage *img = UIGraphicsGetImageFromCurrentImageContext();
        UIGraphicsEndImageContext();
        
        return img;
    }
}

- (void)saveImageOfType:(NSString*) type folder:(NSString*) folder filename:(NSString*) filename withTransparentBackground:(BOOL) transparent includeImage:(BOOL)includeImage cropToImageSize:(BOOL)cropToImageSize {
    UIImage *img = [self createImageWithTransparentBackground:transparent includeImage:includeImage cropToImageSize:cropToImageSize];
    
    if (folder != nil && filename != nil) {
        NSURL *tempDir = [[NSURL fileURLWithPath:NSTemporaryDirectory() isDirectory:YES] URLByAppendingPathComponent: folder];
        NSError * error = nil;
        [[NSFileManager defaultManager] createDirectoryAtPath:[tempDir path]
                                  withIntermediateDirectories:YES
                                                   attributes:nil
                                                        error:&error];
        if (error == nil) {
            NSURL *fileURL = [[tempDir URLByAppendingPathComponent: filename] URLByAppendingPathExtension: type];
            NSData *imageData = [self getImageData:img type:type];
            [imageData writeToURL:fileURL atomically:YES];

            if (_onChange) {
                _onChange(@{ @"success": @YES, @"path": [fileURL path]});
            }
        } else {
            if (_onChange) {
                _onChange(@{ @"success": @NO, @"path": [NSNull null]});
            }
        }
    } else {
        if ([type isEqualToString: @"png"]) {
            img = [UIImage imageWithData: UIImagePNGRepresentation(img)];
        }
        UIImageWriteToSavedPhotosAlbum(img, self, @selector(image:didFinishSavingWithError:contextInfo:), nil);
    }
}

- (UIImage *)scaleImage:(UIImage *)originalImage toSize:(CGSize)size contentMode: (NSString*)mode
{
    CGColorSpaceRef colorSpace = CGColorSpaceCreateDeviceRGB();
    CGContextRef context = CGBitmapContextCreate(NULL, size.width, size.height, 8, 0, colorSpace, kCGImageAlphaPremultipliedLast);
    CGContextClearRect(context, CGRectMake(0, 0, size.width, size.height));

    CGRect targetRect = [Utility fillImageWithSize:originalImage.size toSize:size contentMode:mode];
    CGContextDrawImage(context, targetRect, originalImage.CGImage);
    
    CGImageRef scaledImage = CGBitmapContextCreateImage(context);
    CGColorSpaceRelease(colorSpace);
    CGContextRelease(context);
    
    UIImage *image = [UIImage imageWithCGImage:scaledImage];
    CGImageRelease(scaledImage);
    
    return image;
}

- (NSString*) transferToBase64OfType: (NSString*) type withTransparentBackground: (BOOL) transparent includeImage:(BOOL)includeImage cropToImageSize:(BOOL)cropToImageSize {
    UIImage *img = [self createImageWithTransparentBackground:transparent includeImage:includeImage cropToImageSize:cropToImageSize];
    NSData *data = [self getImageData:img type:type];
    return [data base64EncodedStringWithOptions: NSDataBase64Encoding64CharacterLineLength];
}

- (NSData*)getImageData:(UIImage*)img type:(NSString*) type {
    NSData *data;
    if ([type isEqualToString: @"jpg"]) {
        data = UIImageJPEGRepresentation(img, 1.0);
    } else {
        data = UIImagePNGRepresentation(img);
    }
    return data;
}

- (void)image:(UIImage *)image didFinishSavingWithError:(NSError *)error contextInfo: (void *) contextInfo {
    if (_onChange) {
        _onChange(@{ @"success": error != nil ? @NO : @YES });
    }
}

- (void)notifyPathsUpdate {
    if (_onChange) {
        _onChange(@{ @"pathsUpdate": @(_paths.count) });
    }
}

@end
