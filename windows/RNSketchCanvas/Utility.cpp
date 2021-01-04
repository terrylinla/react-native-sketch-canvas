#include "pch.h"
#include "Utility.h"
#include <sstream>

namespace winrt::RNSketchCanvas::implementation
{
  bool Utility::isSameColor(winrt::Windows::UI::Color color1, winrt::Windows::UI::Color color2) noexcept
  {
    if (color1.R == color2.R && color1.G == color2.G && color1.B == color2.B && color1.A == color2.A)
    {
      return true;
    }
    return false;
  }

  bool Utility::isColorTransparent(winrt::Windows::UI::Color color) noexcept
  {
    // Windows Colors::Transparent is #00FFFFFF but JavaScript code uses #00000000. Let's consider both.
    return
      (color.A == 0) &&
      (
        (color.R == 0 && color.G == 0 && color.R == 0) ||
        (color.R == 255 && color.G == 255 && color.R == 255)
        );
  }

  winrt::Windows::UI::Color Utility::uint32ToColor(uint32_t color) noexcept
  {
    winrt::Windows::UI::Color result;
    result.A = (color >> 24) & 0xff;
    result.R = (color >> 16) & 0xff;
    result.G = (color >> 8) & 0xff;
    result.B = color & 0xff;
    return result;
  }

  Windows::Foundation::Rect Utility::fillImage(float imgWidth, float imgHeight, float targetWidth, float targetHeight, std::string mode)
  {
    float imageAspectRatio = imgWidth / imgHeight;
    float targetAspectRatio = targetWidth / targetHeight;
    if (mode == "AspectFill")
    {
      float scaleFactor = targetAspectRatio < imageAspectRatio ? targetHeight / imgHeight : targetWidth / imgWidth;
      float w = imgWidth * scaleFactor;
      float h = imgHeight * scaleFactor;
      return Windows::Foundation::Rect((targetWidth - w) / 2, (targetHeight - h) / 2, w, h);
    } else if (mode == "ScaleToFill")
    {
      return Windows::Foundation::Rect(0, 0, targetWidth, targetHeight);
    } else
    {
      // AspectFit
      float scaleFactor = targetAspectRatio > imageAspectRatio ? targetHeight / imgHeight : targetWidth / imgWidth;
      float w = imgWidth * scaleFactor;
      float h = imgHeight * scaleFactor;
      return Windows::Foundation::Rect((targetWidth - w) / 2, (targetHeight - h) / 2, w, h);
    }
  }

  std::vector<std::string> Utility::splitLines(std::string input)
  {
    std::stringstream ss(input);
    std::vector<std::string> result;
    std::string line;
    while (std::getline(ss, line, '\n'))
    {
      result.push_back(line);
    }
    return result;
  }

}
