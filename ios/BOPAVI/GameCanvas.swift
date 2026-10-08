import UIKit

/// Native Core Graphics + CADisplayLink renderer. No WKWebView, JS or network.
final class GameCanvas: UIView {
    let game: GameSimulation
    let reducedMotion: Bool
    let skinIndex: Int
    private let pickupHues:[UInt32]=[0xffe25d,0xffca91,0xb4f7ff,0xff9a46,0xf8f4b6,0xb4a5e9,0x8ffff1,0xd7bbff]
    private lazy var pickupSymbol:NSAttributedString=NSAttributedString(string:BopaviCore.collectibleIcons[game.level.world],attributes:[.font:UIFont.systemFont(ofSize:19,weight:.heavy),.foregroundColor:UIColor.white])
    private let birdSprite:UIImage?
    private let feather:[UInt32]=[0x39b5fc,0xffc73e,0xff6883,0x9e86f6,0x45daad,0x6676a8]
    var onFinished: ((GameSimulation) -> Void)?
    var onLevelComplete: ((Int)->Void)?
    var onFlap:(()->Void)?
    var onCollect:(()->Void)?
    private var completedSeen=0
    private var pickupSeen=0
    var onHUDUpdate: ((GameSimulation) -> Void)?
    private var lastHUD:CFTimeInterval = 0
    private var link: CADisplayLink?
    private var previous: CFTimeInterval = 0
    private var reported = false
    var paused = false { didSet { previous = 0 } }
    private let skyA: [UInt32] = [0x159df7,0x18b5e7,0x418ddc,0x6e287e,0x45aaf6,0x131a4b,0x123969,0x0b123f]
    private let skyB: [UInt32] = [0xd0f8ff,0xffe3b2,0xedfbff,0xffa36d,0xffe9b6,0x7461bc,0x60f6d5,0x5955a9]
    private let pillars: [UInt32] = [0x20b96c,0xf5a65b,0x8ad8f5,0xe65b35,0xe9d9b5,0x57459a,0x5fdddc,0x7973f3]
    private lazy var cachedGradient:CGGradient? = {
        let pair=[UIColor(rgb:skyA[game.level.world]).cgColor,UIColor(rgb:skyB[game.level.world]).cgColor] as CFArray
        return CGGradient(colorsSpace:CGColorSpaceCreateDeviceRGB(),colors:pair,locations:[0,1])
    }()
    private let dark: [UInt32] = [0x096c46,0xbd7153,0x4282ad,0x912f35,0x9d8d80,0x241b60,0x247b9b,0x373192]
    init(game:GameSimulation, reducedMotion:Bool,skinIndex:Int) {
        self.game=game;self.reducedMotion=reducedMotion;self.skinIndex=min(5,max(0,skinIndex))
        self.birdSprite=UIImage(named:"Bopi\(min(5,max(0,skinIndex)))")
        super.init(frame:.zero)
        isOpaque=true; contentMode = .redraw; isMultipleTouchEnabled=false
        accessibilityLabel="Dodirni za let Bopija"
        backgroundColor=UIColor(rgb:skyA[game.level.world])
    }
    required init?(coder:NSCoder){fatalError("Use programmatic initialization")}
    override func didMoveToWindow(){super.didMoveToWindow();if window != nil {start()} else {stop()}}
    private func start(){guard link == nil else{return};let l=CADisplayLink(target:self,selector:#selector(frameTick(_:)));l.preferredFramesPerSecond=reducedMotion ? 30 : 60;l.add(to:.main,forMode:.common);link=l}
    func stop(){link?.invalidate();link=nil;previous=0}
    @objc private func frameTick(_ l:CADisplayLink){
        if paused {previous=0;return}
        if !paused && !game.finished {if previous != 0 {game.step(Float(l.timestamp-previous))};previous=l.timestamp}
        else {previous=0}
        if game.completionCount>completedSeen {
            completedSeen=game.completionCount;onLevelComplete?(game.completedOrdinal)
        }
        if game.coins+game.stars>pickupSeen {pickupSeen=game.coins+game.stars;onCollect?()}
        setNeedsDisplay()
        if l.timestamp-lastHUD > 0.35 {lastHUD=l.timestamp;onHUDUpdate?(game)}
        if game.finished && !reported {
            reported=true;stop()
            DispatchQueue.main.async { [weak self] in guard let self=self else{return};if self.window != nil {self.onFinished?(self.game)} }
        }
    }
    override func touchesBegan(_ touches:Set<UITouch>,with event:UIEvent?) {super.touchesBegan(touches,with:event);if !paused && !game.finished {game.flap();onFlap?()}}
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
        background(c)
        for (i,g) in game.level.gates.enumerated() {
            let x=CGFloat(g.x-game.distance)
            if x < -100 || x > 550 {continue}
            gate(c,g,x,i)
        }
        bird(c)
        c.restoreGState()
        let groundTop=(bound.height-800*s)/2+751*s
        if groundTop<bound.height {
            rect(c,0,groundTop,bound.width,bound.height-groundTop,
                game.level.world == 5 || game.level.world == 7 ? 0x171f53 : 0x64c881)
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
        if !reducedMotion {
            for i in 1...3 {
                let x:CGFloat = 126-CGFloat(i)*19-16
                let y:CGFloat = CGFloat(game.y)+8+CGFloat(sin(game.time*9-Float(i)))*4
                oval(c,x,y,18-CGFloat(i)*3,8-CGFloat(i)*1.5,0x95eaff,0.34)
            }
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
        let phase:CGFloat = reducedMotion ? 0 : CGFloat(sin(game.time*19))
        c.scaleBy(x:1,y:1+phase*0.035)
        if let sprite=birdSprite {
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
