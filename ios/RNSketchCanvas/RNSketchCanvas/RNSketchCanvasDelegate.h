//
//  RNSketchCanvasDelegate.h
//  RNSketchCanvas
//
//  Created by terry on 03/08/2017.
//  Copyright © 2017 Terry. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>
#import "RNSketchData.h"

@interface RNSketchCanvasDelegate : NSObject<CALayerDelegate>

@property (strong, nonatomic) NSMutableArray *paths;
@property (strong, nonatomic) NSArray *currentPoints;

@end
