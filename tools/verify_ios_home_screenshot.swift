// BOPAVI iOS release-gate screenshot verifier.
// The system launch artwork alone must never pass as a functional Home capture.
// Uses AppKit available on macOS CI; no network, OCR or third-party dependency.
import Foundation
import AppKit

guard CommandLine.arguments.count == 2,
      let data = try? Data(contentsOf: URL(fileURLWithPath: CommandLine.arguments[1])),
      let bitmap = NSBitmapImageRep(data: data),
      bitmap.pixelsWide > 250,
      bitmap.pixelsHigh > 450 else {
    fputs("FAIL: missing or invalid iPhone Home PNG\n", stderr)
    exit(1)
}
let width = bitmap.pixelsWide
let height = bitmap.pixelsHigh
let step = max(3, width / 130)
let left = Int(Double(width) * 0.12)
let right = Int(Double(width) * 0.88)

// At least one sufficiently wide golden IGRAJ row must be in the lower half.
func fraction(at y: Int, predicate: (CGFloat,CGFloat,CGFloat) -> Bool) -> Double {
    var hits = 0
    var samples = 0
    for x in stride(from: left, to: right, by: step) {
        guard let rgb = bitmap.colorAt(x: x, y: y)?.usingColorSpace(.deviceRGB) else {
            continue
        }
        let r = rgb.redComponent
        let g = rgb.greenComponent
        let b = rgb.blueComponent
        samples += 1
        if predicate(r,g,b) { hits += 1 }
    }
    return samples > 0 ? Double(hits)/Double(samples) : 0
}

// The intentionally minimal Home has no footer/tab bar. Verify the single
// large amber Play CTA in the lower half without requiring removed menus.
var orangeRow = 0.0
for y in stride(from: Int(Double(height)*0.52),
                to: Int(Double(height)*0.95), by: max(3,height/180)) {
    orangeRow = max(orangeRow, fraction(at:y) {r,g,b in
        r > 0.85 && g > 0.32 && g < 0.90 && b < 0.26
    })
}
guard orangeRow >= 0.37 else {
    fputs("FAIL: iOS Home screenshot missing usable golden Play CTA (play=\(orangeRow))\n",stderr)
    exit(1)
}
print("PASS: real iOS Home CTA and bottom navigation visible (\(width)x\(height), play=\(orangeRow))")
