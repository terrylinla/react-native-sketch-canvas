import { driver, By2 } from 'selenium-appium';
import { until } from 'selenium-webdriver';

const setup = require('../jest-windows/driver.setup');
jest.setTimeout(60000);

beforeAll(() => {
  return driver.startWithCapabilities(setup.capabilites);
});

afterAll(() => {
  return driver.quit();
});

describe('Control Renders', () => {

  test('Renders default buttons', async () => {
    await driver.wait(until.elementLocated(By2.nativeName('- Example 1 -')));
    await By2.nativeName('- Example 1 -').click();
    await(driver.sleep(1000));
    await driver.wait(until.elementLocated(By2.nativeName('Undo')));
    await driver.wait(until.elementLocated(By2.nativeName('Clear')));
    await driver.wait(until.elementLocated(By2.nativeName('Eraser')));
    await By2.nativeName('Close').click();
    await(driver.sleep(1000));
  });

})
