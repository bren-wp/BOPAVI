import UIKit

/// Native Core Graphics + CADisplayLink renderer. No WKWebView, JS or network.
final class GameCanvas: UIView {
    let game: GameSimulation
    let reducedMotion: Bool
    let skinIndex: Int
    private let levelProgressFont=UIFont.monospacedDigitSystemFont(ofSize:16,weight:.heavy)
    private let levelBadgeFont=UIFont.monospacedDigitSystemFont(ofSize:15,weight:.heavy)
    private let collectedFont=UIFont.systemFont(ofSize:11,weight:.heavy)
    private var lastLevelBadgeText=""
    private var cachedLevelBadge=NSAttributedString(string:"")
    private let idlePrompt=NSAttributedString(string:"DODIRNI ZA LET",attributes:[
        .font:UIFont.systemFont(ofSize:30,weight:.heavy),.foregroundColor:UIColor.white])
    private var progressLabel=NSAttributedString(string:"")
    private var lastProgressPassed = -1
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

    // Reused translucent two-tone light, matched to Android's 74pt aura.
    // Create gradient colors once rather than inside CADisplayLink.
    private lazy var birdAuraGradient:CGGradient? = {
        let colors=[
            UIColor(red:1,green:243.0/255,blue:187.0/255,alpha:98.0/255).cgColor,
            UIColor(red:45.0/255,green:169.0/255,blue:1,alpha:58.0/255).cgColor,
            UIColor(red:45.0/255,green:169.0/255,blue:1,alpha:0).cgColor
        ] as CFArray
        return CGGradient(colorsSpace:CGColorSpaceCreateDeviceRGB(),
                          colors:colors,locations:[0,0.55,1])
    }()

    private let pickupHues:[UInt32]=[0xffc83b,0xffba83,0xa5efff,0xff9836,0xfff1ad,0xc5adff,0x89f7ef,0xc3a6ff]
    private let birdSprite:UIImage?
    private let hudPortrait:UIImage?
    private let coinWallet:()->Int
    private var lastWalletValue = -1
    private var walletDisplay = "0"
    private let leftWing:UIImage?
    private let rightWing:UIImage?
    // Match 20% smaller Android in-flight art while preserving real wing pivots.
    // Both sprite coordinate systems still map shoulders to their source joints.
    private let birdArtSize:CGFloat
    private let leftWingPivot:CGPoint
    private let rightWingPivot:CGPoint
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
    private var link: CADisplayLink?
    private var previous: CFTimeInterval = 0
    private var reported = false
    var paused = false {
        didSet {
            previous=0
            if paused {stop()} else {start()}
            setNeedsDisplay()
        }
    }
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
    init(game:GameSimulation, reducedMotion:Bool,skinIndex:Int,coinWallet:@escaping ()->Int = {0}) {
        // The complete pilot illustration must resolve using one validated index.
        let selectedSkin=0 // Bopi-only gameplay; legacy saved ownership is retained.
        self.game=game;self.reducedMotion=reducedMotion;self.skinIndex=selectedSkin
        self.coinWallet=coinWallet
        self.hudPortrait=UIImage(named:"BopiPortrait\(selectedSkin)")
        let artSize:CGFloat=124 // Video review: shrink art, never the real hitbox.
        self.birdArtSize=artSize
        let scale=artSize/100
        let premiumScale=artSize/160
        self.leftWingPivot=selectedSkin>=6 ?
            CGPoint(x:-12*premiumScale,y:4*premiumScale) :
            CGPoint(x:-16*scale,y:5*scale)
        self.rightWingPivot=selectedSkin>=6 ?
            CGPoint(x:12*premiumScale,y:4*premiumScale) :
            CGPoint(x:15*scale,y:-4*scale)
        self.birdSprite=UIImage(named:"Bopi\(selectedSkin)")
        self.leftWing=UIImage(named:"BopiLeft\(selectedSkin)")
        self.rightWing=UIImage(named:"BopiRight\(selectedSkin)")
        self.worldBackdrop=UIImage(named:"World\(game.level.world)")
        super.init(frame:.zero)
        isOpaque=true; contentMode = .redraw; isMultipleTouchEnabled=false
        isAccessibilityElement=true
        accessibilityTraits.insert(.button)
        accessibilityLabel="Dodirni za let Bopija"
        backgroundColor=UIColor(rgb:skyA[game.level.world])
    }
    required init?(coder:NSCoder){fatalError("Use programmatic initialization")}
    override func didMoveToWindow(){
        super.didMoveToWindow()
        if window != nil {start();setNeedsDisplay()} else {stop()}
    }
    private func start(){
        // The idle preview is static; only active, unpaused flights require a display link.
        guard link == nil && window != nil && game.active && !game.finished && !paused else{return}
        let l=CADisplayLink(target:self,selector:#selector(frameTick(_:)))
        let hz=Float(min(120,window?.screen.maximumFramesPerSecond ?? 60))
        l.preferredFrameRateRange=CAFrameRateRange(minimum:30,
            maximum:reducedMotion ? 30 : hz,preferred:reducedMotion ? 30 : hz)
        l.add(to:.main,forMode:.common)
        link=l
    }
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
        if game.finished && !reported {
            reported=true;stop()
            DispatchQueue.main.async { [weak self] in guard let self=self else{return};if self.window != nil {self.onFinished?(self.game)} }
        }
    }
    private func triggerFlap(){
        guard !paused && !game.finished else{return}
        let firstFlap = !game.active
        game.flap()
        if firstFlap && game.active {onFlightStarted?()}
        accessibilityLabel="Bopi leti"
        onFlap?()
        start()
        setNeedsDisplay()
    }
    // A swipe begins with one flap; no MOVE event can change altitude.
    override func touchesBegan(_ touches:Set<UITouch>,with event:UIEvent?) {
        super.touchesBegan(touches,with:event)
        if !touches.isEmpty {triggerFlap()}
    }
    override func accessibilityActivate()->Bool {
        guard !paused && !game.finished else{return false}
        triggerFlap()
        return true
    }
    private func color(_ rgb:UInt32,_ alpha:CGFloat=1)->CGColor {CGColor(red:CGFloat((rgb>>16)&255)/255,green:CGFloat((rgb>>8)&255)/255,blue:CGFloat(rgb&255)/255,alpha:alpha)}
    /// Rounded panels share one CGContext path; avoid allocating a CGPath
    /// every time a gate, rim, status badge or HUD control is drawn.
    /// Four quarter arcs preserve the same radius as Android drawRoundRect.
    private func rect(_ c:CGContext,_ x:CGFloat,_ y:CGFloat,_ w:CGFloat,_ h:CGFloat,
                      _ rgb:UInt32,_ rad:CGFloat=0,_ alpha:CGFloat=1) {
        let width=max(0,w), height=max(0,h)
        guard width>0 && height>0 else {return}
        c.setFillColor(color(rgb,alpha))
        if rad<=0 {c.fill(CGRect(x:x,y:y,width:width,height:height));return}
        let r=min(rad,width/2,height/2)
        c.beginPath()
        c.move(to:CGPoint(x:x+r,y:y))
        c.addLine(to:CGPoint(x:x+width-r,y:y))
        c.addArc(center:CGPoint(x:x+width-r,y:y+r),radius:r,
                 startAngle:-.pi/2,endAngle:0,clockwise:false)
        c.addLine(to:CGPoint(x:x+width,y:y+height-r))
        c.addArc(center:CGPoint(x:x+width-r,y:y+height-r),radius:r,
                 startAngle:0,endAngle:.pi/2,clockwise:false)
        c.addLine(to:CGPoint(x:x+r,y:y+height))
        c.addArc(center:CGPoint(x:x+r,y:y+height-r),radius:r,
                 startAngle:.pi/2,endAngle:.pi,clockwise:false)
        c.addLine(to:CGPoint(x:x,y:y+r))
        c.addArc(center:CGPoint(x:x+r,y:y+r),radius:r,
                 startAngle:.pi,endAngle:3 * .pi/2,clockwise:false)
        c.closePath()
        c.fillPath()
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
        // Far scenery fills the physical viewport without stretching Bopi or
        // moving any 480x800 simulation/hitbox coordinates. On long iPhones
        // it crops the image horizontally instead of showing empty sky bars.
        if let world=worldBackdrop, world.size.width>0, world.size.height>0 {
            let scaleToCover=max(bound.width/world.size.width,
                                 bound.height/world.size.height)
            let backdropWidth=world.size.width*scaleToCover
            let backdropHeight=world.size.height*scaleToCover
            world.draw(in:CGRect(x:(bound.width-backdropWidth)*0.5,
                                 y:(bound.height-backdropHeight)*0.5,
                                 width:backdropWidth,height:backdropHeight))
        }
        let s=min(bound.width/480,bound.height/800)
        c.saveGState();c.translateBy(x:(bound.width-480*s)/2,y:(bound.height-800*s)/2);c.scaleBy(x:s,y:s)
        if worldBackdrop==nil {background(c)}
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
        // The next gates are already moving through the same world space.
        for (i,g) in game.upcomingGates.enumerated() {
            let x=CGFloat(game.upcomingGateX(g))
            if x >= -100 && x <= 550 {gate(c,g,x,i,true)}
        }
        for (i,g) in game.level.gates.enumerated() {
            let x=CGFloat(game.gateX(g))
            if x < -100 || x > 550 {continue}
            gate(c,g,x,i)
        }
        bird(c)
        // Matches Android idle hint in the 480x800 logical game coordinates.
        if !game.active && !game.finished {
            rect(c,71,565,338,73,0x102654,27,0.80)
            idlePrompt.draw(at:CGPoint(x:240-idlePrompt.size().width/2,y:579))
        }
        // Before the first actual flight, no HUD overlays the illustrated intro.
        if game.active && !game.finished {
            drawBoostHUD(c)
            drawLevelProgress(c)
        }
        // The native premium pause modal owns this layer, without duplicate text.
        c.restoreGState()
        // Preserve the painted island/clouds down to the final scene pixel:
        // a legacy opaque ground-colored stripe covered the bottom artwork.
        // The full-window sky is already filled before the 480x800 canvas.
    }
    /// Single UIKit-free illustrated HUD: the separately clickable Pause
    /// button remains a real UIButton above the scene, not a painted sprite.
    /// All values are live simulation data. The bar is explicitly progress
    /// within this procedural level, while the cumulative total never resets.
    private func hudText(_ value:String,_ x:CGFloat,_ y:CGFloat,_ font:UIFont,_ rgb:UInt32) {
        (value as NSString).draw(at:CGPoint(x:x,y:y),withAttributes:[
            .font:font,.foregroundColor:UIColor(rgb:rgb)])
    }
    private let hudIconFont=UIFont.systemFont(ofSize:26,weight:.heavy)
    private let hudCoinFont=UIFont.systemFont(ofSize:22,weight:.heavy)
    private let hudWalletFont=UIFont.systemFont(ofSize:15,weight:.heavy)
    private let hudWalletCompactFont=UIFont.systemFont(ofSize:12,weight:.heavy)
    private func drawLevelProgress(_ c:CGContext) {
        let passed=game.totalPassed
        if passed != lastProgressPassed {
            progressLabel=NSAttributedString(string:"PROLAZI UKUPNO  \(passed)",
                 attributes:[.font:levelProgressFont,.foregroundColor:UIColor.white])
            lastProgressPassed=passed
        }
        // Continuous actual course distance, matching the native Android gauge.
        // Never display fictitious metre values from the static reference.
        let total=max(1,game.level.gates.count)
        let ratio=CGFloat(game.levelProgress)
        hudGlass(c,146,18,260,59)
        hudText("♛",154,28,hudIconFont,0xffd45b)
        hudText("⚑",376,29,hudIconFont,0xffffff)
        rect(c,181,34,195,21,0x05234e,10)
        rect(c,184,38,189,13,0x0b66bf,7)
        if ratio>0 {
            rect(c,184,38,189*ratio,13,0x15cfff,7)
            rect(c,185,39,187*ratio,4,0xffffff,2,0.67)
        }
        // The existing race gauge briefly celebrates level entry without
        // covering obstacles or interrupting the continuous simulation.
        let entering=game.levelTransition>0
        rect(c,171,80,211,27,entering ? 0x2d4775 : 0x062859,14,entering ? 0.94 : 0.91)
        let levelText=entering ? "LEVEL \(game.displayLevel)  ·  NOVI LET" :
            "LEVEL \(game.displayLevel)  ·  \(game.passed)/\(total)"
        if lastLevelBadgeText != levelText {
            lastLevelBadgeText=levelText
            cachedLevelBadge=NSAttributedString(string:levelText,attributes:[
                .font:levelBadgeFont,
                .foregroundColor:UIColor(rgb:entering ? 0xffd76a : 0xffffff)])
        }
        cachedLevelBadge.draw(at:CGPoint(x:277-cachedLevelBadge.size().width/2,y:85))
        progressLabel.draw(at:CGPoint(x:277-progressLabel.size().width/2,y:110))
        let seconds=max(0,Int(ceil(Double(game.magnetTime))))
        let balance=max(0,min(100_000_000,coinWallet()))
        accessibilityLabel="Bopi leti. Novčanik \(balance) kovanica. U ovom letu skupljeno \(game.coins). " +
            "Prolazi ukupno \(passed). Zvjezdice \(game.stars). " +
            "ŠTIT ×\(game.shield). MAGNET \(seconds)s"
    }
    /// Source-matched, state-driven glass counters shared with Android Canvas.
    /// Glossy cyan rims replace flat rectangular HUD paint; no game logic changes.
    private func hudGlass(_ c:CGContext,_ x:CGFloat,_ y:CGFloat,_ w:CGFloat,_ h:CGFloat) {
        rect(c,x,y,w,h,0x062755,24,0.929)
        c.setStrokeColor(color(0x43d8ff,196.0/255))
        c.setLineWidth(2.2)
        c.addPath(CGPath(roundedRect:CGRect(x:x+1.5,y:y+1.5,width:w-3,height:h-3),
                         cornerWidth:22,cornerHeight:22,transform:nil))
        c.strokePath()
        rect(c,x+14,y+5,w-28,3,0xd3faff,2,152.0/255)
        rect(c,x+16,y+h-7,w-32,2,0x36aaff,1,54.0/255)
    }
    private func hudGoldCoin(_ c:CGContext) {
        oval(c,78,30,32,37,0xf9a70e)
        oval(c,81,33,26,31,0xffdf5f)
        oval(c,84,36,20,25,0xec9500)
        hudText("♛",86,33,hudCoinFont,0xfff3a4)
    }
    /// Clip the exact previously selected pilot portrait into the gold avatar ring.
    private func hudPilotPortrait(_ c:CGContext) {
        oval(c,5,12,75,75,0x03d9ff,0.56)
        oval(c,8,15,69,69,0xffc946)
        oval(c,11,18,63,63,0x062e70)
        c.saveGState()
        c.addEllipse(in:CGRect(x:13.5,y:20.5,width:58,height:58))
        c.clip()
        hudPortrait?.draw(in:CGRect(x:7,y:13,width:70,height:70))
        c.restoreGState()
    }
    /// A compact visual string is cached until the actual wallet changes.
    private func walletLabel(_ total:Int)->String {
        if total != lastWalletValue {
            lastWalletValue=total
            if total>=10_000_000 {walletDisplay="\(total/1_000_000)M"}
            else if total>=1_000_000 {walletDisplay=String(format:"%.1fM",Double(total)/1_000_000)}
            else if total>=10_000 {walletDisplay="\(total/1_000)K"}
            else if total>=1_000 {walletDisplay=String(format:"%.1fK",Double(total)/1_000)}
            else {walletDisplay=String(total)}
        }
        return walletDisplay
    }
    /// These source-styled orbs are indicators, not phantom tappable items.
    /// Readouts come only from the live simulation's stars and boosts.
    private func hudOrb(_ c:CGContext,_ cx:CGFloat,_ cy:CGFloat,_ icon:String,
                        _ count:String,_ accent:UInt32,_ enabled:Bool) {
        let rim:UInt32=enabled ? accent : 0x5d83ac
        oval(c,cx-33,cy-33,66,66,0x031630,0.73)
        oval(c,cx-30,cy-30,60,60,rim)
        oval(c,cx-27,cy-27,54,54,0x06417b)
        oval(c,cx-25,cy-24,50,48,enabled ? 0x0d5ba2 : 0x132b4b)
        let symbol=NSAttributedString(string:icon,attributes:[
            .font:UIFont.systemFont(ofSize:33,weight:.black),
            .foregroundColor:UIColor(rgb:enabled ? 0xffffff : 0xa4bdd6)])
        symbol.draw(at:CGPoint(x:cx-symbol.size().width/2,y:cy-symbol.size().height/2))
        let bx=cx+25,by=cy+26
        oval(c,bx-17,by-15,34,30,0x071d45)
        oval(c,bx-14,by-12,28,24,enabled ? 0x125aab : 0x27405f)
        let number=NSAttributedString(string:count,attributes:[
            .font:UIFont.monospacedDigitSystemFont(ofSize:15,weight:.heavy),
            .foregroundColor:UIColor.white])
        number.draw(at:CGPoint(x:bx-number.size().width/2,y:by-number.size().height/2))
    }
    private func drawBoostHUD(_ c:CGContext) {
        // The icon/number hierarchy mirrors Android, leaving the top-right
        // reserved for the real UIKit Pause button.
        // Only saved currency is spendable. Flight coins are a real but
        // separate score/collection counter, not minted wallet currency.
        let balance=max(0,min(100_000_000,coinWallet()))
        hudGlass(c,73,22,72,53)
        hudGoldCoin(c)
        let coinLabel=walletLabel(balance)
        hudText(coinLabel,111,39,coinLabel.count>=4 ? hudWalletCompactFont : hudWalletFont,0xffffff)
        hudPilotPortrait(c)
        rect(c,82,84,61,20,0x052a57,10,0.86)
        hudText("LET +\(game.coins)",84,87,collectedFont,0xffd76a)
        hudOrb(c,46,175,"★","\(game.stars)",0xffce43,true)
        hudOrb(c,46,249,"◆","\(game.shield)",0x54e5ff,game.shield>0)
        let seconds=max(0,Int(ceil(Double(game.magnetTime))))
        hudOrb(c,46,323,"∩","\(seconds)",0x41dfff,game.magnetTime>0)
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
    /// Bevelled crown coin, tied exclusively to the real pickup flags.
    /// Uses Core Graphics primitives, with no frame-time image decoding.
    private func drawRoyalCoin(_ c:CGContext,_ cx:CGFloat,_ cy:CGFloat,_ phase:Float) {
        let pulse:CGFloat=reducedMotion ? 0 : CGFloat(sin(game.time*4.2+phase))*1.4
        let radius:CGFloat=17+pulse
        oval(c,cx-radius-4,cy-radius-4,2*radius+8,2*radius+8,0xffcf53,0.27)
        oval(c,cx-radius,cy-radius,2*radius,2*radius,0xaa5b09)
        oval(c,cx-radius+2,cy-radius+2,2*radius-4,2*radius-4,0xffc333)
        oval(c,cx-radius+5,cy-radius+5,2*radius-10,2*radius-10,0xec8e07)
        oval(c,cx-radius+6,cy-radius+6,2*radius-12,2*radius-12,0xffb91a)
        c.setFillColor(color(0xfff0ad))
        c.beginPath()
        c.move(to:CGPoint(x:cx-8,y:cy-5))
        c.addLine(to:CGPoint(x:cx-4,y:cy))
        c.addLine(to:CGPoint(x:cx,y:cy-8))
        c.addLine(to:CGPoint(x:cx+4,y:cy))
        c.addLine(to:CGPoint(x:cx+8,y:cy-5))
        c.addLine(to:CGPoint(x:cx+6,y:cy+7))
        c.addLine(to:CGPoint(x:cx-6,y:cy+7))
        c.closePath();c.fillPath()
        rect(c,cx-6,cy+5,12,3,0xffffff,1)
        oval(c,cx-radius+4,cy-radius+4,6,5,0xffffff,0.67)
    }
    /// Illustration-only finish marker at the *actual* last gate, never a
    /// fictional transition or additional collision boundary.
    private func drawFinishPortal(_ c:CGContext,_ cx:CGFloat,_ cy:CGFloat,_ phase:Float) {
        let pulse:CGFloat=reducedMotion ? 0 : CGFloat(sin(game.time*2+phase))*1.5
        let radius:CGFloat=30+pulse
        oval(c,cx-radius-4,cy-radius-4,2*radius+8,2*radius+8,0x47cfff,0.20)
        c.setStrokeColor(color(0x164eae,0.75));c.setLineWidth(6)
        c.strokeEllipse(in:CGRect(x:cx-radius,y:cy-radius,width:2*radius,height:2*radius))
        c.setStrokeColor(color(0x45e9ff));c.setLineWidth(3)
        c.strokeEllipse(in:CGRect(x:cx-radius+3.5,y:cy-radius+3.5,
                                  width:2*radius-7,height:2*radius-7))
        c.setStrokeColor(color(0xe7ffff,0.82));c.setLineWidth(1.5)
        c.strokeEllipse(in:CGRect(x:cx-radius+7,y:cy-radius+7,
                                  width:2*radius-14,height:2*radius-14))
        hudText("♛",cx-10,cy-51,hudIconFont,0xffdb63)
    }
    /// Subtle suspended-cliff facets, confined to the existing gate shafts.
    /// Only drawn geometry changes: the BopaviCore opening and collision remain.
    /// Distinctive material facets, always inside the existing collision caps.
    /// Cosmetic geometry is identical to Android; never narrows the flight gap.
    private func drawBiomeGateRelief(_ c:CGContext,_ x:CGFloat,_ w:CGFloat,
                                     _ top:CGFloat,_ bottom:CGFloat,_ world:Int) {
        let accents:[UInt32]=[0x87ef82,0xbd74ff,0xff8b39,0xffd782,
                              0x6ff4ec,0xffe7a0,0x6ceab4,0xbb92ff]
        let glints:[UInt32]=[0xd4ffb0,0x7ef4ff,0xffcf70,0xffefa8,
                             0xffb6e7,0xffffff,0xffa7e7,0x85dfff]
        for side in 0...1 {
            let edge=side==0 ? top : bottom
            let sign:CGFloat=side==0 ? -1 : 1
            for i in 0...2 {
                let left=x+8+CGFloat(i)*(w-27)/3
                let right=min(x+w-7,left+(w-20)/3+6)
                let depth:CGFloat=10+CGFloat(i%2)*6
                c.beginPath()
                c.move(to:CGPoint(x:left,y:edge+sign*5))
                c.addLine(to:CGPoint(x:(left+right)*0.5,y:edge+sign*(depth+6)))
                c.addLine(to:CGPoint(x:right,y:edge+sign*5))
                c.addLine(to:CGPoint(x:(left+right)*0.5,y:edge+sign*2))
                c.closePath()
                c.setFillColor(color(i==1 ? glints[world] : accents[world],
                                     i==1 ? 210.0/255 : 145.0/255))
                c.fillPath()
            }
            let inner=edge+sign*35
            if inner>=20 && inner<=730 {
                if world==2 || world==1 || world==7 {
                    for i in 0...1 {
                        let cx=x+17+CGFloat(i)*(w-32)
                        c.beginPath()
                        c.move(to:CGPoint(x:cx-7,y:inner))
                        c.addLine(to:CGPoint(x:cx+1,y:inner+sign*23))
                        c.addLine(to:CGPoint(x:cx+8,y:inner+sign*3))
                        c.closePath()
                        c.setFillColor(color(i==0 ? glints[world] : accents[world],210.0/255))
                        c.fillPath()
                    }
                } else {
                    for i in 0...2 {
                        let cx=x+13+CGFloat(i)*(w-28)/2
                        oval(c,cx-5,inner-4,10,8,i==1 ? glints[world] : accents[world])
                    }
                }
            }
        }
    }
    private func drawRockFacets(_ c:CGContext,_ x:CGFloat,_ w:CGFloat,
                                _ start:CGFloat,_ end:CGFloat){
        let span=end-start
        guard span>=34 else{return}
        c.setFillColor(color(0xffffff,0.235))
        c.beginPath()
        c.move(to:CGPoint(x:x+4,y:start+6))
        c.addLine(to:CGPoint(x:x+w*0.39,y:start+11))
        c.addLine(to:CGPoint(x:x+w*0.61,y:start+span*0.56))
        c.addLine(to:CGPoint(x:x+w*0.24,y:end-9))
        c.addLine(to:CGPoint(x:x+4,y:end-16))
        c.closePath();c.fillPath()
        c.setFillColor(color(0x081836,0.35))
        c.beginPath()
        c.move(to:CGPoint(x:x+w*0.61,y:start+6))
        c.addLine(to:CGPoint(x:x+w-16,y:start+4))
        c.addLine(to:CGPoint(x:x+w-16,y:end-13))
        c.addLine(to:CGPoint(x:x+w*0.40,y:end-7))
        c.addLine(to:CGPoint(x:x+w*0.61,y:start+span*0.56))
        c.closePath();c.fillPath()
        c.setStrokeColor(color(0xffffff,0.39))
        c.setLineWidth(2)
        c.beginPath()
        c.move(to:CGPoint(x:x+10,y:start+span*0.32))
        c.addLine(to:CGPoint(x:x+w*0.33,y:start+span*0.37))
        c.addLine(to:CGPoint(x:x+w*0.49,y:start+span*0.35))
        c.strokePath()
    }
    private func gate(_ c:CGContext,_ g:BopaviCore.Gate,_ x:CGFloat,_ index:Int,_ incoming:Bool=false){
        let shape=BopaviCore.opening(g,game.time);let top=CGFloat(shape.top),bottom=CGFloat(shape.bottom)
        let w=CGFloat(g.width)
        let hue:UInt32 = g.kind==0 ? 0xad8c71 : pillars[g.kind]
        let shade:UInt32 = g.kind==0 ? 0x6e5c63 : dark[g.kind]
        let cap:UInt32 = g.kind==0 ? 0x55ce6c : hue
        rect(c,x+7,0,w-12,top,shade,7);rect(c,x,0,w-13,top,hue,8)
        rect(c,x-7,top-28,w+12,28,cap,7)
        rect(c,x+7,bottom,w-12,755-bottom,shade,7);rect(c,x,bottom,w-13,755-bottom,hue,7)
        rect(c,x-7,bottom,w+12,27,cap,7)
        drawRockFacets(c,x,w,2,top-30)
        drawRockFacets(c,x,w,bottom+28,751)
        // Responsive cap details never stick outside the simulated collision lip.
        let flecks:[UInt32]=[0xb7ed7a,0x9af7ff,0xffd06e,0xffe29a,
                             0xf8e5cc,0xffe6a1,0xa7ffdf,0xd9baff]
        for j in 0...2 {
            let fx=x+9+(w-22)*CGFloat(j)*0.5
            let inset:CGFloat=j==1 ? 2 : 0
            oval(c,fx,top-17+inset,5,5,flecks[g.kind])
            oval(c,fx,bottom+11+inset,5,5,flecks[g.kind])
        }
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
        drawBiomeGateRelief(c,x,w,top,bottom,g.kind)
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
        let finalGate=incoming ? index==game.upcomingGates.count-1 :
            index==game.level.gates.count-1
        if finalGate {drawFinishPortal(c,mid,center,g.phase)}
        if g.coin && (incoming || game.coinVisible(index)) {
            drawRoyalCoin(c,mid,center,g.phase)
        }
        if g.star && (incoming || game.starVisible(index)) {star(c,mid+35,center-25,12,0xffe25d)}
        if g.power>0 && (incoming || game.powerVisible(index)) {oval(c,mid+26,center+26,26,26,0x1a3c8b);oval(c,mid+34,center+34,10,10,g.power==1 ? 0x63edff : 0xff8ddd)}
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
            let r:CGFloat=48+CGFloat(sin(game.time*5))*2
            c.strokeEllipse(in:CGRect(x:126-r,y:CGFloat(game.y)-r,width:2*r,height:2*r))
        }
        c.saveGState();c.translateBy(x:126,y:CGFloat(game.y))
        // A cached radial cyan/gold light follows the real pilot and flap input.
        // Never scale the sprite or modify the physical collision radius.
        if let aura=birdAuraGradient {
            let auraImpulse:CGFloat=reducedMotion ? 0 : max(0,min(1,CGFloat(game.flapPulse)/0.24))
            let auraScale:CGFloat=1+auraImpulse*0.13
            c.saveGState()
            c.scaleBy(x:auraScale,y:auraScale)
            c.drawRadialGradient(aura,startCenter:.zero,startRadius:0,
                                 endCenter:.zero,endRadius:53,options:[])
            c.restoreGState()
        }
        let angle=CGFloat(min(48,max(-24,game.velocity*0.06))) * .pi / 180
        c.rotate(by:angle)
        let flapStrength:CGFloat = reducedMotion ? 0 : max(0,min(1,CGFloat(game.flapPulse)/0.24))
        let phase:CGFloat = reducedMotion ? 0 : CGFloat(sin(game.time*19))+flapStrength*0.65
        // No whole-body stretching: moving wings provide the animation.
        if let sprite=birdSprite {
            // Real touch adds a crisp 17-degree upstroke that eases away in 240 ms.
            let wingAngle:CGFloat = reducedMotion ? 0 :
                (CGFloat(sin(game.time*19))*23+flapStrength*17) * .pi / 180
            let half=birdArtSize/2
            if let left=leftWing {
                c.saveGState()
                c.translateBy(x:leftWingPivot.x,y:leftWingPivot.y)
                c.rotate(by:wingAngle)
                left.draw(in:CGRect(x:-half-leftWingPivot.x,
                                    y:-half-leftWingPivot.y,
                                    width:birdArtSize,height:birdArtSize))
                c.restoreGState()
            }
            if let right=rightWing {
                c.saveGState()
                c.translateBy(x:rightWingPivot.x,y:rightWingPivot.y)
                c.rotate(by:-wingAngle)
                right.draw(in:CGRect(x:-half-rightWingPivot.x,
                                     y:-half-rightWingPivot.y,
                                     width:birdArtSize,height:birdArtSize))
                c.restoreGState()
            }
            sprite.draw(in:CGRect(x:-half,y:-half,width:birdArtSize,height:birdArtSize))
            c.restoreGState()
            return
        }
        // Asset fallback remains available if resources are missing.
        triangle(c,CGPoint(x:-12,y:11),CGPoint(x:-35-phase*3,y:22),CGPoint(x:-29,y:5),0xef385b)
        oval(c,-30,0,22,16,0x1678d8)
        c.saveGState();c.rotate(by:phase*26 * .pi/180)
        oval(c,-28,-4,36,20,0x0c78dc);oval(c,-25,-6,29,11,0x4ac3ff);c.restoreGState()
        oval(c,-23,-24,49,49,0x095cc8);oval(c,-20,-25,43,48,feather[min(5,skinIndex)])
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
