import UIKit

/// Native Core Graphics + CADisplayLink renderer. No WKWebView, JS or network.
final class GameCanvas: UIView {
    let game: GameSimulation
    let reducedMotion: Bool
    let skinIndex: Int
    private let boostFont=UIFont.systemFont(ofSize:16,weight:.heavy)
    private let levelProgressFont=UIFont.monospacedDigitSystemFont(ofSize:16,weight:.heavy)
    private var progressLabel=NSAttributedString(string:"")
    private var lastProgressPassed = -1
    private var lastProgressTotal = -1
    // Same 8 biome colors, centers and 185pt radius as Android.
    // Cached gradients avoid allocating colors or shader arrays every frame.
    private let glowHues:[UInt32]=[0xdaffaf,0xffe6a6,0xc8f6ff,0xffb178,0xfff1cc,0xb7a0ff,0xa4fff4,0xbcb3ff]
    private let glowX:[CGFloat]=[350,390,395,360,380,396,370,365]
    private let glowY:[CGFloat]=[180,132,154,180,146,169,181,170]
    private lazy var glowGradients:[CGGradient?] = {
        let space=CGColorSpaceCreateDeviceRGB()
        return glowHues.map { hue in
            CGGradient(colorsSpace:space,
                colors:[UIColor(rgb:hue).withAlphaComponent(0.278).cgColor,
                        UIColor(rgb:hue).withAlphaComponent(0).cgColor] as CFArray,
                locations:[0,1])
        }
    }()

    private let pickupHues:[UInt32]=[0xffc83b,0xffba83,0xa5efff,0xff9836,0xfff1ad,0xc5adff,0x89f7ef,0xc3a6ff]
    private lazy var pickupSymbol:NSAttributedString=NSAttributedString(string:BopaviCore.collectibleIcons[game.level.world],attributes:[.font:UIFont.systemFont(ofSize:19,weight:.heavy),.foregroundColor:UIColor.white])
    private let birdSprite:UIImage?
    private let leftWing:UIImage?
    private let rightWing:UIImage?
    private let worldBackdrop:UIImage?
    private let feather:[UInt32]=[0x39b5fc,0xffc73e,0xff6883,0x9e86f6,0x45daad,0x6676a8]
    var onFinished: ((GameSimulation) -> Void)?
    var onLevelComplete: ((Int)->Void)?
    var onFlap:(()->Void)?
    var onFlightStarted:(()->Void)?
    var onCollect:(()->Void)?
    var onShieldImpact:(()->Void)?
    private var completedSeen=0
    private var pickupSeen=0
    private var shieldImpactNotified=false
    var onHUDUpdate: ((GameSimulation) -> Void)?
    private var lastHUD:CFTimeInterval = 0
    private var link: CADisplayLink?
    private var previous: CFTimeInterval = 0
    private var reported = false
    var paused = false { didSet { previous = 0;setNeedsDisplay() } }
    private let skyA: [UInt32] = [0x159df7,0x18b5e7,0x418ddc,0x6e287e,0x45aaf6,0x131a4b,0x123969,0x0b123f]
    private let skyB: [UInt32] = [0xd0f8ff,0xffe3b2,0xedfbff,0xffa36d,0xffe9b6,0x7461bc,0x60f6d5,0x5955a9]
    // Biome-specific glints and near-field leaves drawn with Core Graphics.
    private let atmosphereHues:[UInt32]=[0xffedab,0xffdea0,0xeaffff,0xffbd67,0xd3fff1,0xcab7ff,0x8dfff1,0xc5d1ff]
    private let foregroundHues:[UInt32]=[0x63e1a1,0xffce81,0xa6e8ff,0xff834f,0xe0fff0,0xa395ec,0x6ef0da,0x8fa5f1]
    private let pillars: [UInt32] = [0x20b96c,0xf5a65b,0x8ad8f5,0xe65b35,0xe9d9b5,0x57459a,0x5fdddc,0x7973f3]
    private lazy var cachedGradient:CGGradient? = {
        let pair=[UIColor(rgb:skyA[game.level.world]).cgColor,UIColor(rgb:skyB[game.level.world]).cgColor] as CFArray
        return CGGradient(colorsSpace:CGColorSpaceCreateDeviceRGB(),colors:pair,locations:[0,1])
    }()
    private let dark: [UInt32] = [0x096c46,0xbd7153,0x4282ad,0x912f35,0x9d8d80,0x241b60,0x247b9b,0x373192]
    init(game:GameSimulation, reducedMotion:Bool,skinIndex:Int) {
        self.game=game;self.reducedMotion=reducedMotion;self.skinIndex=min(5,max(0,skinIndex))
        self.birdSprite=UIImage(named:"Bopi\(min(5,max(0,skinIndex)))")
        self.leftWing=UIImage(named:"BopiLeft\(min(5,max(0,skinIndex)))")
        self.rightWing=UIImage(named:"BopiRight\(min(5,max(0,skinIndex)))")
        self.worldBackdrop=UIImage(named:"World\(game.level.world)")
        super.init(frame:.zero)
        isOpaque=true; contentMode = .redraw; isMultipleTouchEnabled=false
        accessibilityLabel="Dodirni za let Bopija"
        backgroundColor=UIColor(rgb:skyA[game.level.world])
    }
    required init?(coder:NSCoder){fatalError("Use programmatic initialization")}
    override func didMoveToWindow(){super.didMoveToWindow();if window != nil {start()} else {stop()}}
    private func start(){guard link == nil else{return};let l=CADisplayLink(target:self,selector:#selector(frameTick(_:)));let hz=Float(min(120,window?.screen.maximumFramesPerSecond ?? 60));l.preferredFrameRateRange=CAFrameRateRange(minimum:30,maximum:reducedMotion ? 30 : hz,preferred:reducedMotion ? 30 : hz);l.add(to:.main,forMode:.common);link=l}
    func stop(){link?.invalidate();link=nil;previous=0}
    @objc private func frameTick(_ l:CADisplayLink){
        if paused {previous=0;return}
        if !game.finished {if previous != 0 {game.step(Float(l.timestamp-previous))};previous=l.timestamp}
        else {previous=0}
        if game.completionCount>completedSeen {
            completedSeen=game.completionCount;onLevelComplete?(game.completedOrdinal)
        }
        if game.coins+game.stars>pickupSeen {pickupSeen=game.coins+game.stars;onCollect?()}
        // One callback on shield consumption, not each frame of the impact pulse.
        if game.impactPulse>0 && !shieldImpactNotified {
            shieldImpactNotified=true
            onShieldImpact?()
        } else if game.impactPulse<=0 {
            shieldImpactNotified=false
        }
        setNeedsDisplay()
        if l.timestamp-lastHUD > 0.35 {lastHUD=l.timestamp;onHUDUpdate?(game)}
        if game.finished && !reported {
            reported=true;stop()
            DispatchQueue.main.async { [weak self] in guard let self=self else{return};if self.window != nil {self.onFinished?(self.game)} }
        }
    }
    override func touchesBegan(_ touches:Set<UITouch>,with event:UIEvent?) {
        super.touchesBegan(touches,with:event)
        guard !paused && !game.finished else{return}
        let firstFlap = !game.active
        game.flap()
        if firstFlap && game.active {onFlightStarted?()}
        accessibilityLabel="Bopi leti"
        onFlap?()
    }
    private func color(_ rgb:UInt32,_ alpha:CGFloat=1)->CGColor {CGColor(red:CGFloat((rgb>>16)&255)/255,green:CGFloat((rgb>>8)&255)/255,blue:CGFloat(rgb&255)/255,alpha:alpha)}
    private func rect(_ c:CGContext,_ x:CGFloat,_ y:CGFloat,_ w:CGFloat,_ h:CGFloat,_ rgb:UInt32,_ rad:CGFloat=0,_ alpha:CGFloat=1){
        c.setFillColor(color(rgb,alpha));let r=CGRect(x:x,y:y,width:max(0,w),height:max(0,h));if rad>0 {c.addPath(CGPath(roundedRect:r,cornerWidth:rad,cornerHeight:rad,transform:nil));c.fillPath()} else {c.fill(r)}
    }
    private func oval(_ c:CGContext,_ x:CGFloat,_ y:CGFloat,_ w:CGFloat,_ h:CGFloat,_ rgb:UInt32,_ alpha:CGFloat=1){c.setFillColor(color(rgb,alpha));c.fillEllipse(in:CGRect(x:x,y:y,width:w,height:h))}
    private func triangle(_ c:CGContext,_ a:CGPoint,_ b:CGPoint,_ d:CGPoint,_ rgb:UInt32){
        c.setFillColor(color(rgb));c.beginPath();c.move(to:a);c.addLine(to:b);c.addLine(to:d);c.closePath();c.fillPath()
    }
    override func draw(_ bound:CGRect){
        guard let c=UIGraphicsGetCurrentContext() else{return}
        // Paint sky across the full screen before fitting the collision coordinate grid.
        // No stretched characters or obstacles on tall or short devices.
        if let gradient=cachedGradient {
            c.drawLinearGradient(gradient,start:CGPoint(x:0,y:0),
                end:CGPoint(x:0,y:bound.height),options:[])
        }
        let s=min(bound.width/480,bound.height/800)
        c.saveGState();c.translateBy(x:(bound.width-480*s)/2,y:(bound.height-800*s)/2);c.scaleBy(x:s,y:s)
        if let background=worldBackdrop {
            background.draw(in:CGRect(x:0,y:0,width:480,height:800))
        } else {
            background(c)
        }
        drawWorldLighting(c)
        drawParallaxIslands(c)
        // Shared lightweight atmospheric depth, rendered above the fixed landscape.
        let drift:CGFloat = reducedMotion ? 0 : (CGFloat(game.distance)*0.075).truncatingRemainder(dividingBy:580)
        let mist:UInt32 = (game.level.world == 5 || game.level.world == 7) ? 0x8cbbff : 0xffffff
        let mistAlpha:CGFloat = (game.level.world == 5 || game.level.world == 7) ? 0.13 : 0.22
        for i in 0...3 {
            let x=(CGFloat(i)*174+75-drift+580).truncatingRemainder(dividingBy:580)-95
            let y:CGFloat=146+CGFloat(i%3)*148
            oval(c,x,y,106,24,mist,mistAlpha)
            oval(c,x+24,y-12,50,36,mist,mistAlpha)
        }
        drawWorldAtmosphere(c)
        for (i,g) in game.level.gates.enumerated() {
            let x=CGFloat(game.gateX(g))
            if x < -100 || x > 550 {continue}
            gate(c,g,x,i)
        }
        bird(c)
        // Before the first actual flight, no HUD overlays the illustrated intro.
        if game.active && !game.finished {
            drawBoostHUD(c)
            drawLevelProgress(c)
        }
        if game.levelTransition>0 {
            let opacity:CGFloat=CGFloat(game.levelTransition/0.78)
            rect(c,135,111,210,46,0x103b76,18,0.84*opacity)
            let title="LEVEL \(game.displayLevel)" as NSString
            title.draw(at:CGPoint(x:174,y:120),withAttributes:[
                .font:UIFont.systemFont(ofSize:20,weight:.heavy),
                .foregroundColor:UIColor.white.withAlphaComponent(opacity)])
        }
        if paused {
            rect(c,40,340,400,118,0x1b2b55,24,0.91)
            let pauseTitle="PAUZA" as NSString
            let textSize=pauseTitle.size(withAttributes:[.font:UIFont.systemFont(ofSize:36,weight:.heavy)])
            pauseTitle.draw(at:CGPoint(x:(480-textSize.width)/2,y:378),withAttributes:[
                .font:UIFont.systemFont(ofSize:36,weight:.heavy),.foregroundColor:UIColor.white])
            let resumeHint="ODABERI NASTAVI LET" as NSString
            let hintFont=UIFont.systemFont(ofSize:16,weight:.bold)
            let hintWidth=resumeHint.size(withAttributes:[.font:hintFont]).width
            resumeHint.draw(at:CGPoint(x:(480-hintWidth)/2,y:420),withAttributes:[
                .font:hintFont,.foregroundColor:UIColor.white])
        }
        c.restoreGState()
        let groundTop=(bound.height-800*s)/2+751*s
        if groundTop<bound.height {
            rect(c,0,groundTop,bound.width,bound.height-groundTop,
                game.level.world == 5 || game.level.world == 7 ? 0x171f53 : 0x64c881)
        }
    }
    /// One small gate-progress rail; labels are rebuilt only when gate counts change.
    /// The same 480x800 logical coordinates and fill fraction are used on Android.
    /// This is purely presentation: BopaviCore owns all gate progress and transitions.
    private func drawLevelProgress(_ c:CGContext) {
        let total=max(1,game.level.gates.count)
        let passed=max(0,min(total,game.passed))
        if passed != lastProgressPassed || total != lastProgressTotal {
            progressLabel=NSAttributedString(string:"PROLAZI \(passed)/\(total)",
                attributes:[.font:levelProgressFont,.foregroundColor:UIColor.white])
            lastProgressPassed=passed
            lastProgressTotal=total
            if game.active {accessibilityLabel="Bopi leti. Prolazi \(passed) od \(total)"}
        }
        rect(c,180,160,286,47,0x18305d,16,0.85)
        progressLabel.draw(at:CGPoint(x:193,y:163))
        rect(c,193,188,260,7,0x4f7baf,3.5,0.47)
        if passed>0 {
            let fill=260*CGFloat(passed)/CGFloat(total)
            rect(c,193,188,fill,7,pickupHues[game.level.world],3.5)
        }
    }
    // Drawn in the same unscaled 480x800 game coordinates as Android.
    // Lightweight rounded chips reveal actual remaining protection and magnet time.
    private func drawBoostHUD(_ c:CGContext) {
        let attributes:[NSAttributedString.Key:Any]=[.font:boostFont,.foregroundColor:UIColor.white]
        if game.shield>0 {
            rect(c,14,79,124,31,0x183e75,14,0.87)
            ("ŠTIT ×\(game.shield)" as NSString).draw(at:CGPoint(x:25,y:86),withAttributes:attributes)
        }
        if game.magnetTime>0 {
            rect(c,14,114,137,31,0x183e75,14,0.87)
            let seconds=Int(ceil(Double(game.magnetTime)))
            ("MAGNET \(seconds)s" as NSString).draw(at:CGPoint(x:25,y:121),withAttributes:attributes)
        }
    }
    /// Procedural scenery over the illustration and behind real collision geometry.
    /// 12 atmosphere motes and 9 near-field stems, bounded work per frame.
    /// Reduced motion freezes offsets and sway without hiding environmental detail.
    /// Biome glow unifies illustrated backgrounds and dynamic geometry.
    /// A cached CGGradient is reused on every frame, including reduced-motion mode.
    private func drawWorldLighting(_ c:CGContext) {
        let world=game.level.world
        guard let gradient=glowGradients[world] else{return}
        let center=CGPoint(x:glowX[world],y:glowY[world])
        c.drawRadialGradient(gradient,startCenter:center,startRadius:0,
                             endCenter:center,endRadius:185,options:[])
    }
    // Same three drift speeds, tile width and island geometry as Android.
    // Illustration-only, rendered below gates and Bopi; no per-frame bitmaps.
    private let islandTopHues:[UInt32]=[0x83e575,0xffd98a,0xc3f3ff,0xffac58,
                                         0xffe7ae,0xaa95e6,0x71edda,0xbcb4ff]
    private let islandRockHues:[UInt32]=[0x9e7e70,0xb88a69,0x83b9d4,0x914455,
                                          0xb39b88,0x51417f,0x4578a9,0x55538f]
    private func drawParallaxIslands(_ c:CGContext) {
        let world=game.level.world
        for layer in 0...2 {
            let drift=CGFloat(ParallaxScenery.offset(game.distance,layer:layer,
                                                      reducedMotion:reducedMotion))
            let width:CGFloat=83-CGFloat(layer)*9
            let height:CGFloat=38+CGFloat(layer)*13
            let alpha:CGFloat=CGFloat(layer == 0 ? 78 : (layer == 1 ? 105 : 132))/255
            for i in 0...8 {
                let x=CGFloat(i)*174+CGFloat(layer)*53-drift-118
                if x < -110 || x > 530 {continue}
                let y:CGFloat=416+CGFloat(layer)*92+CGFloat(i%2)*26
                c.setFillColor(color(islandRockHues[world],alpha))
                c.beginPath()
                c.move(to:CGPoint(x:x+6,y:y+5))
                c.addLine(to:CGPoint(x:x+width-6,y:y+5))
                c.addLine(to:CGPoint(x:x+width*0.70,y:y+height))
                c.addLine(to:CGPoint(x:x+width*0.42,y:y+height+11))
                c.addLine(to:CGPoint(x:x+width*0.18,y:y+height*0.75))
                c.closePath()
                c.fillPath()
                oval(c,x-5,y-11,width+10,22,islandTopHues[world],alpha)
                rect(c,x+13,y-9,width-26,4,0xffffff,2,min(145.0/255,alpha+24.0/255))
                if world==0 || world==4 {
                    rect(c,x+width*0.52,y+15,width*0.03,height-7,
                         0xb9f4ff,2,min(120.0/255,alpha))
                }
            }
        }
    }
    private func drawWorldAtmosphere(_ c:CGContext) {
        let world=game.level.world
        let distance:CGFloat=reducedMotion ? 0 : CGFloat(game.distance)
        let t:CGFloat=reducedMotion ? 0 : CGFloat(game.time)
        let dust=atmosphereHues[world]
        for i in 0..<12 {
            let x=((CGFloat(i)*113+29-distance*0.11)
                .truncatingRemainder(dividingBy:560)+560)
                .truncatingRemainder(dividingBy:560)-45
            let shimmer:CGFloat=reducedMotion ? 0 : sin(t*(1.45+CGFloat(i%3)*0.19)+CGFloat(i)*0.87)
            let y=CGFloat(175+(i*83)%430)+shimmer*4
            c.setFillColor(color(dust,reducedMotion ? 108.0/255 : max(45,min(150,106+42*shimmer))/255))
            c.fillEllipse(in:CGRect(x:x-(2.3+CGFloat(i%3)*0.65),y:y-(2.3+CGFloat(i%3)*0.65),
                                    width:2*(2.3+CGFloat(i%3)*0.65),height:2*(2.3+CGFloat(i%3)*0.65)))
            if i%4 == 0 {
                oval(c,x-8,y-8,16,16,dust,27.0/255)
            }
        }
        let near=foregroundHues[world]
        for i in 0..<9 {
            let x=((CGFloat(i)*85+32-distance*0.31)
                .truncatingRemainder(dividingBy:680)+680)
                .truncatingRemainder(dividingBy:680)-60
            let y=CGFloat(700+(i%3)*12)
            let sway:CGFloat=reducedMotion ? 0 : sin(t*1.75+CGFloat(i))*3
            c.setStrokeColor(color(near,83.0/255))
            c.setLineWidth(2.5)
            c.move(to:CGPoint(x:x,y:y+21))
            c.addLine(to:CGPoint(x:x+sway+6,y:y-5))
            c.strokePath()
            oval(c,x+sway-1,y-9,16,10,near,110.0/255)
        }
    }
    private func background(_ c:CGContext){
        let w=game.level.world;let night=w==5 || w==7
        for layer in 0...2 {
            let par:CGFloat=reducedMotion ? 0 : CGFloat(game.distance)*(0.08+CGFloat(layer)*0.12)
            for i in -1...(reducedMotion ? 2 : 4) {
                let x=CGFloat(i)*154+CGFloat(layer)*67-par.truncatingRemainder(dividingBy:154)
                let y:CGFloat=70+CGFloat(layer)*108+CGFloat((i & 1)*27)
                let rgb:UInt32=night ? 0x7d9fcf : 0xebfaff
                oval(c,x,y,91,28,rgb,night ? 0.35 : 0.8)
                oval(c,x+19,y-17,48,46,rgb,night ? 0.35 : 0.8)
            }
        }
        let ridge:UInt32=night ? 0x22265e : (w==3 ? 0xa35455 : 0x83bfbb)
        for i in -1...5 {
            let x=CGFloat(i)*127-(CGFloat(game.distance)*0.055).truncatingRemainder(dividingBy:127)
            triangle(c,CGPoint(x:x,y:753),CGPoint(x:x+55,y:619),CGPoint(x:x+108,y:753),ridge)
            if w==2 {triangle(c,CGPoint(x:x+39,y:650),CGPoint(x:x+55,y:619),CGPoint(x:x+73,y:650),0xffffff)}
        }
        worldDetails(c,w)
        rect(c,0,751,480,49,night ? 0x171f53 : 0x64c881)
        rect(c,0,752,480,8,night ? 0x5d70ca : 0xa8e889)
        if w==7 {for i in 0..<20 {oval(c,CGFloat((i*83+17)%470),CGFloat((i*113+31)%510),2,2,0xffffff)}}
    }
    /// Distinct backgrounds, drawn with simple primitives for lower-end iPhones.
    private func worldDetails(_ c:CGContext,_ w:Int){
        let shift:CGFloat = reducedMotion ? 0 : CGFloat(game.distance)*0.035
        switch w {
        case 0:
            for i in 0...3 {
                let x=CGFloat(i)*166-shift.truncatingRemainder(dividingBy:180)
                let y:CGFloat=505+CGFloat(i%2)*31
                triangle(c,CGPoint(x:x,y:y+12),CGPoint(x:x+104,y:y+12),CGPoint(x:x+44,y:y+98),0xa78970)
                oval(c,x-5,y-10,114,34,0x279f65)
                oval(c,x+3,y-15,96,26,0x8be66f)
                rect(c,x+28,y+24,7,41,0x8feaff,3,0.6)
                for j in 0...2 {oval(c,x+14+CGFloat(j)*29,y-16,18,10,0xbafb87)}
            }
        case 1:
            oval(c,365,100,67,67,0xffec98)
            for i in 0...4 {let x=CGFloat(i)*138-shift.truncatingRemainder(dividingBy:180);oval(c,x,675,108,14,0x5ae7fa,0.65);oval(c,x+28,697,87,11,0xffffff,0.4)}
            rect(c,370,550,12,171,0xac8555,5)
            for j in 0...4 {let y:CGFloat=568+CGFloat(j)*3;oval(c,331+CGFloat(j)*8,y-58,66,75,0x56be81)}
        case 2:for i in 0...8 {let x=(CGFloat(i)*79+33-shift*0.6+480).truncatingRemainder(dividingBy:480),y:CGFloat=135+CGFloat(i%4)*111;c.setStrokeColor(color(0xffffff,0.7));c.setLineWidth(2);c.move(to:CGPoint(x:x-9,y:y));c.addLine(to:CGPoint(x:x+9,y:y));c.move(to:CGPoint(x:x,y:y-9));c.addLine(to:CGPoint(x:x,y:y+9));c.strokePath()}
        case 3:for i in 0...4 {let x=CGFloat(i)*137-shift.truncatingRemainder(dividingBy:180);triangle(c,CGPoint(x:x,y:690),CGPoint(x:x+51,y:575),CGPoint(x:x+90,y:690),0x633047);oval(c,x+38,593,25,16,0xffb24b);rect(c,x+43,620,6,79,0xf58135,4)}
        case 4:for i in 0...3 {let x=CGFloat(i)*165-shift.truncatingRemainder(dividingBy:180);rect(c,x+38,532,24,202,0xe2d5a3,6);rect(c,x+23,522,54,20,0xffe3a7,3);rect(c,x+23,713,54,20,0xffe3a7,3)}
        case 5:oval(c,358,103,67,67,0xffe1b2);oval(c,374,90,68,69,0x131a4b);for i in 0...12 {oval(c,CGFloat((i*119+37)%470),CGFloat((i*77+128)%590),4,4,0xffd89a)}
        case 6:for i in 0...5 {let x=CGFloat(i)*104-shift*0.8;triangle(c,CGPoint(x:x+15,y:733),CGPoint(x:x+40,y:594-CGFloat(i%2)*25),CGPoint(x:x+69,y:733),i%2==0 ? 0x5be4d7 : 0xb68bee);c.setStrokeColor(color(0xffffff,0.7));c.setLineWidth(2);c.move(to:CGPoint(x:x+40,y:594-CGFloat(i%2)*25));c.addLine(to:CGPoint(x:x+40,y:720));c.strokePath()}
        case 7:oval(c,324,110,129,129,0xba79d9);oval(c,347,122,75,46,0x6cdfeb,0.54);c.setStrokeColor(color(0xf8e3ff,0.6));c.setLineWidth(7);c.strokeEllipse(in:CGRect(x:303,y:142,width:170,height:64));for i in 0...11 {oval(c,CGFloat((i*103+31)%460),CGFloat((i*173+61)%500),3,3,0xffffff)}
        default:break
        }
    }
    /// Subtle moving-gate warnings drawn within existing lip bounds.
    /// Purely decorative; the BopaviCore collision opening remains unchanged.
    /// Reduced motion keeps the hints visible but completely still.
    private func drawMovingGateRimCues(_ c:CGContext,_ g:BopaviCore.Gate,
                                         _ x:CGFloat,_ top:CGFloat,_ bottom:CGFloat) {
        if g.movement<=0 {return}
        let pulse:CGFloat = reducedMotion ? 0.5 :
            (sin(CGFloat(game.time)*2.3+CGFloat(g.phase))+1)*0.5
        let offset:CGFloat = reducedMotion ? 4.5 : pulse*9
        let opacity:CGFloat = (70+125*pulse)/255
        let hue=pickupHues[g.kind]
        let right=x+CGFloat(g.width)-23-offset
        // Lip rectangles are [top-28, top] and [bottom, bottom+27].
        rect(c,x+8+offset,top-22,13,5,hue,2,opacity)
        rect(c,right,top-22,13,5,hue,2,opacity)
        rect(c,x+8+offset,bottom+15,13,5,hue,2,opacity)
        rect(c,right,bottom+15,13,5,hue,2,opacity)
    }
    private func gate(_ c:CGContext,_ g:BopaviCore.Gate,_ x:CGFloat,_ index:Int){
        let shape=BopaviCore.opening(g,game.time);let top=CGFloat(shape.top),bottom=CGFloat(shape.bottom)
        let w=CGFloat(g.width)
        let hue:UInt32 = g.kind==0 ? 0xad8c71 : pillars[g.kind]
        let shade:UInt32 = g.kind==0 ? 0x6e5c63 : dark[g.kind]
        let cap:UInt32 = g.kind==0 ? 0x55ce6c : hue
        rect(c,x+7,0,w-12,top,shade,7);rect(c,x,0,w-13,top,hue,8)
        rect(c,x-7,top-28,w+12,28,cap,7)
        rect(c,x+7,bottom,w-12,755-bottom,shade,7);rect(c,x,bottom,w-13,755-bottom,hue,7)
        rect(c,x-7,bottom,w+12,27,cap,7)
        rect(c,x+6,0,7,top-30,g.kind==3 ? 0xffcf7b : 0xffffff,3,0.48)
        rect(c,x+6,bottom+28,7,725-bottom,g.kind==3 ? 0xffcf7b : 0xffffff,3,0.48)
        switch g.kind {
        case 0:
            for j in 0...2 {
                let yy:CGFloat=60+CGFloat(j)*110
                if yy+18<top-29 {rect(c,x+12,yy,35,8,0x906a5c,3,0.48)}
                let by=bottom+38+CGFloat(j)*115
                if by+18<755 {rect(c,x+9,by,35,8,0x906a5c,3,0.48)}
                oval(c,x+CGFloat(j)*17,top-24,16,10,0x4ee882)
                oval(c,x+CGFloat(j)*19,bottom+6,12,6,0x2b934d)
            }
        case 2,6:for j in 0...2 {oval(c,x+CGFloat(j)*16,top-24+CGFloat(j)*7,13,7,shade)}
        case 1:for j in 0...2 {oval(c,x+CGFloat(j)*18,bottom+6,12,6,0xffe6b4)}
        case 3:
            rect(c,x,bottom+7,w,7,0xffd56d,3)
            for j in 0...2 {oval(c,x+12+CGFloat(j)*18,bottom+16+CGFloat(j%2)*5,6,8,0xffa047)}
        case 4:for j in 0...2{rect(c,x+CGFloat(j)*16,top-19,7,14,0xfffff0,2)}
        case 5:
            rect(c,x+11,top-16,8,11,0x9e83ef)
            for j in 0...2 {oval(c,x+14,bottom+8+CGFloat(j)*11,8,8,0xb1a0ff)}
        case 7:
            for j in 0...2 {oval(c,x+CGFloat(j)*17,bottom+3,9,9,0xc1a4ff)}
            rect(c,x+14,top-21,11,7,0xb2ecff,3)
        default:break
        }
        drawMovingGateRimCues(c,g,x,top,bottom)
        // Same rock strata and biome-specific highlights as Android.
        let seam:[UInt32]=[0x685e5e,0xb57852,0xe7fbff,0xff973e,
                           0xffe9ab,0xa693ed,0x89fff0,0xb7b2ff]
        func courses(_ start:CGFloat,_ end:CGFloat){
            var y=start
            var n=0
            while y+16<end && n<12 {
                rect(c,x+8+CGFloat(n%3)*4,y,34+CGFloat(n%2)*6,3,seam[g.kind],2)
                if n%2==0 {
                    c.setStrokeColor(color(shade,0.49))
                    c.setLineWidth(2.5)
                    c.move(to:CGPoint(x:x+25,y:y+5))
                    c.addLine(to:CGPoint(x:x+18,y:y+18))
                    c.addLine(to:CGPoint(x:x+34,y:y+29))
                    c.strokePath()
                }
                if g.kind==0 && n%3==0 {
                    oval(c,x+34,y+5,10,13,0x67d87d)
                }
                y += 57;n += 1
            }
        }
        courses(32,top-34);courses(bottom+34,744)
        rect(c,x-3,top-27,w+5,4,0xffffff,2,0.53)
        rect(c,x-3,bottom+3,w+5,5,0xffffff,2,0.40)
                let center=(top+bottom)*0.5,mid=x+w*0.5
        if g.coin && game.coinVisible(index) {
            let pulse:CGFloat = reducedMotion ? 0 : CGFloat(sin(game.time*5+g.phase))*2
            oval(c,mid-16-pulse,center-16-pulse,32+2*pulse,32+2*pulse,pickupHues[g.kind]);oval(c,mid-9,center-9,18,18,0xffffff,0.46)
            pickupSymbol.draw(at:CGPoint(x:mid-10,y:center-11))
        }
        if g.star && game.starVisible(index) {star(c,mid+35,center-25,12,0xffe25d)}
        if g.power>0 && game.powerVisible(index) {oval(c,mid+26,center+26,26,26,0x1a3c8b);oval(c,mid+34,center+34,10,10,g.power==1 ? 0x63edff : 0xff8ddd)}
    }
    private func star(_ c:CGContext,_ x:CGFloat,_ y:CGFloat,_ size:CGFloat,_ rgb:UInt32){
        c.setFillColor(color(rgb));c.beginPath()
        for i in 0..<10 {
            let angle=CGFloat(i) * .pi / 5 - .pi / 2;let r=(i%2==0 ? size : size*0.43)
            let point=CGPoint(x:x+cos(angle)*r,y:y+sin(angle)*r)
            if i==0 {c.move(to:point)} else {c.addLine(to:point)}
        }
        c.closePath();c.fillPath()
    }
    /// Bounded visual feedback for actual pickup and shield impact events.
    /// No sprite assets or mutable particle objects; reduced motion keeps calm rings.
    private func drawFeedbackSparkles(_ c:CGContext) {
        if reducedMotion {return}
        let pickup=game.collectPulse
        let hit=game.impactPulse
        if pickup<=0 && hit<=0 {return}
        if pickup>0 {
            let progress:CGFloat=max(0,min(1,1-CGFloat(pickup)/0.36))
            let radius:CGFloat=24+42*progress
            c.setFillColor(color(pickupHues[game.level.world],min(210,210*(1-progress))/255))
            for i in 0..<10 {
                let angle=CGFloat(i)*6.2831853/10
                let x:CGFloat=126+cos(angle)*radius
                let y:CGFloat=CGFloat(game.y)+sin(angle)*radius
                let size:CGFloat=2.9+CGFloat(i%3)*0.65
                c.fillEllipse(in:CGRect(x:x-size,y:y-size,width:size*2,height:size*2))
            }
        }
        if hit>0 {
            let progress:CGFloat=max(0,min(1,1-CGFloat(hit)/0.65))
            let radius:CGFloat=33+48*progress
            c.setStrokeColor(color(0xb8efff,min(230,230*(1-progress))/255))
            c.setLineWidth(2.8)
            c.setLineCap(.round)
            for i in 0..<12 {
                let angle=CGFloat(i)*6.2831853/12
                let dx=cos(angle)
                let dy=sin(angle)
                c.move(to:CGPoint(x:126+dx*radius,y:CGFloat(game.y)+dy*radius))
                c.addLine(to:CGPoint(x:126+dx*(radius+9),y:CGFloat(game.y)+dy*(radius+9)))
                c.strokePath()
            }
            c.setLineCap(.butt)
        }
    }
    /// Real tap-response gust; never changes collision or physics and does not
    /// allocate sprite resources inside CADisplayLink. Reduced motion stays calm.
    private func drawFlapWake(_ c:CGContext) {
        guard !reducedMotion && game.flapPulse>0 else {return}
        let strength:CGFloat=max(0,min(1,CGFloat(game.flapPulse)/0.24))
        let progress=1-strength
        let hue=pickupHues[game.level.world]
        let radius:CGFloat=28+27*progress
        c.setStrokeColor(color(hue,105*strength/255))
        c.setLineWidth(2.5)
        c.strokeEllipse(in:CGRect(x:126-radius,y:CGFloat(game.y)-radius,
                                  width:radius*2,height:radius*2))
        c.setFillColor(color(hue,190*strength/255))
        for i in 0..<7 {
            let x:CGFloat=101-CGFloat(i)*9-30*progress
            let y:CGFloat=CGFloat(game.y)+CGFloat(i%3-1)*19+sin(CGFloat(game.time)*13+CGFloat(i)*1.7)*4
            let r:CGFloat=2.4+CGFloat(i%3)*0.7
            c.fillEllipse(in:CGRect(x:x-r,y:y-r,width:2*r,height:2*r))
        }
    }
    private func bird(_ c:CGContext){
        if game.collectPulse>0 {
            let t=CGFloat(game.collectPulse)/0.36
            let r:CGFloat=28+(1-t)*38
            c.setStrokeColor(color(0xffe69c,0.75*t));c.setLineWidth(reducedMotion ? 2 : 3)
            c.strokeEllipse(in:CGRect(x:126-r,y:CGFloat(game.y)-r,width:2*r,height:2*r))
        }
        if game.impactPulse>0 {
            let t=CGFloat(game.impactPulse)/0.65
            let r:CGFloat=35+(1-t)*28
            c.setStrokeColor(color(0xb8efff,0.82*t));c.setLineWidth(5)
            c.strokeEllipse(in:CGRect(x:126-r,y:CGFloat(game.y)-r,width:2*r,height:2*r))
        }
        drawFeedbackSparkles(c)
        drawFlapWake(c)
        // Motion ribbons make the sprite feel embedded in the world, not pasted on.
        if !reducedMotion && game.active && !game.finished {
            for i in 0..<5 {
                let x:CGFloat=107-CGFloat(i)*16
                let y=CGFloat(game.y)+CGFloat(i%3-1)*13+CGFloat(sin(game.time*5+Float(i)))*3
                c.setStrokeColor(color(0xb4edff,CGFloat(130-i*18)/255))
                c.setLineWidth(2.8-CGFloat(i)*0.32)
                c.setLineCap(.round)
                c.beginPath()
                c.move(to:CGPoint(x:x,y:y))
                c.addQuadCurve(to:CGPoint(x:x-27-CGFloat(i)*2,y:y+2),
                               control:CGPoint(x:x-13,y:y-5))
                c.strokePath()
            }
            c.setLineCap(.butt)
        }
        if game.magnetTime>0 {oval(c,92,CGFloat(game.y)-34,68,68,0x4fdfff,0.27)}
        if game.shield>0 {
            c.setStrokeColor(color(0x9fefff));c.setLineWidth(3)
            let r:CGFloat=34+CGFloat(sin(game.time*5))*2
            c.strokeEllipse(in:CGRect(x:126-r,y:CGFloat(game.y)-r,width:2*r,height:2*r))
        }
        c.saveGState();c.translateBy(x:126,y:CGFloat(game.y))
        let angle=CGFloat(min(48,max(-24,game.velocity*0.06))) * .pi / 180
        c.rotate(by:angle)
        let flapStrength:CGFloat = reducedMotion ? 0 : max(0,min(1,CGFloat(game.flapPulse)/0.24))
        let phase:CGFloat = reducedMotion ? 0 : CGFloat(sin(game.time*19))+flapStrength*0.65
        // No whole-body stretching: moving wings provide the animation.
        if let sprite=birdSprite {
            // Real touch adds a crisp 17-degree upstroke that eases away in 240 ms.
            let wingAngle:CGFloat = reducedMotion ? 0 :
                (CGFloat(sin(game.time*19))*23+flapStrength*17) * .pi / 180
            if let left=leftWing {
                c.saveGState();c.translateBy(x:-16,y:5);c.rotate(by:wingAngle)
                left.draw(in:CGRect(x:-34,y:-55,width:100,height:100));c.restoreGState()
            }
            if let right=rightWing {
                c.saveGState();c.translateBy(x:15,y:-4);c.rotate(by:-wingAngle)
                right.draw(in:CGRect(x:-65,y:-46,width:100,height:100));c.restoreGState()
            }
            sprite.draw(in:CGRect(x:-50,y:-50,width:100,height:100))
            c.restoreGState()
            return
        }
        // Asset fallback remains available if resources are missing.
        triangle(c,CGPoint(x:-12,y:11),CGPoint(x:-35-phase*3,y:22),CGPoint(x:-29,y:5),0xef385b)
        oval(c,-30,0,22,16,0x1678d8)
        c.saveGState();c.rotate(by:phase*26 * .pi/180)
        oval(c,-28,-4,36,20,0x0c78dc);oval(c,-25,-6,29,11,0x4ac3ff);c.restoreGState()
        oval(c,-23,-24,49,49,0x095cc8);oval(c,-20,-25,43,48,feather[skinIndex])
        oval(c,-13,5,32,20,0xffffff)
        oval(c,-14,-17,12,7,0xffffff,0.5)
        oval(c,18,0,7,7,0xffa9ad)
        oval(c,-7,-16,14,19,0xffffff);oval(c,6,-15,14,19,0xffffff)
        let blink:CGFloat = !reducedMotion && game.time.truncatingRemainder(dividingBy:4.7)>4.57 ? 3 : 13
        oval(c,-2,-blink,7,blink+4,0x10224f);oval(c,10,-blink,7,blink+4,0x10224f)
        triangle(c,CGPoint(x:11,y:5),CGPoint(x:30,y:9),CGPoint(x:13,y:17),0xffb321)
        rect(c,-18,7,39,7,0xff5b61,3);rect(c,-23,-24,45,7,0x7d451f,3)
        oval(c,-16,-37,21,21,0xa65d2b);oval(c,-12,-34,14,14,0x8cdeff)
        oval(c,3,-36,22,20,0xa65d2b);oval(c,7,-32,13,12,0x8cdeff)
        c.restoreGState()
    }
}

private extension UIColor {
    convenience init(rgb:UInt32){self.init(red:CGFloat((rgb>>16)&255)/255,green:CGFloat((rgb>>8)&255)/255,blue:CGFloat(rgb&255)/255,alpha:1)}
}
