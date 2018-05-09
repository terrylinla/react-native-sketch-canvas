//
//  RNSketchCanvasDelegate.m
//  RNSketchCanvas
//
//  Created by terry on 03/08/2017.
//  Copyright © 2017 Terry. All rights reserved.
//

#import "RNSketchCanvasDelegate.h"
#import "Utility.h"

@implementation RNSketchCanvasDelegate

- (void)drawLayer:(CALayer *)layer inContext:(CGContextRef)context {
    if (_paths) {
        for (RNSketchData *data in _paths) {
            if (data.path) {
                [Utility drawPath: data.path inContext: context strokeWidth: data.strokeWidth strokeColor: data.strokeColor];
            }
        }
    }
}

@end
