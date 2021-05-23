#pragma once
namespace winrt::RNSketchCanvas::implementation
{
  class Utility
  {
  public:
    static bool isSameColor(winrt::Windows::UI::Color, winrt::Windows::UI::Color) noexcept;
    static bool isColorTransparent(winrt::Windows::UI::Color) noexcept;
    static winrt::Windows::UI::Color uint32ToColor(uint32_t) noexcept;
    static Windows::Foundation::Rect fillImage(float imgWidth, float imgHeight, float targetWidth, float targetHeight, std::string mode);
    static std::vector<std::string> splitLines(std::string input);
  };
}
