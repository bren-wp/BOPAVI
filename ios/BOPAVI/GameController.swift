import UIKit
import UniformTypeIdentifiers

/// Premium, self-contained control. The gradient resizes without a new image allocation.
private final class BopaviActionButton: UIButton {
    private let gradient = CAGradientLayer()
    init(primary:Bool,muted:Bool=false) {
        super.init(frame:.zero)
        gradient.colors = primary
            ? [UIColor(red:1.00,green:0.88,blue:0.32,alpha:1).cgColor,
               UIColor(red:1.00,green:0.67,blue:0.09,alpha:1).cgColor,
               UIColor(red:1.00,green:0.46,blue:0.00,alpha:1).cgColor]
            : muted
            ? [UIColor(red:0.57,green:0.72,blue:0.89,alpha:1).cgColor,
               UIColor(red:0.26,green:0.43,blue:0.67,alpha:1).cgColor,
               UIColor(red:0.12,green:0.23,blue:0.39,alpha:1).cgColor]
            : [UIColor(red:0.14,green:0.79,blue:1.00,alpha:1).cgColor,
               UIColor(red:0.03,green:0.49,blue:0.93,alpha:1).cgColor,
               UIColor(red:0.02,green:0.30,blue:0.71,alpha:1).cgColor]
        gradient.startPoint=CGPoint(x:0.5,y:0);gradient.endPoint=CGPoint(x:0.5,y:1)
        layer.insertSublayer(gradient,at:0)
        layer.cornerRadius=29
        layer.borderWidth=primary ? 2 : 1
        layer.borderColor=(primary
            ? UIColor(red:1,green:0.94,blue:0.60,alpha:1)
            : UIColor(red:0.47,green:0.91,blue:1,alpha:1)).cgColor
        layer.shadowColor=(primary
            ? UIColor(red:0.55,green:0.20,blue:0.0,alpha:1)
            : UIColor(red:0.0,green:0.48,blue:0.95,alpha:1)).cgColor
        layer.shadowOpacity=0.40
        layer.shadowRadius=9
        layer.shadowOffset=CGSize(width:0,height:4)
        setTitleColor(.white,for:.normal)
        let face=UIFont.systemFont(ofSize:18,weight:.heavy)
        titleLabel?.font=UIFont(descriptor:face.fontDescriptor.withDesign(.rounded)
            ?? face.fontDescriptor,size:18)
        titleLabel?.adjustsFontSizeToFitWidth=true
        titleLabel?.minimumScaleFactor=0.72
        titleLabel?.numberOfLines=1
        contentEdgeInsets=UIEdgeInsets(top:8,left:12,bottom:8,right:12)
        accessibilityTraits.insert(.button)
    }
    required init?(coder:NSCoder){fatalError("Use programmatic initialization")}
    override func layoutSubviews(){
        super.layoutSubviews()
        gradient.frame=bounds
        gradient.cornerRadius=29
        layer.shadowPath=UIBezierPath(roundedRect:bounds,cornerRadius:29).cgPath
    }
}

/// Gradient-backed Settings sheet; reflows with Dynamic Type and safe areas.
private final class BopaviSettingsPanel: UIStackView {
    private let fill=CAGradientLayer()
    override init(frame:CGRect) {
        super.init(frame:frame)
        axis = .vertical
        alignment = .fill
        spacing = 10
        isLayoutMarginsRelativeArrangement = true
        layoutMargins = UIEdgeInsets(top:14,left:13,bottom:18,right:13)
        fill.colors = [
            UIColor(red:0.024,green:0.196,blue:0.431,alpha:0.97).cgColor,
            UIColor(red:0.016,green:0.086,blue:0.192,alpha:0.99).cgColor
        ]
        fill.startPoint=CGPoint(x:0.5,y:0)
        fill.endPoint=CGPoint(x:0.5,y:1)
        fill.cornerRadius=27
        layer.insertSublayer(fill,at:0)
        layer.cornerRadius=27
        layer.borderWidth=2
        layer.borderColor=UIColor(red:0.259,green:0.812,blue:1,alpha:1).cgColor
        layer.shadowColor=UIColor(red:0,green:0.3,blue:0.8,alpha:1).cgColor
        layer.shadowOpacity=0.46
        layer.shadowRadius=12
        layer.shadowOffset=CGSize(width:0,height:6)
    }
    required init(coder:NSCoder){fatalError("Use programmatic initialization")}
    override func layoutSubviews(){
        super.layoutSubviews()
        fill.frame=bounds
        layer.shadowPath=UIBezierPath(roundedRect:bounds,cornerRadius:27).cgPath
    }
}

/** Native UIKit menus and Core Graphics gameplay, no WKWebView. */
final class GameController: UIViewController {
    private let progress=ProgressStore()
    private let sound=Soundscape()
    private let collectHaptic=UISelectionFeedbackGenerator()
    private let shieldHaptic=UIImpactFeedbackGenerator(style:.medium)
    private var canvas:GameCanvas?
    private weak var pauseFitViewport:UIScrollView?
    private weak var pauseFitPanel:UIStackView?
    private weak var resultFitViewport:UIScrollView?
    private weak var resultFitPanel:UIStackView?
    private var gameWorld=0
    private var gameNumber=1
    private var backgroundGradient:CAGradientLayer?
    override var preferredStatusBarStyle:UIStatusBarStyle {.lightContent}
    // Full-bleed menus retain UIKit safe-area protection for device cutouts.
    override var prefersStatusBarHidden:Bool { true }
    override var prefersHomeIndicatorAutoHidden:Bool { canvas != nil }
    override func viewDidLoad(){
        super.viewDidLoad()
        NotificationCenter.default.addObserver(self,selector:#selector(pauseForInterruption(_:)),
            name:UIApplication.willResignActiveNotification,object:nil)
        NotificationCenter.default.addObserver(self,selector:#selector(resumeIdlePreview(_:)),
            name:UIApplication.didBecomeActiveNotification,object:nil)
        sound.enabled=progress.soundEnabled
        sound.musicVolume=Float(progress.musicVolume)/100
        sound.effectsVolume=Float(progress.effectsVolume)/100
        // iOS already displays the branded native LaunchArt while UIKit loads.
        // The old extra runtime splash was replaced on the very next frame,
        // producing the duplicate logo/loading flash reported on real phones.
        // There is no asynchronous data to await here; create Home once.
        showHome()
    }
    deinit { NotificationCenter.default.removeObserver(self) }
    @objc private func pauseForInterruption(_ note:Notification) {
        // Keep an active flight exactly where the user left it on call, lock or app switch.
        // Never auto-resume physics or audio in the background.
        guard let activeCanvas=canvas else {return}
        if activeCanvas.game.active {activeCanvas.paused=true}
        sound.pause()
    }
    @objc private func resumeIdlePreview(_ note:Notification) {
        // Idle preview has no pause button; restore its soundtrack on return.
        if let activeCanvas=canvas, !activeCanvas.game.active && !activeCanvas.paused {
            sound.resume()
        }
    }
    private func clear() {
        canvas?.stop();canvas=nil;sound.stop()
        setNeedsStatusBarAppearanceUpdate()
        backgroundGradient=nil
        view.subviews.forEach{$0.removeFromSuperview()}
        view.layer.sublayers?.forEach{$0.removeFromSuperlayer()}
        view.backgroundColor=UIColor(red:0.03,green:0.10,blue:0.24,alpha:1)
    }
    // Menus share the illustrated backgrounds and gold / cyan / navy palette.
    // Gameplay and persistence remain independent of these presentation views.
    private func ui(_ text:String)->String {GameLanguages.text(text,progress.languageCode)}
    /// Fixed-size 2×4 paged native picker: no action-sheet scrolling,
    /// no external language libraries, no account or network access.
    private func startSceneryDrift(_ backdrop:UIImageView) {
        guard !progress.lessMotion && !UIAccessibility.isReduceMotionEnabled else {return}
        let drift=CABasicAnimation(keyPath:"transform.scale")
        drift.fromValue=1.015
        drift.toValue=1.045
        drift.duration=12.5
        drift.autoreverses=true
        drift.repeatCount = .infinity
        drift.timingFunction=CAMediaTimingFunction(name:.easeInEaseOut)
        backdrop.layer.add(drift,forKey:"scenery-breathing")
    }
    /** Only the simplified Home uses a menu host. No hidden screens or old navigation. */
    private func menu()->UIStackView {
        clear()
        let viewport=UIScrollView()
        viewport.translatesAutoresizingMaskIntoConstraints=false
        viewport.isScrollEnabled=false
        viewport.alwaysBounceVertical=false
        viewport.showsVerticalScrollIndicator=false
        view.addSubview(viewport)
        NSLayoutConstraint.activate([
            viewport.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            viewport.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            viewport.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            viewport.trailingAnchor.constraint(equalTo:view.trailingAnchor)
        ])
        let stack=UIStackView()
        stack.axis = .vertical
        stack.alignment = .fill
        stack.spacing=7
        stack.isLayoutMarginsRelativeArrangement=true
        stack.layoutMargins=UIEdgeInsets(top:12,left:18,bottom:12,right:18)
        stack.translatesAutoresizingMaskIntoConstraints=false
        viewport.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo:viewport.contentLayoutGuide.topAnchor),
            stack.bottomAnchor.constraint(equalTo:viewport.contentLayoutGuide.bottomAnchor),
            stack.leadingAnchor.constraint(equalTo:viewport.contentLayoutGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo:viewport.contentLayoutGuide.trailingAnchor),
            stack.widthAnchor.constraint(equalTo:viewport.frameLayoutGuide.widthAnchor)
        ])
        return stack
    }
    override func viewDidLayoutSubviews(){
        super.viewDidLayoutSubviews()
        backgroundGradient?.frame=view.bounds
        // Apply the same non-scrolling safe-area fit to menus, pause and result.
        // Each remains a real UIKit interface with functioning native buttons.
        func fit(_ scroll:UIScrollView,_ panel:UIStackView) {
            guard !scroll.isScrollEnabled, scroll.bounds.height>0,
                  panel.bounds.height>0 else{return}
            let naturalHeight=panel.bounds.height
            let factor=min(CGFloat(1),scroll.bounds.height/naturalHeight)
            let shift = -(naturalHeight * (1-factor) / 2)
            let desired=CGAffineTransform(scaleX:factor,y:factor)
                .translatedBy(x:0,y:factor>0 ? shift/factor : 0)
            if panel.transform != desired {panel.transform=desired}
        }
        if let scroll=view.subviews.compactMap({$0 as? UIScrollView}).first,
           let stack=scroll.subviews.compactMap({$0 as? UIStackView}).first {
            fit(scroll,stack)
        }
        if let scroll=pauseFitViewport,let panel=pauseFitPanel {
            fit(scroll,panel)
        }
        if let scroll=resultFitViewport,let panel=resultFitPanel {
            fit(scroll,panel)
        }
    }
    @discardableResult private func label(_ text:String,_ size:CGFloat,_ color:UIColor,_ into:UIStackView)->UILabel {
        let l=UILabel()
        l.numberOfLines=0;l.textAlignment = .center;l.text=ui(text)
        let font=UIFont.systemFont(ofSize:size,weight:size>=26 ? .black : .semibold)
        l.font=UIFont(descriptor:font.fontDescriptor.withDesign(.rounded) ?? font.fontDescriptor,size:size)
        l.adjustsFontForContentSizeCategory=true
        l.textColor=color
        l.setContentCompressionResistancePriority(.required,for:.vertical);into.addArrangedSubview(l)
        return l
    }
    private func button(_ title:String,in stack:UIStackView,primary:Bool=true,muted:Bool=false,action:@escaping()->Void){
        let b=BopaviActionButton(primary:primary,muted:muted)
        b.setTitle(ui(title),for:.normal)
        b.accessibilityLabel=ui(title)
        b.heightAnchor.constraint(greaterThanOrEqualToConstant:primary ? 68 : 58).isActive=true
        b.addAction(UIAction{_ in self.sound.effect("click");action()},for:.touchUpInside)
        stack.addArrangedSubview(b)
    }
    private func showHome(){
        let stack=menu()
        view.backgroundColor=UIColor(red:0.04,green:0.45,blue:0.79,alpha:1)
        if let artwork=UIImage(named:"HomeArt"){
            let backdrop=UIImageView(image:artwork)
            backdrop.translatesAutoresizingMaskIntoConstraints=false
            backdrop.contentMode = .scaleAspectFill
            backdrop.clipsToBounds=true
            backdrop.isUserInteractionEnabled=false
            backdrop.accessibilityElementsHidden=true
            startSceneryDrift(backdrop)
            view.insertSubview(backdrop,at:0)
            NSLayoutConstraint.activate([
                backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                backdrop.topAnchor.constraint(equalTo:view.topAnchor),
                backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
        }
        if let viewport=view.subviews.compactMap({$0 as? UIScrollView}).first {
            stack.heightAnchor.constraint(greaterThanOrEqualTo:viewport.frameLayoutGuide.heightAnchor).isActive=true
        }
        // One real Play action. No shop/profile/world/level navigation.
        // The mascot and logo remain separate runtime assets for real layout.
        let logo=UIImageView(image:UIImage(named:"Logo"))
        logo.contentMode = .scaleAspectFit
        logo.isAccessibilityElement=false
        logo.heightAnchor.constraint(equalToConstant:156).isActive=true
        stack.addArrangedSubview(logo)
        let pilot=UIImageView(image:UIImage(named:"BopiPortrait0"))
        pilot.contentMode = .scaleAspectFit
        pilot.isAccessibilityElement=false
        pilot.heightAnchor.constraint(equalToConstant:325).isActive=true
        stack.addArrangedSubview(pilot)
        let spacer=UIView()
        spacer.setContentHuggingPriority(.defaultLow,for:.vertical)
        stack.addArrangedSubview(spacer)
        let subtitle=label(progress.languageCode=="hr" ? "MALI LETOVI · VELIKE PRIČE" :
            "SMALL FLIGHTS · BIG STORIES",16,.white,stack)
        subtitle.accessibilityLabel=subtitle.text
        button(progress.languageCode=="hr" ? "▶  IGRAJ" : "▶  PLAY",in:stack){
            self.sound.effect("click")
            self.startGame(0,1)
        }
    }

    private func showPremiumPause(for canvas:GameCanvas){
        let overlay=UIView()
        overlay.translatesAutoresizingMaskIntoConstraints=false
        overlay.backgroundColor=UIColor(red:0,green:0.055,blue:0.14,alpha:0.70)
        view.addSubview(overlay)
        NSLayoutConstraint.activate([
            overlay.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            overlay.trailingAnchor.constraint(equalTo:view.trailingAnchor),
            overlay.topAnchor.constraint(equalTo:view.topAnchor),
            overlay.bottomAnchor.constraint(equalTo:view.bottomAnchor)
        ])
        let scroll=UIScrollView()
        scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.showsVerticalScrollIndicator=false
        scroll.alwaysBounceVertical=false
        scroll.isScrollEnabled=false
        pauseFitViewport=scroll
        overlay.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:overlay.safeAreaLayoutGuide.topAnchor,constant:5),
            scroll.bottomAnchor.constraint(equalTo:overlay.safeAreaLayoutGuide.bottomAnchor,constant:-5),
            scroll.leadingAnchor.constraint(equalTo:overlay.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:overlay.trailingAnchor)
        ])
        let panel=UIStackView()
        panel.axis = .vertical
        panel.alignment = .fill
        panel.spacing=9
        panel.translatesAutoresizingMaskIntoConstraints=false
        panel.isLayoutMarginsRelativeArrangement=true
        panel.layoutMargins=UIEdgeInsets(top:15,left:17,bottom:20,right:17)
        pauseFitPanel=panel
        panel.backgroundColor=UIColor(red:0.015,green:0.16,blue:0.37,alpha:0.98)
        panel.layer.cornerRadius=27
        panel.layer.borderWidth=3
        panel.layer.borderColor=UIColor(red:0.12,green:0.83,blue:1,alpha:1).cgColor
        scroll.addSubview(panel)
        NSLayoutConstraint.activate([
            panel.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor,constant:13),
            panel.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor,constant:-13),
            panel.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor,constant:8),
            panel.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor,constant:-8),
            panel.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor,constant:-26)
        ])
        let mascot=UIImageView(image:UIImage(named:"BopiPortrait0"))
        mascot.contentMode = .scaleAspectFit
        mascot.isAccessibilityElement=false
        mascot.heightAnchor.constraint(equalToConstant:154).isActive=true
        panel.addArrangedSubview(mascot)
        label(progress.languageCode=="hr" ? "♛  PAUZA" : "♛  PAUSE",33,UIColor.white,panel)
        label(progress.languageCode=="hr" ? "Let je pauziran" : "Flight paused",14,
              UIColor(red:0.75,green:0.90,blue:1,alpha:1),panel)
        let close:(Bool)->Void = {[weak overlay,weak canvas,weak self] resume in
            overlay?.removeFromSuperview()
            if resume {canvas?.paused=false;self?.sound.resume()}
        }
        func command(_ caption:String,style:Int,handler:@escaping()->Void){
            let control:UIButton
            if style==2 {
                let exit=UIButton(type:.system)
                exit.backgroundColor=UIColor(red:0.22,green:0.34,blue:0.57,alpha:1)
                exit.layer.cornerRadius=26
                exit.layer.borderWidth=2
                exit.layer.borderColor=UIColor(red:0.65,green:0.77,blue:0.93,alpha:1).cgColor
                control=exit
            } else {
                control=BopaviActionButton(primary:style==0)
            }
            control.setTitle(self.ui(caption),for:.normal)
            control.setTitleColor(.white,for:.normal)
            control.titleLabel?.font=UIFont.systemFont(ofSize:18,weight:.heavy)
            control.titleLabel?.adjustsFontSizeToFitWidth=true
            control.titleLabel?.minimumScaleFactor=0.7
            control.accessibilityLabel=self.ui(caption)
            control.heightAnchor.constraint(equalToConstant:59).isActive=true
            control.addAction(UIAction{_ in self.sound.effect("click");handler()},for:.touchUpInside)
            panel.addArrangedSubview(control)
        }
        command(progress.languageCode=="hr" ? "▶  NASTAVI" : "▶  CONTINUE",style:0){close(true)}
        command(progress.languageCode=="hr" ? "↻  PONOVO" : "↻  RETRY",style:1){
            close(false);self.startGame(self.gameWorld,self.gameNumber)
        }
        command(progress.languageCode=="hr" ? "↪  IZLAZ" : "↪  EXIT",style:2){close(false);self.showHome()}
        // No separate settings panel in the streamlined experience.
        // Pause only exposes Continue, Retry and Exit.
        UIAccessibility.post(notification:.screenChanged,argument:panel)
    }
    private func startGame(_ world:Int,_ number:Int){
        clear();gameWorld=world;gameNumber=number
        let game=GameSimulation(BopaviCore.createStream(0,1),endless:true,initialShield:0,initialMagnet:0,difficulty:1,initialOrdinal:1)
        sound.startWorld(world)
        let gameCanvas=GameCanvas(game:game,reducedMotion:progress.lessMotion,skinIndex:0,
            languageCode:progress.languageCode)
        canvas=gameCanvas
        setNeedsStatusBarAppearanceUpdate()
        gameCanvas.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(gameCanvas)
        NSLayoutConstraint.activate([gameCanvas.topAnchor.constraint(equalTo:view.topAnchor),gameCanvas.bottomAnchor.constraint(equalTo:view.bottomAnchor),gameCanvas.leadingAnchor.constraint(equalTo:view.leadingAnchor),gameCanvas.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        gameCanvas.onFinished = { [weak self,weak gameCanvas] g in
            guard let self=self, self.canvas === gameCanvas else{return}
            self.showResult(g)
        }
        gameCanvas.onFlap = { [weak self] in self?.sound.effect("tap") }
        gameCanvas.onCollect = { [weak self] in
            self?.sound.effect("collect")
            if self?.progress.hapticEnabled == true {self?.collectHaptic.selectionChanged()}
        }
        gameCanvas.onShieldImpact = { [weak self] in
            guard let self=self else {return}
            self.sound.effect("hit")
            if self.progress.hapticEnabled {self.shieldHaptic.impactOccurred()}
        }
        // Gameplay counts now share one frame-synchronous Core Graphics HUD
        // on Android and iOS; avoid a duplicate UIKit chip over the scene.
        let pause=UIButton(type:.system);pause.setTitle("Ⅱ",for:.normal);pause.titleLabel?.font=UIFont.boldSystemFont(ofSize:24)
        pause.backgroundColor=UIColor(red:0.11,green:0.24,blue:0.41,alpha:0.96);pause.setTitleColor(.white,for:.normal)
        pause.layer.cornerRadius=16;pause.layer.borderWidth=1
        pause.layer.borderColor=UIColor.white.withAlphaComponent(0.3).cgColor
        pause.accessibilityLabel="Izbornik tijekom igre"
        // Keep pause out of the idle flight preview and VoiceOver tree.
        pause.isHidden=true
        pause.translatesAutoresizingMaskIntoConstraints=false;view.addSubview(pause)
        gameCanvas.onFlightStarted = { [weak self,weak gameCanvas,weak pause] in
            guard let self=self, self.canvas === gameCanvas else{return}
            // The pre-flight preview never spends purchased equipment.
            pause?.isHidden=false
        }
        NSLayoutConstraint.activate([pause.trailingAnchor.constraint(equalTo:view.trailingAnchor,constant:-15),pause.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:12),pause.heightAnchor.constraint(equalToConstant:48),pause.widthAnchor.constraint(equalToConstant:58)])
        pause.addAction(UIAction{[weak self,weak gameCanvas] _ in
            guard let self=self,let canvas=gameCanvas,
                  self.canvas === canvas,canvas.game.active && !canvas.game.finished else{return}
            canvas.paused=true;self.sound.pause()
            self.showPremiumPause(for:canvas)

        },for:.touchUpInside)
    }
    private func showResult(_ g:GameSimulation){
        let previousRecord=progress.bestPoints()
        let newRecord=g.score()>previousRecord && g.score()>0
        sound.effect(g.won ? "level" : "hit")
        let headline=progress.languageCode == "hr" ? "REZULTAT" : "RESULT"
        progress.recordRun(g)
        gameNumber=g.displayLevel
        clear()
        view.backgroundColor=UIColor(red:0.09,green:0.39,blue:0.73,alpha:1)
        if let image=UIImage(named:"World\(gameWorld)") {
            let backdrop=UIImageView(image:image)
            backdrop.contentMode = .scaleAspectFill
            backdrop.clipsToBounds=true;backdrop.translatesAutoresizingMaskIntoConstraints=false
            backdrop.isAccessibilityElement=false;view.addSubview(backdrop)
            NSLayoutConstraint.activate([
                backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                backdrop.topAnchor.constraint(equalTo:view.topAnchor),
                backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
        }
        let dim=UIView()
        dim.backgroundColor=UIColor(red:0.01,green:0.08,blue:0.21,alpha:0.34)
        dim.translatesAutoresizingMaskIntoConstraints=false
        view.addSubview(dim)
        NSLayoutConstraint.activate([
            dim.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            dim.trailingAnchor.constraint(equalTo:view.trailingAnchor),
            dim.topAnchor.constraint(equalTo:view.topAnchor),
            dim.bottomAnchor.constraint(equalTo:view.bottomAnchor)
        ])
        let scroll=UIScrollView();scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.showsVerticalScrollIndicator=false
        scroll.alwaysBounceVertical=false
        scroll.isScrollEnabled=false
        resultFitViewport=scroll
        view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:view.trailingAnchor)
        ])
        let stack=UIStackView()
        resultFitPanel=stack
        stack.axis = .vertical;stack.alignment = .fill;stack.spacing=7
        stack.isLayoutMarginsRelativeArrangement=true
        stack.layoutMargins=UIEdgeInsets(top:12,left:18,bottom:18,right:18)
        stack.translatesAutoresizingMaskIntoConstraints=false
        scroll.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor),
            stack.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor),
            stack.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor),
            stack.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor),
            stack.heightAnchor.constraint(greaterThanOrEqualTo:scroll.frameLayoutGuide.heightAnchor)
        ])
        // Same live visual composition as Android: real selected pilot and
        // world image, not a mocked result screenshot or static generic hero.
        let pilotIndex=0
        let header=UIView()
        header.clipsToBounds=true
        header.layer.cornerRadius=22
        header.layer.borderWidth=2
        header.layer.borderColor=UIColor(red:0.34,green:0.81,blue:1,alpha:1).cgColor
        header.heightAnchor.constraint(equalToConstant:174).isActive=true
        header.isAccessibilityElement=true
        header.accessibilityLabel="Odabrani letač: \(progress.skinNames[pilotIndex]), svijet \(BopaviCore.names[gameWorld])"
        stack.addArrangedSubview(header)
        let scene=UIImageView(image:UIImage(named:"World\(gameWorld)"))
        scene.translatesAutoresizingMaskIntoConstraints=false
        scene.contentMode = .scaleAspectFill
        scene.clipsToBounds=true
        scene.isAccessibilityElement=false
        header.addSubview(scene)
        let fade=UIView()
        fade.translatesAutoresizingMaskIntoConstraints=false
        fade.backgroundColor=UIColor(red:0.02,green:0.09,blue:0.26,alpha:0.32)
        fade.isAccessibilityElement=false
        header.addSubview(fade)
        let pilot=UIImageView(image:UIImage(named:"BopiPortrait\(pilotIndex)"))
        pilot.translatesAutoresizingMaskIntoConstraints=false
        pilot.contentMode = .scaleAspectFit
        pilot.isAccessibilityElement=false
        header.addSubview(pilot)
        NSLayoutConstraint.activate([
            scene.leadingAnchor.constraint(equalTo:header.leadingAnchor),
            scene.trailingAnchor.constraint(equalTo:header.trailingAnchor),
            scene.topAnchor.constraint(equalTo:header.topAnchor),
            scene.bottomAnchor.constraint(equalTo:header.bottomAnchor),
            fade.leadingAnchor.constraint(equalTo:header.leadingAnchor),
            fade.trailingAnchor.constraint(equalTo:header.trailingAnchor),
            fade.topAnchor.constraint(equalTo:header.topAnchor),
            fade.bottomAnchor.constraint(equalTo:header.bottomAnchor),
            pilot.leadingAnchor.constraint(equalTo:header.leadingAnchor,constant:6),
            pilot.trailingAnchor.constraint(equalTo:header.trailingAnchor,constant:-6),
            pilot.centerYAnchor.constraint(equalTo:header.centerYAnchor),
            pilot.heightAnchor.constraint(equalToConstant:170)
        ])
        label(headline,31,UIColor(red:1,green:0.86,blue:0.38,alpha:1),stack)
        if newRecord {
            let banner=UILabel()
            banner.text=ui("♛  NOVI REKORD!")
            banner.textColor=UIColor(red:1,green:0.93,blue:0.67,alpha:1)
            banner.textAlignment = .center
            banner.font=UIFont.systemFont(ofSize:19,weight:.heavy)
            banner.backgroundColor=UIColor(red:0.42,green:0.22,blue:0.04,alpha:0.96)
            banner.layer.cornerRadius=20
            banner.layer.borderWidth=2
            banner.layer.borderColor=UIColor(red:1,green:0.79,blue:0.26,alpha:1).cgColor
            banner.clipsToBounds=true
            banner.heightAnchor.constraint(equalToConstant:46).isActive=true
            banner.accessibilityLabel=ui("Novi osobni rekord: \(g.score()) bodova")
            stack.addArrangedSubview(banner)
        }
        let sky=UIColor(red:0.67,green:0.92,blue:1,alpha:1)
        let gold=UIColor(red:1,green:0.82,blue:0.28,alpha:1)
        let summary=UIStackView()
        summary.axis = .horizontal;summary.alignment = .fill
        summary.distribution = .fillEqually;summary.spacing=6
        stack.addArrangedSubview(summary)
        // All summary values are derived from the real finished GameSimulation.
        func resultCard(_ caption:String,icon:String,value:String,highlight:UIColor)->UILabel {
            let card=UIStackView()
            card.axis = .vertical;card.alignment = .fill;card.spacing=3
            card.isLayoutMarginsRelativeArrangement=true
            card.layoutMargins=UIEdgeInsets(top:8,left:3,bottom:9,right:3)
            card.backgroundColor=UIColor(red:0.025,green:0.15,blue:0.34,alpha:0.97)
            card.layer.cornerRadius=19;card.layer.borderWidth=2
            card.layer.borderColor=UIColor(red:0.22,green:0.79,blue:1,alpha:1).cgColor
            card.heightAnchor.constraint(equalToConstant:116).isActive=true
            summary.addArrangedSubview(card)
            let iconView=label(icon,29,gold,card)
            iconView.heightAnchor.constraint(equalToConstant:37).isActive=true
            iconView.isAccessibilityElement=false
            let title=label(caption,11,.white,card)
            title.numberOfLines=2;title.adjustsFontSizeToFitWidth=true
            title.minimumScaleFactor=0.80
            title.heightAnchor.constraint(equalToConstant:34).isActive=true
            title.isAccessibilityElement=false
            let amount=label(value,23,highlight,card)
            amount.adjustsFontSizeToFitWidth=true
            amount.minimumScaleFactor=0.67
            amount.accessibilityLabel=ui("\(caption) \(value)")
            amount.heightAnchor.constraint(equalToConstant:32).isActive=true
            return amount
        }
        _=resultCard("REZULTAT",icon:"★",value:String(g.score()),highlight:gold)
        _=resultCard(progress.languageCode=="hr" ? "HRANA" : "FOOD",
            icon:"🍓",value:String(g.foodCollected),highlight:sky)
        let best=label(progress.languageCode=="hr" ?
            "★  NAJBOLJI REZULTAT: \(progress.bestPoints())" :
            "★  BEST SCORE: \(progress.bestPoints())",18,.white,stack)
        best.backgroundColor=UIColor(red:0.025,green:0.16,blue:0.37,alpha:0.98)
        best.layer.cornerRadius=20
        best.layer.borderWidth=2
        best.layer.borderColor=UIColor(red:0.22,green:0.80,blue:1,alpha:1).cgColor
        best.heightAnchor.constraint(equalToConstant:60).isActive=true
        button(progress.languageCode=="hr" ? "▶  PONOVO" : "▶  RETRY",in:stack){
            self.startGame(0,1)
        }
        button(progress.languageCode=="hr" ? "⌂  POČETNA" : "⌂  HOME",
               in:stack,primary:false){self.showHome()}
    }

}
