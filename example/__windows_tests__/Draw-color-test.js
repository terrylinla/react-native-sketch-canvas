import { driver, By2 } from 'selenium-appium';
import { until, Origin } from 'selenium-webdriver';
import { PNG } from 'pngjs';
import pixelmatch from 'pixelmatch';

const setup = require('../jest-windows/driver.setup');
jest.setTimeout(60000);


function pngFromBase64(base64) {
  const pngBuffer = Buffer.from(base64, 'base64');
  return PNG.sync.read(pngBuffer);
};

const pixelThreshold = 10; // Allow 10 pixel difference, to account for anti-aliasing differences.
function pixelDiffPNGs(img1, img2) {
  return pixelmatch(img1.data, img2.data, null, img1.width, img1.height);
}

beforeAll(() => {
  return driver.startWithCapabilities(setup.capabilites);
});

afterAll(() => {
  return driver.quit();
});

describe('Test draw', () => {

  test('Draw line', async () => {
    await driver.wait(until.elementLocated(By2.nativeName('- Example 2 -')));
    
    // Set to specific size, so that drawing is expectable.
    await driver.manage().window().setRect({x:0,y:0,width:500,height:500});
    await By2.nativeName('- Example 2 -').click();
    await(driver.sleep(1000));

    await By2.nativeName('Red').click();
    await(driver.sleep(1000));

    let screenshot_before_drawing = pngFromBase64(await driver.takeScreenshot());

    // Have to use bridge, since WinAppDriver doesn't support mouse movement directly.
    let builder = driver.actions({bridge: true});
    
    let drawAction = builder
      .move({x:100, y:-100, origin: Origin.POINTER})
      .press()
      .move({x:100, y:-100, origin: Origin.POINTER})
      .release();
    drawAction.perform();
    await(driver.sleep(1000));

    let screenshot_after_drawing = pngFromBase64(await driver.takeScreenshot());
    expect(pixelDiffPNGs(screenshot_before_drawing, screenshot_after_drawing)).toBeGreaterThanOrEqual(pixelThreshold);

    await By2.nativeName('Close').click();
    await(driver.sleep(1000));
  });

  test('Clear clears line', async () => {
    await driver.wait(until.elementLocated(By2.nativeName('- Example 1 -')));
    
    // Set to specific size, so that drawing is expectable.
    await driver.manage().window().setRect({x:0,y:0,width:500,height:500});
    await By2.nativeName('- Example 1 -').click();
    await(driver.sleep(1000));

    let screenshot_before_drawing = pngFromBase64(await driver.takeScreenshot());

    // Have to use bridge, since WinAppDriver doesn't support mouse movement directly.
    let builder = driver.actions({bridge: true});
    
    let drawAction = builder
      .move({x:0, y:50, origin: Origin.POINTER})
      .press()
      .move({x:100, y:100, origin: Origin.POINTER})
      .release();
    drawAction.perform();
    await(driver.sleep(1000));

    let screenshot_after_drawing = pngFromBase64(await driver.takeScreenshot());
    expect(pixelDiffPNGs(screenshot_before_drawing, screenshot_after_drawing)).toBeGreaterThanOrEqual(pixelThreshold);
    
    await By2.nativeName('Clear').click();
    await(driver.sleep(1000));

    let screenshot_after_clearing = pngFromBase64(await driver.takeScreenshot());
    expect(pixelDiffPNGs(screenshot_before_drawing, screenshot_after_clearing)).toBeLessThanOrEqual(pixelThreshold);

    await By2.nativeName('Close').click();
    await(driver.sleep(1000));
  });

  test('Undo line works', async () => {
    await driver.wait(until.elementLocated(By2.nativeName('- Example 1 -')));
    
    // Set to specific size, so that drawing is expectable.
    await driver.manage().window().setRect({x:0,y:0,width:500,height:500});
    await By2.nativeName('- Example 1 -').click();
    await(driver.sleep(1000));

    let screenshot_before_drawing = pngFromBase64(await driver.takeScreenshot());

    // Have to use bridge, since WinAppDriver doesn't support mouse movement directly.
    let builder = driver.actions({bridge: true});
    
    let drawAction = builder
      .move({x:-50, y:0, origin: Origin.POINTER})
      .press()
      .move({x:50, y:50, origin: Origin.POINTER})
      .release();
    drawAction.perform();
    await(driver.sleep(1000));

    let screenshot_after_drawing_first_line = pngFromBase64(await driver.takeScreenshot());
    expect(pixelDiffPNGs(screenshot_before_drawing, screenshot_after_drawing_first_line)).toBeGreaterThanOrEqual(pixelThreshold);

    drawAction.perform();
    await(driver.sleep(1000));

    let screenshot_after_drawing_second_line = pngFromBase64(await driver.takeScreenshot());

    expect(pixelDiffPNGs(screenshot_before_drawing, screenshot_after_drawing_second_line)).toBeGreaterThanOrEqual(pixelThreshold);
    expect(pixelDiffPNGs(screenshot_after_drawing_first_line, screenshot_after_drawing_second_line)).toBeGreaterThanOrEqual(pixelThreshold);

    await By2.nativeName('Undo').click();
    await(driver.sleep(1000));

    let screenshot_after_first_undo = pngFromBase64(await driver.takeScreenshot());
    expect(pixelDiffPNGs(screenshot_after_first_undo, screenshot_after_drawing_first_line)).toBeLessThanOrEqual(pixelThreshold);

    await By2.nativeName('Undo').click();
    await(driver.sleep(1000));

    let screenshot_after_second_undo = pngFromBase64(await driver.takeScreenshot());
    expect(pixelDiffPNGs(screenshot_after_second_undo, screenshot_before_drawing)).toBeLessThanOrEqual(pixelThreshold);

    await By2.nativeName('Close').click();
    await(driver.sleep(1000));
  });

})
