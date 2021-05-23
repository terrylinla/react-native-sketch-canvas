#pragma once
#include <vector>

namespace winrt::RNSketchCanvas::implementation
{
  class SketchData
  {
  public:
    std::vector<winrt::Windows::Foundation::Numerics::float2> points;
    int id;
    winrt::Windows::UI::Color strokeColor;
    float strokeWidth;
    bool isTranslucent;

    SketchData(int id, winrt::Windows::UI::Color strokeColor, float strokeWidth);
    SketchData(int id, winrt::Windows::UI::Color strokeColor, float strokeWidth, std::vector<winrt::Windows::Foundation::Numerics::float2> points);
    
    static winrt::Windows::Foundation::Numerics::float2 midPoint(const winrt::Windows::Foundation::Numerics::float2&, const winrt::Windows::Foundation::Numerics::float2&);

    void addPoint(const winrt::Windows::Foundation::Numerics::float2& p);
    
    void drawLastPoint(const winrt::Microsoft::Graphics::Canvas::CanvasDrawingSession&);
    void draw(const winrt::Microsoft::Graphics::Canvas::CanvasDrawingSession&);
    void draw(const winrt::Microsoft::Graphics::Canvas::CanvasDrawingSession&, int);

  private:
    std::optional<winrt::Microsoft::Graphics::Canvas::Geometry::CanvasGeometry> mPath;
    static std::optional<winrt::Microsoft::Graphics::Canvas::Geometry::CanvasStrokeStyle> mStrokeStyle;

    static winrt::Microsoft::Graphics::Canvas::Geometry::CanvasStrokeStyle getStrokeStyle();

    winrt::Microsoft::Graphics::Canvas::Geometry::CanvasGeometry evaluatePath();
    void addPointToPath(
      const winrt::Windows::Foundation::Numerics::float2& tPoint,
      const winrt::Windows::Foundation::Numerics::float2& pPoint,
      const winrt::Windows::Foundation::Numerics::float2& point
    );

  };
}
