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
final class GameController: UIViewController, UIDocumentPickerDelegate {
    private let progress=ProgressStore()
    private let sound=Soundscape()
    private let collectHaptic=UISelectionFeedbackGenerator()
    private let shieldHaptic=UIImpactFeedbackGenerator(style:.medium)
    private var canvas:GameCanvas?
    private var currentMenuName="BOPAVI"
    private weak var pauseFitViewport:UIScrollView?
    private weak var pauseFitPanel:UIStackView?
    private weak var resultFitViewport:UIScrollView?
    private weak var resultFitPanel:UIStackView?
    private var gameWorld=0
    private var gameNumber=1
    private var backgroundGradient:CAGradientLayer?
    private let worldAccents:[UIColor] = [
        UIColor(red:0.23,green:0.83,blue:0.56,alpha:1), UIColor(red:0.50,green:0.92,blue:1,alpha:1),
        UIColor(red:1,green:0.51,blue:0.39,alpha:1), UIColor(red:0.72,green:0.60,blue:0.98,alpha:1),
        UIColor(red:0.39,green:0.91,blue:0.88,alpha:1), UIColor(red:1,green:0.85,blue:0.57,alpha:1),
        UIColor(red:0.70,green:0.93,blue:0.45,alpha:1), UIColor(red:0.74,green:0.69,blue:1,alpha:1)
    ]
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
    private func menu(_ title:String,_ subtitle:String)->UIStackView {
        clear()
        currentMenuName=title
        if title != "BOPAVI" {
            let world=gameWorld >= 0 && gameWorld < 8 ? gameWorld : 0
            let backdrop=UIImageView(image:UIImage(named:"World\(world)"))
            backdrop.translatesAutoresizingMaskIntoConstraints=false
            backdrop.contentMode = .scaleAspectFill
            backdrop.clipsToBounds=true
            backdrop.isAccessibilityElement=false
            startSceneryDrift(backdrop)
            view.addSubview(backdrop)
            NSLayoutConstraint.activate([
                backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                backdrop.topAnchor.constraint(equalTo:view.topAnchor),
                backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
            let shade=UIView()
            // Brighter painted biomes; dark real controls retain their contrast.
            shade.backgroundColor=UIColor(red:0.012,green:0.08,blue:0.19,alpha:0.18)
            shade.translatesAutoresizingMaskIntoConstraints=false
            view.addSubview(shade)
            NSLayoutConstraint.activate([
                shade.leadingAnchor.constraint(equalTo:view.leadingAnchor),
                shade.trailingAnchor.constraint(equalTo:view.trailingAnchor),
                shade.topAnchor.constraint(equalTo:view.topAnchor),
                shade.bottomAnchor.constraint(equalTo:view.bottomAnchor)
            ])
            // The reference Worlds/Profile header features a second flying
            // Bopi behind live UI. Match the Android native layered approach.
            if title == "SVJETOVI" || title == "PROFIL" || title == "TRGOVINA" {
                let flyingPilot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
                flyingPilot.contentMode = .scaleAspectFit
                flyingPilot.isAccessibilityElement=false
                flyingPilot.isUserInteractionEnabled=false
                flyingPilot.translatesAutoresizingMaskIntoConstraints=false
                view.addSubview(flyingPilot)
                NSLayoutConstraint.activate([
                    flyingPilot.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor,constant:37),
                    flyingPilot.trailingAnchor.constraint(equalTo:view.trailingAnchor,constant:-7),
                    flyingPilot.widthAnchor.constraint(equalToConstant:150),
                    flyingPilot.heightAnchor.constraint(equalToConstant:165)
                ])
            }
        }
        let scroll=UIScrollView()
        scroll.translatesAutoresizingMaskIntoConstraints=false
        // Extended Settings requires readable controls, not a downscaled
        // whole screen. Only this advanced Settings sheet scrolls; the normal
        // reference menus still fit inside the device's safe-area viewport.
        let isExtendedSettings = title == "POSTAVKE"
        scroll.alwaysBounceVertical=isExtendedSettings
        scroll.isScrollEnabled=isExtendedSettings
        scroll.showsVerticalScrollIndicator=isExtendedSettings
        scroll.keyboardDismissMode = .interactive
        view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:view.trailingAnchor)])
        let stack=UIStackView()
        stack.axis = .vertical;stack.alignment = .fill
        let compact = title == "TRGOVINA" || title == "MOJI REKORDI" || title == "PROFIL" || title == "POSTIGNUĆA" || title == "KOLEKCIJA"
        stack.spacing=title == "BOPAVI" || compact ? 7 : 12
        stack.translatesAutoresizingMaskIntoConstraints=false
        stack.layoutMargins=title == "BOPAVI" ?
            UIEdgeInsets(top:12,left:18,bottom:12,right:18) :
            compact ? UIEdgeInsets(top:7,left:12,bottom:8,right:12) :
            UIEdgeInsets(top:18,left:18,bottom:28,right:18)
        stack.isLayoutMarginsRelativeArrangement=true
        scroll.addSubview(stack)
        NSLayoutConstraint.activate([
            stack.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor),
            stack.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor),
            stack.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor),
            stack.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor),
            stack.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor)])
        if title != "BOPAVI" {
            let logo=UIImageView(image:UIImage(named:"Logo"))
            logo.contentMode = .scaleAspectFit
            logo.accessibilityElementsHidden=true
            logo.heightAnchor.constraint(equalToConstant:compact ? 68 : 94).isActive=true
            if title == "POSTAVKE" {
                // Same selected-pilot header arrangement as Android, drawn from
                // shipped standalone portrait art rather than a reference PNG.
                let pilot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
                pilot.contentMode = .scaleAspectFit
                pilot.isAccessibilityElement=false
                pilot.widthAnchor.constraint(equalToConstant:117).isActive=true
                pilot.heightAnchor.constraint(equalToConstant:117).isActive=true
                let hero=UIStackView(arrangedSubviews:[logo,pilot])
                hero.axis = .horizontal
                hero.alignment = .center
                hero.spacing = 3
                hero.heightAnchor.constraint(equalToConstant:118).isActive=true
                stack.addArrangedSubview(hero)
            } else {
                stack.addArrangedSubview(logo)
            }
            let headline=label(title,31,.white,stack)
            // Retain an independently accessible native heading rather than
            // a baked title inside the concept painting. Saturated azure
            // metal and cyan edge mirror Android's shared header treatment.
            headline.backgroundColor=UIColor(red:0.025,green:0.28,blue:0.66,alpha:0.98)
            headline.layer.cornerRadius=23
            headline.layer.borderWidth=2
            headline.layer.borderColor=UIColor(red:0.45,green:0.91,blue:1,alpha:1).cgColor
            headline.layer.shadowColor=UIColor(red:0.02,green:0.78,blue:1,alpha:1).cgColor
            headline.layer.shadowOpacity=0.24
            headline.layer.shadowRadius=6
            headline.layer.shadowOffset=CGSize(width:0,height:2)
            headline.clipsToBounds=false
            headline.heightAnchor.constraint(greaterThanOrEqualToConstant:compact ? 49 : 58).isActive=true
            if !subtitle.isEmpty {label(subtitle,15,UIColor(red:0.79,green:0.93,blue:1,alpha:1),stack)}
        }
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
        let stack=menu("BOPAVI","")
        view.backgroundColor=UIColor(red:0.04,green:0.45,blue:0.79,alpha:1)
        if let artwork=UIImage(named:"HomeArt") {
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
        // HomeArt contains scenery alone; the native wordmark, selected pilot,
        // currency and all controls sit above it as responsive live views.
        // Home fits exactly within the available iPhone safe area: no scrolling,
        // clipped footer, or large blank region below the navigation.
        if let homeViewport=view.subviews.compactMap({$0 as? UIScrollView}).first {
            // Keep the footer at the bottom on tall devices, but permit a
            // larger intrinsic stack on short devices so viewport fitting
            // can scale all hit targets coherently instead of clipping them.
            stack.heightAnchor.constraint(greaterThanOrEqualTo:homeViewport.frameLayoutGuide.heightAnchor).isActive=true
        }
        // The two original transparent hero assets remain independent.
        let record=UILabel()
        record.text="★  "+(progress.languageCode=="hr" ? "NAJBOLJI REZULTAT: " : "BEST SCORE: ")+String(progress.bestPoints())
        record.font=UIFont.systemFont(ofSize:17,weight:.heavy)
        record.textColor = .white
        record.textAlignment = .center
        record.backgroundColor=UIColor(red:0.02,green:0.17,blue:0.42,alpha:0.93)
        record.layer.cornerRadius=21;record.clipsToBounds=true
        record.layer.borderWidth=2
        record.layer.borderColor=UIColor(red:0.26,green:0.84,blue:1,alpha:1).cgColor
        record.heightAnchor.constraint(equalToConstant:47).isActive=true
        record.accessibilityLabel=record.text
        stack.addArrangedSubview(record)
        let homeLogo=UIImageView(image:UIImage(named:"Logo"))
        homeLogo.contentMode = .scaleAspectFit
        homeLogo.isAccessibilityElement=false
        homeLogo.heightAnchor.constraint(equalToConstant:154).isActive=true
        stack.addArrangedSubview(homeLogo)
        let homePilot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
        homePilot.contentMode = .scaleAspectFit
        homePilot.isAccessibilityElement=false
        homePilot.heightAnchor.constraint(equalToConstant:310).isActive=true
        stack.addArrangedSubview(homePilot)
        let spacer=UIView()
        spacer.setContentHuggingPriority(.defaultLow,for:.vertical)
        stack.addArrangedSubview(spacer)
        button("▶  IGRAJ",in:stack){
            self.startGame(self.progress.chosenWorld(),1)
        }
        label(progress.languageCode=="hr" ? "Dodirni zaslon za zamah. Leti što dalje!" :
            "Tap to flap. Fly as far as you can!",14,.white,stack)
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
        let mascot=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
        mascot.contentMode = .scaleAspectFit
        mascot.isAccessibilityElement=false
        mascot.heightAnchor.constraint(equalToConstant:154).isActive=true
        panel.addArrangedSubview(mascot)
        label(progress.languageCode=="hr" ? "♛  PAUZA" : "♛  PAUSE",33,UIColor.white,panel)
        label((progress.languageCode=="hr" ? "Let je zaustavljen" : "Flight paused"),14,
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
            close(false);self.startGame(self.gameWorld,1)
        }
        command(progress.languageCode=="hr" ? "↪  IZLAZ" : "↪  EXIT",style:2){close(false);self.showHome()}
        label(progress.languageCode=="hr" ?
            "Nastavi let, pokušaj ponovno ili se vrati na početnu." :
            "Resume your flight, retry or return Home.",13,
            UIColor(red:0.82,green:0.92,blue:1,alpha:1),panel)
        UIAccessibility.post(notification:.screenChanged,argument:panel)
    }
    private func startGame(_ world:Int,_ number:Int){
        clear();gameWorld=world;gameNumber=number
        let game=GameSimulation(BopaviCore.createStream(world,number),endless:true,initialShield:0,initialMagnet:0,difficulty:progress.difficulty,initialOrdinal:number)
        sound.startWorld(world)
        let gameCanvas=GameCanvas(game:game,reducedMotion:progress.lessMotion,skinIndex:progress.skinIndex(),
            coinWallet:{[weak self] in self?.progress.coins() ?? 0},languageCode:progress.languageCode)
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
        let previous=progress.bestPoints()
        let newRecord=g.score()>previous && g.score()>0
        sound.effect("hit")
        progress.recordRun(g)
        clear()
        view.backgroundColor=UIColor(red:0.04,green:0.25,blue:0.54,alpha:1)
        let backdrop=UIImageView(image:UIImage(named:"HomeArt"))
        backdrop.translatesAutoresizingMaskIntoConstraints=false
        backdrop.contentMode = .scaleAspectFill
        backdrop.clipsToBounds=true
        backdrop.isAccessibilityElement=false
        view.addSubview(backdrop)
        NSLayoutConstraint.activate([
            backdrop.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            backdrop.trailingAnchor.constraint(equalTo:view.trailingAnchor),
            backdrop.topAnchor.constraint(equalTo:view.topAnchor),
            backdrop.bottomAnchor.constraint(equalTo:view.bottomAnchor)
        ])
        let shade=UIView()
        shade.translatesAutoresizingMaskIntoConstraints=false
        shade.backgroundColor=UIColor(red:0.01,green:0.07,blue:0.18,alpha:0.51)
        view.addSubview(shade)
        NSLayoutConstraint.activate([
            shade.leadingAnchor.constraint(equalTo:view.leadingAnchor),
            shade.trailingAnchor.constraint(equalTo:view.trailingAnchor),
            shade.topAnchor.constraint(equalTo:view.topAnchor),
            shade.bottomAnchor.constraint(equalTo:view.bottomAnchor)
        ])
        let scroll=UIScrollView()
        scroll.translatesAutoresizingMaskIntoConstraints=false
        scroll.isScrollEnabled=false
        scroll.showsVerticalScrollIndicator=false
        resultFitViewport=scroll
        view.addSubview(scroll)
        NSLayoutConstraint.activate([
            scroll.topAnchor.constraint(equalTo:view.safeAreaLayoutGuide.topAnchor),
            scroll.bottomAnchor.constraint(equalTo:view.safeAreaLayoutGuide.bottomAnchor),
            scroll.leadingAnchor.constraint(equalTo:view.safeAreaLayoutGuide.leadingAnchor),
            scroll.trailingAnchor.constraint(equalTo:view.safeAreaLayoutGuide.trailingAnchor)
        ])
        let panel=UIStackView()
        panel.axis = .vertical
        panel.alignment = .fill
        panel.spacing=12
        panel.translatesAutoresizingMaskIntoConstraints=false
        panel.isLayoutMarginsRelativeArrangement=true
        panel.layoutMargins=UIEdgeInsets(top:18,left:18,bottom:18,right:18)
        resultFitPanel=panel
        scroll.addSubview(panel)
        NSLayoutConstraint.activate([
            panel.leadingAnchor.constraint(equalTo:scroll.contentLayoutGuide.leadingAnchor),
            panel.trailingAnchor.constraint(equalTo:scroll.contentLayoutGuide.trailingAnchor),
            panel.topAnchor.constraint(equalTo:scroll.contentLayoutGuide.topAnchor),
            panel.bottomAnchor.constraint(equalTo:scroll.contentLayoutGuide.bottomAnchor),
            panel.widthAnchor.constraint(equalTo:scroll.frameLayoutGuide.widthAnchor),
            panel.heightAnchor.constraint(greaterThanOrEqualTo:scroll.frameLayoutGuide.heightAnchor)
        ])
        let portrait=UIImageView(image:UIImage(named:"BopiPortrait\(progress.skinIndex())"))
        portrait.contentMode = .scaleAspectFit
        portrait.isAccessibilityElement=false
        portrait.heightAnchor.constraint(equalToConstant:198).isActive=true
        panel.addArrangedSubview(portrait)
        label(newRecord ? "♛  NOVI REKORD!" : "REZULTAT",32,.white,panel)
        let stats=UIStackView()
        stats.axis = .vertical
        stats.alignment = .fill
        stats.spacing=7
        stats.layoutMargins=UIEdgeInsets(top:14,left:15,bottom:14,right:15)
        stats.isLayoutMarginsRelativeArrangement=true
        stats.layer.cornerRadius=24
        stats.layer.borderWidth=3
        stats.layer.borderColor=UIColor(red:0.3,green:0.85,blue:1,alpha:1).cgColor
        stats.backgroundColor=UIColor(red:0.02,green:0.13,blue:0.32,alpha:0.96)
        panel.addArrangedSubview(stats)
        func stat(_ title:String,_ number:Int){
            let row=UIStackView()
            row.axis = .horizontal
            row.alignment = .center
            let heading=UILabel()
            heading.text=title
            heading.font=UIFont.systemFont(ofSize:16,weight:.bold)
            heading.textColor = .white
            heading.setContentCompressionResistancePriority(.defaultLow,for:.horizontal)
            row.addArrangedSubview(heading)
            let value=UILabel()
            value.text=String(number)
            value.font=UIFont.systemFont(ofSize:24,weight:.heavy)
            value.textColor=UIColor(red:1,green:0.81,blue:0.3,alpha:1)
            row.addArrangedSubview(value)
            row.heightAnchor.constraint(equalToConstant:54).isActive=true
            stats.addArrangedSubview(row)
        }
        stat("★  REZULTAT",g.score())
        stat("♛  PROLAZI",g.totalPassed)
        stat("🏆  NAJBOLJI REZULTAT",progress.bestPoints())
        button("▶  PONOVO",in:panel){self.startGame(self.gameWorld,1)}
        button("⌂  POČETNA",in:panel,primary:false){self.showHome()}
        UIAccessibility.post(notification:.screenChanged,argument:panel)
    }

    private func alert(_ title:String,_ message:String){let a=UIAlertController(title:title,message:message,preferredStyle:.alert);a.addAction(UIAlertAction(title:"U redu",style:.default));present(a,animated:true)}
}
