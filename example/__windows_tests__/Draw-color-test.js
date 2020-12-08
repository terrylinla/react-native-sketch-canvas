import { driver, By2 } from 'selenium-appium';
import { until, Origin } from 'selenium-webdriver';

const setup = require('../jest-windows/driver.setup');
jest.setTimeout(60000);

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

    let screenshot_before_drawing = await driver.takeScreenshot();

    // Have to use bridge, since WinAppDriver doesn't support mouse movement directly.
    let builder = driver.actions({bridge: true});
    
    let drawAction = builder
      .move({x:100, y:-100, origin: Origin.POINTER})
      .press()
      .move({x:100, y:-100, origin: Origin.POINTER})
      .release();
    drawAction.perform();
    await(driver.sleep(1000));

    let screenshot_after_drawing = await driver.takeScreenshot();

    expect(screenshot_before_drawing).not.toBe(screenshot_after_drawing);

    await By2.nativeName('Close').click();
    await(driver.sleep(1000));
  });

  test('Clear clears line', async () => {
    await driver.wait(until.elementLocated(By2.nativeName('- Example 1 -')));
    
    // Set to specific size, so that drawing is expectable.
    await driver.manage().window().setRect({x:0,y:0,width:500,height:500});
    await By2.nativeName('- Example 1 -').click();
    await(driver.sleep(1000));

    let screenshot_before_drawing = await driver.takeScreenshot();

    // Have to use bridge, since WinAppDriver doesn't support mouse movement directly.
    let builder = driver.actions({bridge: true});
    
    let drawAction = builder
      .move({x:0, y:50, origin: Origin.POINTER})
      .press()
      .move({x:100, y:100, origin: Origin.POINTER})
      .release();
    drawAction.perform();
    await(driver.sleep(1000));

    let screenshot_after_drawing = await driver.takeScreenshot();

    expect(screenshot_before_drawing).not.toBe(screenshot_after_drawing);

    await By2.nativeName('Clear').click();
    await(driver.sleep(1000));

    let screenshot_after_clearing = await driver.takeScreenshot();
    expect(screenshot_before_drawing).toBe(screenshot_after_clearing);

    await By2.nativeName('Close').click();
    await(driver.sleep(1000));
  });

  test('Undo line works', async () => {
    await driver.wait(until.elementLocated(By2.nativeName('- Example 1 -')));
    
    // Set to specific size, so that drawing is expectable.
    await driver.manage().window().setRect({x:0,y:0,width:500,height:500});
    await By2.nativeName('- Example 1 -').click();
    await(driver.sleep(1000));

    let screenshot_before_drawing = await driver.takeScreenshot();

    // Have to use bridge, since WinAppDriver doesn't support mouse movement directly.
    let builder = driver.actions({bridge: true});
    
    let drawAction = builder
      .move({x:-50, y:0, origin: Origin.POINTER})
      .press()
      .move({x:50, y:50, origin: Origin.POINTER})
      .release();
    drawAction.perform();
    await(driver.sleep(1000));

    let screenshot_after_drawing_first_line = await driver.takeScreenshot();

    expect(screenshot_before_drawing).not.toBe(screenshot_after_drawing_first_line);

    drawAction.perform();
    await(driver.sleep(1000));

    let screenshot_after_drawing_second_line = await driver.takeScreenshot();

    expect(screenshot_before_drawing).not.toBe(screenshot_after_drawing_second_line);
    expect(screenshot_after_drawing_first_line).not.toBe(screenshot_after_drawing_second_line);

    await By2.nativeName('Undo').click();
    await(driver.sleep(1000));

    let screenshot_after_first_undo = await driver.takeScreenshot();
    expect(screenshot_after_first_undo).toBe(screenshot_after_drawing_first_line);

    await By2.nativeName('Undo').click();
    await(driver.sleep(1000));

    let screenshot_after_second_undo = await driver.takeScreenshot();
    expect(screenshot_after_second_undo).toBe(screenshot_before_drawing);

    await By2.nativeName('Close').click();
    await(driver.sleep(1000));
  });

})
